# mdplayer.driver.fmp7

FMP7 (.owi) driver, and its compiler (.mwi)

## Usage

FMP7 is a Windows program that synthesizes its own OPNA/OPM and hands the result to DirectSound,
and there is no source for it and no dll to link against. So this plays the real thing: `FMP7.exe`
on an emulated PC (jdosbox, embedded through `jdos.api.JDosBox`). Point
`-Dmdplayer.fmp7.path=<dir>` at the directory holding `FMP7.exe`, `common_resrc.dll` and
`addon/exFMP7.dll` — the FMP7 distribution as it comes.

| property               | default               | what it is                                             |
|------------------------|-----------------------|--------------------------------------------------------|
| `mdplayer.fmp7.path`   | `/usr/local/src/FMP7` | where FMP7.exe and its dlls are                        |
| `mdplayer.fmp7.memory` | `64`                  | megabytes the emulated PC gets — see below             |
| `mdplayer.fmp7.prime`  | `3`                   | seconds of audio in hand before the song starts        |
| `mdplayer.fmp7.queue`  | `6`                   | seconds the queue between the emulator and here holds  |
| `jdos.novideo`         | `true` unless set     | jdosbox's own: draw nothing — see below                |

**The machine needs 64MB.** On DOSBox's default 16 FMP7 starts, loads the song and plays it
perfectly well, and quietly never publishes its public work: the allocation in front of it fails
and FMP7 carries on without one. Nothing says so and the only symptom is a visualizer that stays
empty, so this is worth knowing before hunting it again.

A `jdosbox.reg` beside `FMP7.exe` is copied along with it and read by the win32 layer before the
program starts; without one, `Fmp7Player` writes its own. What is in it is FMP7's oversampling,
turned off for each of its three sound sources, which is the one setting of its own that costs
real time — see below for the ones that turn out not to.

## What it costs, and the two things that decide whether it keeps up

FMP7 synthesizes a whole OPNA (or OPM) in software, and how hard that is depends entirely on the
song. Measured here as the processor time one second of audio costs the machine's thread:

| song           | parts                 | cost |
|----------------|-----------------------|------|
| `829.owi`      | 6 FM, 3 SSG           | 0.25 |
| `overdose.owi` | 9 FM, 7 SSG, and PCM  | 0.70 |

A cost of 1.0 is a machine that can only just synthesize the song as fast as it is heard, with
nothing left for a heavy passage — which is what a song breaking up sounds like. Two things
matter, and both of them are worth more than every FMP7 setting put together:

**Draw nothing.** `-Djdos.novideo=true`, which `Fmp7Player` sets unless it has already been set,
is worth about half of everything the machine does. FMP7 is a gui program and will otherwise
spend its time drawing level meters and a keyboard for a window nobody is looking at — mdplayer
draws its own display from the work FMP7 publishes. On `overdose.owi` this is the difference
between a cushion that holds above three seconds for the whole song and one that reaches 0.9s
and then sits at nothing.

**Start behind.** A song is not evenly hard, so the player starts `mdplayer.fmp7.prime` seconds
behind the emulator rather than level with it. It costs less than it sounds: nothing is being
taken while the cushion builds, so the emulator has its whole margin to build with, and three
seconds of cushion take about two of waiting.

### what does not help

There is **no frequency to turn down**. FMP7 generates at 48kHz and there is no setting for it:
`ReSampleRate` and `ReSamplePower` only apply when its own resampler is enabled, which by default
it is not ("ダイレクト出力"), and sweeping `ReSampleRate` through all six of its values leaves the
sound buffer at 48000Hz and the cost unchanged. Nor does anything else it reads —
`BufferNum`, `DispFlags`, `MonitorInterval`, `LevelMeterSensitivity`, `PlayFlags` — move the cost
at all. The `OverSampling` values are the exception and are already off in the registry `Fmp7Player`
writes: turned on for all three sound sources they take `overdose.owi` from 0.70 to 0.85.

On the emulator's side, a profile of the machine's thread says why: 61% of it is in compiled
guest code, 17% in the dynamic core's own dispatch, 10% in blocks the compiler has not taken,
and everything else — memory, the scheduler, the win32 layer, reflection — under 3% each. The
10% looks like the obvious win and is not: only 742 of the 3622 blocks the song touches ever
reach the compiler, but pulling `compiler.threshold` down from 1000 to 1 compiles 1260 of them
and makes the whole thing *slower* (0.93 to 0.95), the compiling costing more than the
interpreting saved. Nothing else is left to remove; it is the synthesis itself.

