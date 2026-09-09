# mdsound.driver.mgsdrv

MSX

## References

- samples
  - https://www.msx.org/downloads/1427-classified-mgs-music-files
  - `msx_music_data`, whose `Mus-MGSDRV` carries each song's MML beside the .mgs it compiles to
- MGSC.COM on a CP/M emulator, which this port follows
  - https://github.com/digital-sound-antiques/mgsc
- data format
  - https://github.com/digital-sound-antiques/mgsc/blob/master/mgs-format.md
- MML syntax
  - http://www.gigamix.jp/mgsdrv/MGSC111.TXT

### MML

A ".mus" is the MML a ".mgs" is compiled from, and it is compiled on the way to playing it by
`mdplayer.lib.mgsc.MgscCompiler`, which runs the real MSX `MGSC.COM` on a Z80 with enough
MSX-DOS under it. Put `MGSC.COM` next to `MGSDRV.COM`, under `mdplayer.mgs.dir`.

Note that ".mus" is PC98 MUAP98's MML as well - only the `#` directives inside tell them apart,
which is what `MgscCompiler.isMgsMml` and `FileFormat.accepts` are for.
