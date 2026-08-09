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