**The jvm matters more than any of it.** The same song, the same everything else:

| jvm                                          | cost |
|----------------------------------------------|------|
| GraalVM 25                                   | 0.80 |
| OpenJDK 25                                   | 0.93 |
| OpenJDK 25, `-XX:-DontCompileHugeMethods`    | 0.88 |

jdosbox compiles the guest's code into java methods, and a hot one can be bigger than the 8000
bytecodes hotspot will jit; left at the default those run in hotspot's own interpreter. The
`run` profile passes that flag and `-XX:+UseParallelGC`. If a song still breaks up, GraalVM is
worth more than both.

## What paces it

FMP7 keeps time the way it would on real hardware: it writes into a DirectSound buffer ahead of
the play cursor and cannot get further ahead than the buffer is long, which here is 400ms. So the
emulated sound card has to take its samples at the rate they are meant to be heard at, and no
faster — which is what happens when `PcmQueue.write` blocks until the driver has made room. The
cushion the emulator gets to build is therefore the queue, three seconds of it; the 400ms is only
how far FMP7 itself runs ahead of that.

jdosbox plays a DirectSound buffer at an `AudioSink` differently from the way it plays one at a
sound card, and it has to: a card is a place, and a program that writes nothing to it hears the
last thing again, while a sink is a stream where everything handed over is heard once and in
order. So the sink is given what the program has written since the last time and nothing else,
and nothing at all while it has written nothing — which is also what keeps a program that is
still loading, and writing nothing for seconds at a time, from being run flat out into the queue.
One chunk of what was written is always left untaken, because a ring in which the play cursor has
caught the write cursor is a ring with no free space in it, which is the same thing a full one
looks like: FMP7 reads it that way, stops writing, and the song stops with it.

## The visualizer

