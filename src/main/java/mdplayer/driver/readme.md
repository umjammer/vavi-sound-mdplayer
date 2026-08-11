# package mdplayer.driver

## Usage

### how to create sample

#### ♪ XGM

[sgdk](https://github.com/Stephane-D/SGDK)

````shell
$ cd $SGDK
$ bin/xgmtool ./sample/game/sonic/res/music/sonic1.vgm out1.xgm
````

#### ♪ XGM2

[sgdk](https://github.com/Stephane-D/SGDK)

````shell
$ cd $SGDK
$ java -jar bin/xgm2tool.jar ./sample/game/sonic/res/music/sonic1.vgm out2.xgm                                                                                           vavi
````

#### ♪ ZGM

[mml2vgm](https://github.com/kuma4649/mml2vgm)

#### ♪ PSF / PSF2

[aosdk](https://github.com/nmlgc/aosdk) ships a psf and a psf2 in its `samples/` directory,
which is what `tmp/psf/pe.psf` and `tmp/psf/01.psf2` are.

A `.minipsf` holds only what makes it different from a library and names that library in its
`_lib` tag, so the `.psflib` has to sit beside it (or on the file search path). The same goes
for `.minipsf2` and its `.psf2lib`.

The reference audio the tests compare against was rendered by aosdk itself: build only the psf
engine plus a small main that writes a wav, i.e.

````shell
$ gcc -O2 -DPATH_MAX=1024 -DHAS_PSXCPU=1 -DLSB_FIRST=1 -I. -Ieng_psf -o refmain \
    refmain.c corlett.c utils.c eng_psf/eng_psf.c eng_psf/eng_psf2.c eng_psf/psx.c \
    eng_psf/psx_hw.c eng_psf/peops/spu.c eng_psf/peops2/spu.c eng_psf/peops2/registers.c \
    eng_psf/peops2/dma.c -lz -lm
````

where `refmain.c` calls `psf_start`/`psf_sample`/`psf_frame` (735 samples then one frame) and
dumps the samples. The GUI's full build needs GLFW, which this avoids.

The port is sample exact against that reference, with one **deliberate** exception: aosdk's
`DeliverEvent` leaves `psx_bios_hle` with a bare `return` when the event is not active, which
skips the `PC = RA` at the bottom of the function and leaves the pc on the call - so the song
re-runs it for ever and plays silence. `PsxHw` returns to the caller instead (`PsxHwTest`
covers it). Fourteen of the forty six tracks of Dragon Quest Monsters 1+2 are silent in aosdk
for this reason and play here.

A psf says how long it is in its `length` tag, and most rips carry one; the ones that do not
(the whole PlayOnline Viewer set, `BATTLE1.psf2`) would play for ever, so `Setting.Psf` stands in
with a default length and fade — three minutes and ten seconds, which is what the other psf
players use. Set the length to 0 there to let such a song run on.

#### ♪ smaf (.mmf)

There is no MA-2/MA-3/MA-5 emulator to write chip registers to, so this format is played by the
real thing: `mmftoolc.exe` (a Windows build of murachue's mmftool driving Yamaha's `M5_Emu*.dll`)
running on an emulated PC. jdosbox is the emulator, embedded through its `jdos.api.JDosBox`
facade; `mdplayer.lib.smaf.MmfToolPlayer` boots a headless machine with the tool's directory
copied onto drive C, and takes what the guest writes to its `waveOut` device through a
`jdos.api.AudioSink`. Point `-Dmdplayer.smaf.mmftool=<dir>` at the directory holding
`mmftoolc.exe`, `M5_EmuHw.dll`, `M5_EmuSmw5.dll` and `DefMA3_16.vm3`.

**Settings.** All of it is system properties; only the first one has to be set, and then only if
the tool is not where it is looked for.

| property                  | default                  | what it is                                                                                                          |
|---------------------------|--------------------------|---------------------------------------------------------------------------------------------------------------------|
| `mdplayer.smaf.mmftool`   | `/usr/local/src/mmftool` | directory holding `mmftoolc.exe` and its dlls                                                                       |
| `mdplayer.smaf.volume`    | `100`                    | the player's own master volume, 0-127, handed to it as `MMFTOOL_MASTER_VOLUME`. Its top setting clips on loud songs |
| `mdplayer.smaf.prime`     | `0`                      | fixed cushion in seconds; 0 works it out from how the emulator is doing                                             |
| `mdplayer.smaf.song`      | `60`                     | the song length the cushion is built to cover, when the emulator is running short                                   |
| `mdplayer.smaf.queue`     | `20`                     | how many seconds of audio the queue holds - the cushion's ceiling                                                   |
| `mdplayer.smaf.cycles`    | `max`                    | how much of the host the emulated cpu may take                                                                      |
| `mdplayer.smaf.threshold` | `1000`                   | how often a block runs before jdosbox compiles it                                                                   |

Raise `mdplayer.smaf.prime` if a song still stutters (it trades silence at the start for it);
lower it, or set it to something small like `1`, to start sooner on a fast machine. The rest are
there to be measured against, not to be turned.

`SmafDriverTest` takes `mdplayer.smaf.test.mmf` (which song) and `mdplayer.smaf.test.seconds`
(how long to let it play), and needs `-Dvavi.test=ai`.
