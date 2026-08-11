# mdplayer.driver.smaf

smaf (.mmf) driver

## Usage

There is no MA-2/MA-3/MA-5 emulator to write chip registers to, so this plays the real thing:
[mmftool](https://github.com/murachue/mmftool) driving Yamaha's `M5_Emu*.dll` on an emulated PC
(jdosbox, embedded through `jdos.api.JDosBox`). Point `-Dmdplayer.smaf.mmftool=<dir>` at the
directory holding `mmftoolc.exe`, `M5_EmuHw.dll`, `M5_EmuSmw5.dll` and `DefMA3_16.vm3`.

mmftool needs one patch this project relies on: it reads `MMFTOOL_SAMPLE_RATE` (and
`MMFTOOL_MASTER_VOLUME`) from its environment, in `EmuSetSampleRate` before `EmuInitialize`.
That matters because synthesis costs in proportion to the rate: the heaviest MA-5 song is
synthesized at 0.95 of real time at 48000 and 1.43 at 32000, which is worth more than every
optimization made to the emulator put together. Only 22050, 32000, 44100 and 48000 work — the
dll answers anything else with silence rather than an error, so mmftool refuses those.

| property                 | default                  | what it is                                                       |
|--------------------------|--------------------------|------------------------------------------------------------------|
| `mdplayer.smaf.mmftool`  | `/usr/local/src/mmftool` | where mmftoolc.exe and its dlls are                              |
| `mdplayer.smaf.volume`   | `100`                    | the player's own master volume, 0-127; 127 clips on loud songs   |
| `mdplayer.smaf.rate.ma5` | `32000`                  | what MA-5 and MA-7 are synthesized at; costs only above 16kHz    |
| `mdplayer.smaf.prime`    | `3`                      | seconds of audio in hand before the song starts                  |

MA-1/2/3 and Uta are synthesized at 48000 — there is nothing to gain by lowering them. Raising
`rate.ma5` back to 48000 makes that song *behind* for its whole length, so it then has to start
on a ten second cushion instead of three; that follows the rate by itself and is not a setting.

The player must run with `-XX:+UseParallelGC` (the `run` profile does): the emulated PC is one
guest cpu on one host thread, pinned at 100% of a core for the whole song, and g1's write
barriers cost that thread about 5% for concurrency it never needs. No other core can help — x86
emulation of one guest cpu is serial, which is why a 24 core host shows 12% and still struggles.
