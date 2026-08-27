/*
 * fmc7c - a console front end for fmc.dll, the compile core of FMP7's MML compiler FMC7.
 *
 * FMC7.exe itself cannot be driven from outside: it ignores its command line entirely (its
 * startup takes __argc and __wargv and nothing ever reads them again), and compiles only from
 * its own file dialog, from a file dropped on its window, or from one it has been told to watch.
 * fmc.dll's Compile() is the published api for exactly this case - see
 * http://fmpdoc.fmp.jp/fmc-api/ - so this is the whole of the program: load the dll, call it,
 * and say what it said.
 *
 * It is built without a C runtime - kernel32 and nothing else - because the emulated PC it runs
 * on has kernel32 and not the runtime a modern mingw links against by default (the ucrt's
 * api-ms-win-crt-* stubs), and because there is nothing here worth a runtime.
 *
 *   i686-w64-mingw32-gcc -O2 -nostdlib -ffreestanding -e _entry -Wl,--subsystem,console \
 *       -o fmc7c.exe fmc7c.c -lkernel32
 *
 * Usage: fmc7c <in.mwi> [out.owi] [flags]
 * The exit code is fmc.dll's own status: 0 SUCCESS, 1 ERROR_COMPILE, then the file errors.
 * Every error and warning is written to stdout, one to a line, as
 *   fmc7c: error 509:36 [P0] <what fmc.dll had to say>
 *
 * @author Naohide Sano
 */
#include <windows.h>

typedef int  (__stdcall *COMPILE)(LPWSTR in, LPWSTR out, DWORD flags);
typedef int  (__stdcall *GETINFONUM)(void);
typedef void*(__stdcall *GETINFO)(int no);
typedef void (__stdcall *FREE)(void);
typedef void (__stdcall *GETVER)(int *major, int *minor, int *rev);

/*
 * One of the things fmc.dll has to say about a compile, laid out as it writes it.
 *
 * fmc32_control.h is not published, so this is the part of FMC32::INFO that was read back out of
 * a compile: a type, and for a log entry the message, where it happened and which part it was
 * in. The dll packs it, hence the pragma - the ints after the name are not aligned.
 */
#pragma pack(push, 1)
typedef struct {
    int type;               /* 0 file, 1 part, 2 log */
    int kind;               /* log: 0 error, 1 warning */
    const wchar_t *message;
    int line;
    int column;
    wchar_t part[3];
} LOG;
#pragma pack(pop)

static HANDLE out;

static void puts_(const char *s) {
    DWORD written = 0;
    int length = 0;
    while (s[length]) length++;
    WriteFile(out, s, length, &written, NULL);
}

/*
 * A wide string, as utf-8.
 *
 * Encoded here rather than by WideCharToMultiByte because the emulated PC has no code page 65001
 * (jdosbox's Kernel32 answers anything but the ansi one with "not implemented yet" and stops the
 * program), and fmc.dll has plenty to say in Japanese - which is the half of what it says that
 * matters most. Everything fmc.dll writes is in the basic plane, so surrogates only have to be
 * passed through as the pair they are, not recombined.
 */
static void putw_(const wchar_t *s) {
    char buf[1024];
    int n = 0;
    if (!s) { puts_("(null)"); return; }
    while (*s && n < (int) sizeof buf - 4) {
        unsigned c = (unsigned) *s++;
        if (c < 0x80) {
            buf[n++] = (char) c;
        } else if (c < 0x800) {
            buf[n++] = (char) (0xc0 | (c >> 6));
            buf[n++] = (char) (0x80 | (c & 0x3f));
        } else {
            buf[n++] = (char) (0xe0 | (c >> 12));
            buf[n++] = (char) (0x80 | ((c >> 6) & 0x3f));
            buf[n++] = (char) (0x80 | (c & 0x3f));
        }
    }
    buf[n] = 0;
    puts_(buf);
}

static void puti_(int v) {
    char buf[16];
    int i = sizeof buf - 1;
    unsigned u = v < 0 ? -(unsigned) v : (unsigned) v;
    buf[i] = 0;
    do { buf[--i] = (char) ('0' + u % 10); u /= 10; } while (u);
    if (v < 0) buf[--i] = '-';
    puts_(buf + i);
}