There is no chip to read and nothing goes past on its way out either. What there is instead is
better than a register trace: FMP7 publishes what its driver is doing, part by part, as it does
it — a shared memory called `FMP7_PUBLIC_WORK`, laid out by `fmp32_sharework.h` and described at
[the api page](http://fmpdoc.fmp.jp/fmp7-api/). Those are the notes the MML asked for, with the
part they belong to and the sound source they went to, rather than a frequency worked back out of
a divider.

On a real PC another process reads that with `OpenFileMapping`. Here the other process is this
jvm, so `jdos.win.api.SharedMemory` is that call made from outside the machine. `Fmp7Work` reads
the bytes, `Fmp7Player` keeps them, and `Fmp7Reader` puts them on the fmdsp rows — FM, SSG and
PCM parts onto the three row groups, which is the driver's own idea of its parts rather than a
mapping. The analyzer bars are not part of this: FMP7's audio goes through mdplayer's own mixer,
so they are measured off the sound itself the way every rendered format's are.

Two things sit between what FMP7 says and what is being heard, and both are handled in
`Fmp7Player#workAt`. The queue is the first, and it is exact: a reading is taken every time
samples are handed over and filed under the position in the audio they were handed over at, so
the reading that belongs to the sound now leaving the mixer can be looked up rather than guessed.
The readings therefore have to reach back at least as far as the queue does — too few of them and
the oldest one still held is newer than the one wanted, which is a display running ahead of its
own music by however much is missing. The second is FMP7's own sound buffer, which is how far its
player has run ahead of its sound card, and that one is measured: at the moment the first sound
of the song is handed over, whatever FMP7 says it has already played is exactly that lead.

Measured against the audio, the two then agree to within about a tenth of a second, and hold
there for as long as the song runs.

## The compiler

An `.mwi` is the MML an `.owi` is compiled from, and opening one plays it: the plugin compiles it
first (`Fmp7Compiler`) and hands the driver the object. It takes about half a second and needs
nothing the player does not already need — `fmc.dll` and `common_resrc.dll` out of the same FMP7
directory.

It costs one machine, though, and that machine is spent before the player asks for its own — so an
`.mwi` meets the "second and later machines sometimes fail to start" problem one song sooner than
an `.owi` does. `mdplayer.fmp7.attempts` covers it the same way.

| property                          | default  | what it is                                     |
|-----------------------------------|----------|------------------------------------------------|
| `mdplayer.fmp7.compiler.memory`   | `32`     | megabytes the compiler's machine gets          |
| `mdplayer.fmp7.compiler.timeout`  | `120000` | how long a compile is given [ms]               |

**Not FMC7.exe.** The compiler FMP7 ships with is a gui program and it ignores its command line
entirely — its startup takes `__argc` and `__wargv` and nothing ever reads them again, which is
why `wine FMC7.exe song.mwi` sits there doing nothing. It compiles from its own file dialog, from
a file dropped on its window, or from one it has been told to watch, and none of those can be
driven from outside. What can is `fmc.dll`, the compile core underneath it, whose `Compile()` is
published for exactly this — [the api page](http://fmpdoc.fmp.jp/fmc-api/). So what runs on the
emulated PC is `fmc7c.exe`: a hundred lines of C that load that dll, call it, and print what it
said. Its source is `mdplayer/lib/fmp7/fmc7c.c`, beside the built copy in the jar, and the one
line that builds it is at the top of the file.

It is built with no C runtime at all — kernel32 and nothing else. A modern mingw links against the
ucrt (`api-ms-win-crt-*`), which the emulated PC does not have, so the six kernel32 calls it does
make are the whole of its dependencies. It also does its own utf-8 encoding and its own command
line splitting, for the same reason each time: the emulated PC has no code page 65001, no
`CommandLineToArgvW`, and a `GetCommandLineW` that hands back an ansi string.

`fmc32_control.h` is not published, so the shape of what `GetInfo` returns was read back out of a
compile — a type, and for a log entry the message, the line and column, and the part. It is
packed, which is why the offsets in `fmc7c.c` are not aligned.

### What it cannot do

**Say which line was wrong.** fmc.dll reports every error by throwing it — one C++ throw each,
caught a frame or two up — and jdosbox has no C++ exception handling, so the program dies at the
first one and takes the reason with it. A song that compiles never throws and does not care;
warnings do not throw either and come through in full. A song that does not compile is a song that
would not have compiled on Windows either — FMC7 has moved on since most of these were written and
is stricter than the version they were made with, and an envelope on a PCM part is an error now
where it was not in 2010 — but here all that can be said is that it was rejected.

**Carry a Japanese name into the object.** The text an `.owi` holds is UTF-16, converted from the
source by the emulated PC's ansi code page, which is a western one; a name written in kanji comes
back out of the object as the bytes it was, one character each. It costs nothing here because the
object is never written to disk and the source says the same thing in its own encoding — see
`Fmp7Plugin#getSourceMetaData`, which is what a song compiled here is shown from. It would matter
to anything that kept the object.

### What it took to run at all

`fmc.dll` is the first program here to lean on the C runtime, and jdosbox's `msvcr90` was missing
twelve of the functions it imports. An import jdosbox does not have is stubbed to return 0 rather
than refused, so this does not look like a missing function: `tolower` answered 0, every keyword
in the file compared equal to `""`, and the compiler rejected every song it was handed with an
error about the first line of the information block. Four more things had to be right before it
would compile anything:

- **`GetLastError` always answered 0.** Every api call clears the last error before it runs, and
  `GetLastError` is a call, so it cleared the thing it was there to read. fmc.dll deletes its
  output before writing it, sees the delete fail, asks why, is told "no error", and gives up.
- **`DeleteFile` on a missing file said `ERROR_PATH_NOT_FOUND`.** Windows says `FILE_NOT_FOUND`
  when only the file is missing, and that is the one fmc.dll carries on from.
- **`MultiByteToWideChar` read nothing when handed -1**, which is how nearly every caller says
  "the string ends at its nul". The title, the composer and the comment all came out empty.
- **An ansi string written back into guest memory went out as UTF-8.** `String.getBytes()` with no
  charset, against a `getString` that reads a byte to a character — so anything above 0x7f grew a
  byte on the way back and the two no longer agreed.

`std::string`'s `substr`, `erase`, `operator[]`, `operator=` and `npos` were missing from
`msvcp90` too. All of it is in jdosbox 0.74.36v.

### Which songs compile

Of the nine `.mwi` under `tmp/fmp7`, seven compile and two are rejected — the same seven and the
same two as under wine. Five of the seven produce the object wine's `fmc.dll` does byte for byte;
the other two are the two whose credits are written in kanji, and they differ only in how those
bytes were mapped into the text chunk (and in the checksum over it), which is the code page again.

Against the `.owi` their authors shipped in 2010, `deltaray` differs in six bytes and `Altair` in
one: the compiler version stamp in the header and the checksum of the chunk it sits in. The rest
differ by more, because the compiler has had nine versions of fixes since.
