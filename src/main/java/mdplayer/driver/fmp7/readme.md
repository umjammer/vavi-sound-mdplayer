# mdplayer.driver.fmp7

FMP7 (.owi) driver

## Usage

FMP7 is a Windows program that synthesizes its own OPNA/OPM and hands the result to DirectSound,
and there is no source for it and no dll to link against. So this plays the real thing: `FMP7.exe`
on an emulated PC (jdosbox, embedded through `jdos.api.JDosBox`). Point
`-Dmdplayer.fmp7.path=<dir>` at the directory holding `FMP7.exe`, `common_resrc.dll` and
`addon/exFMP7.dll` — the FMP7 distribution as it comes.

| property                | default               | what it is                                     |
|-------------------------|-----------------------|------------------------------------------------|
| `mdplayer.fmp7.path`    | `/usr/local/src/FMP7` | where FMP7.exe and its dlls are                |
| `mdplayer.fmp7.memory`  | `64`                  | megabytes the emulated PC gets — see below     |

**The machine needs 64MB.** On DOSBox's default 16 FMP7 starts, loads the song and plays it
perfectly well, and quietly never publishes its public work: the allocation in front of it fails
and FMP7 carries on without one. Nothing says so and the only symptom is a visualizer that stays
empty, so this is worth knowing before hunting it again.

A `jdosbox.reg` beside `FMP7.exe` is copied along with it and read by the win32 layer before the
program starts; without one, `Fmp7Player` writes its own. Every line of it buys emulated time at
the price of some quality — no oversampling, cheapest resampler, output at the rate everything
else runs at — and it is the difference between FMP7 needing nearly all of the machine and
needing about three quarters of it.

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

## What does not work yet

One FMP7 song after another is fine, however the first one ended — stopped part way through or
left to play out; `Fmp7DriverTest#aSecondSongPlaysAfterTheFirst` is that case and it passes. What
does not work is an FMP7 song after a **SMAF** song: FMP7 loads, sets up its DirectSound buffers,
and then throws an error code of its own (`_CxxThrowException type=.H`, which is an int) and dies
before it makes a sound.

It is the hazard the smaf driver's readme describes seen from the other side — the win32 layer is
a whole system in statics, written for a process that runs one program and exits — and it is a
jdosbox one rather than this driver's. Two of the things a machine used to leave behind are now
thrown away between machines: the COM interface tables, which held addresses into a machine that
had gone (that one stopped a second FMP7 song dead), and the emulated sound devices, whose
threads outlived their machine. Whatever this one is, it is neither of those; it happens between
FMP7 finishing with DirectSound and asking for its addon driver.

## Not this driver

`.mwi` is MML source, not a song: `FMC7.exe` compiles it into the `.owi` this plays. The other
FMP7 addon drivers (exFMP4, exPMD, exMXDRV, exS98P) play formats mdplayer already has drivers of
its own for, so only `.owi` is claimed here.