static int atoi_(const wchar_t *s) {
    int v = 0;
    while (*s >= L'0' && *s <= L'9') v = v * 10 + (*s++ - L'0');
    return v;
}

/*
 * The command line, split on spaces with "quotes" honoured, taken from GetCommandLineA and
 * widened a byte at a time.
 *
 * Not CommandLineToArgvW, and not GetCommandLineW: the emulated PC has no CommandLineToArgvW at
 * all, and its GetCommandLineW hands back an ANSI string in a buffer sized for one (jdosbox's
 * WinProcess#getCommandLineW copies with strcpy where strcpyW is meant), so a program that asks
 * either of them there is told it has no arguments. Widening by hand rather than through
 * MultiByteToWideChar for the same sort of reason - the emulated one counts its result the way
 * the terminating-nul form does, whatever it was handed - and the drive under all this carries
 * ascii names only, so a byte is a character here.
 */
static int split(char *line, wchar_t **argv, wchar_t *buf, int max) {
    int argc = 0;
    while (*line && argc < max) {
        int quoted = 0;
        while (*line == ' ' || *line == '\t') line++;
        if (!*line) break;
        if (*line == '"') { quoted = 1; line++; }
        argv[argc++] = buf;
        while (*line && (quoted ? *line != '"' : (*line != ' ' && *line != '\t'))) {
            *buf++ = (wchar_t) (unsigned char) *line++;
        }
        if (*line) line++;
        *buf++ = 0;
    }
    return argc;
}

void entry(void) {
    int argc, status, i;
    wchar_t *argv[8];
    /* three paths and a number, and small enough that gcc does not want a stack probe for it */
    wchar_t argbuf[1024];
    HMODULE h;
    COMPILE compile;
    GETINFONUM infoNum;
    GETINFO info;
    FREE freeInfo;
    GETVER version;

    out = GetStdHandle(STD_OUTPUT_HANDLE);
    argc = split(GetCommandLineA(), argv, argbuf, 8);
    if (argc < 2) {
        puts_("usage: fmc7c <in.mwi> [out.owi] [flags]\n");
        ExitProcess(2);
    }

    h = LoadLibraryW(L"fmc.dll");
    if (!h) {
        puts_("fmc7c: no fmc.dll beside me\n");
        ExitProcess(3);
    }
    compile = (COMPILE) GetProcAddress(h, "Compile");
    infoNum = (GETINFONUM) GetProcAddress(h, "GetInfoNum");
    info = (GETINFO) GetProcAddress(h, "GetInfo");
    freeInfo = (FREE) GetProcAddress(h, "Free");
    version = (GETVER) GetProcAddress(h, "GetVer");
    if (!compile) {
        puts_("fmc7c: fmc.dll has no Compile\n");
        ExitProcess(3);
    }
    if (version) {
        int major = 0, minor = 0, revision = 0;
        char rev[2];
        version(&major, &minor, &revision);
        puts_("fmc7c: fmc.dll ");
        puti_(major);
        puts_(".");
        if (minor < 10) puts_("0");
        puti_(minor);
        rev[0] = (char) revision;
        rev[1] = 0;
        puts_(rev);
        puts_("\n");
    }

    status = compile(argv[1],
                     argc > 2 && argv[2][0] ? argv[2] : NULL,
                     argc > 3 ? (DWORD) atoi_(argv[3]) : 0);

    if (infoNum && info) {
        int n = infoNum();
        for (i = 0; i < n; i++) {
            LOG *e = (LOG *) info(i);
            if (!e || e->type != 2) continue;
            puts_(e->kind ? "fmc7c: warning " : "fmc7c: error ");
            puti_(e->line);
            puts_(":");
            puti_(e->column);
            puts_(" [");
            putw_(e->part);
            puts_("] ");
            putw_(e->message);
            puts_("\n");
        }
    }
    if (freeInfo) freeInfo();

    puts_("fmc7c: status ");
    puti_(status);
    puts_("\n");
    ExitProcess(status);
}
