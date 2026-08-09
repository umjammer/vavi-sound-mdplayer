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

Two other things a real rip will find, both covered by tests: a branch in a branch's delay slot
leaves the "a branch is pending" marker in `delayr`, which the C then writes one past the end of
its register file (`R3000Test`), and the IOP printf's integer conversions have to be written by
hand because java's `Formatter` has no `%u` and rejects a precision on `%d` and `%x` - Square's
IOP driver asks for its wave bank as `wave%4.4u.wd` (`PsxHwTest`).
