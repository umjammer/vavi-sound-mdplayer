# mdplayer.driver.fmp7

FMP7 (.owi) driver

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

## When a song ends

A song with an end - `yonao_hasai.owi` is 242 seconds of one - used to hang there: the last bar
played and the player sat on it for ever. The readings the driver watches are taken as the samples
are handed over, so when FMP7 stops making sound there are no more readings, and the last one says
"playing" for the rest of time. Asked that, the driver waits.

So `Fmp7Player#isSongOver` reads the work itself once the queue has run dry, rather than looking at
a reading that has stopped coming - a tenth of a second apart at most, because a starved render
asks it for every frame of silence it puts out. FMP7 usually says plainly that it has stopped; if
it claims to be playing while making no sound and going nowhere for two seconds, that counts too.

## When a song does not start at all

The second and later machines in one JVM sometimes fail while loading, and FMP7 says so itself:
`exFMP7.dll: 未対応のエラーが発生しました [-1]` - "an unsupported error occurred" - in a MessageBox
that a machine with nothing drawn cannot show, which is why nobody had ever seen it. jdosbox logs
those now (`the guest says: ...`), and getting FMP7 to name its own error took reading the api
trail to find the `MessageBoxA` and then reading the literals out of `FMP7.exe` to see it was
scanning `addon\` for `exfmp7.dll`. It fails in two ways: the program throws and the machine goes,
or the machine stays up having published nothing.

What is left over from the machine before to cause it is still unknown. It is not the number of
previous machines (six short ones fail more often than six long ones) and it is not a settling
race (a second between machines makes it worse). So the driver does the one thing that is certain
to be right for a play list: a song that has made no sound gets **another machine**, up to
`mdplayer.fmp7.attempts` of them. A machine that has failed this way has never made a sound, so a
retry cannot cut a song short, and `Fmp7Player#isFinished` will not call a song finished while an
attempt is still owed - which is what used to drop it on the driver's first starved read.

Eight suite runs green, the retry firing in three of them; sixty seconds each of five songs, no
failures.

**The way to work on any of it** is
`Fmp7WorkProbe.queueSoak` in jDOSBox, which is this driver's arrangement in miniature - a bounded
queue the sink blocks on, a consumer taking a buffer at a time, a prime - and which found three of
the four above. At `-Dqueue=6 -Dprime=3` before the cursor fix it killed the machine at exactly
7.7s, four times out of four: a question that costs forty seconds instead of ten minutes, and the
only way any of this was separable from the noise. `-Djdos.trail=true` keeps a trail of the
guest's last api calls and dumps it whenever the program ends, which is how the timer flood was
found; it costs time per call, and FMP7 is sensitive enough to that to have died 6/6 with it on
before the timer fix.

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

## The end of a song, and why it used to freeze there

Two separate things had to be right before a song that ends could end.

The first is knowing it is over. FMP7 does not say so: the file has no length, and a song that
does not loop simply stops writing to its sound buffer. `Fmp7Player.isSongOver` therefore asks
the published work, but only once everything already handed over has been heard - the queue holds
seconds of sound the listener has not reached yet, and a song is not over while any of it is
left. The work says either that nothing is playing at all, or that the play position has not
moved for two seconds; either way the driver marks itself stopped, which is what every one of the
player's three ends-of-song is watching (`Audio.play`, `FormMain`'s screen loop, and the sampled
SPI all test `driver.stopped`).

The second is not blocking after that. `render` waits up to `READ_TIMEOUT_MILLIS` for the
emulator to come up with samples, and waiting - rather than filling silence in - is deliberate:
rendering to a file, that wait is the only thing holding the loop to the emulator's pace. But
`Audio.play` renders **two frames at a time**, and its fade-out is a hundred thousand samples
long. With nothing left to come, every one of those two-frame renders paid the full half second:
twenty five thousand of them, hours, with no sound and no end. The song *had* ended - the driver
said so on time, `Fmp7DriverTest#playsThroughTheDriver` had always shown it ending at 191.5s of
tritone's 191s - and the player still sat there. That is what "some songs freeze at the end" was,
and it is why it could not be found from the driver: rendered a buffer at a time, one wait covers
thousands of samples and costs nothing to notice.

So the wait is now skipped once the driver is stopped. `Fmp7DriverTest#endsThroughThePlayersOwnLoop`
is the test for it, and it is the player's own loop rather than the driver's - the only place the
failure exists. The smaf driver is built the same way and had the same freeze; it has the same fix.
