# fmp

| name | type | status | desxription                  |
|------|------|:------:|------------------------------|
| MPI  | MML  |        | for OPN                      |
| MVI  | MML  |        | for OPNA                     |
| MZI  | MML  |        | for OPN or OPNA w/ PPZ8      |
| OPI  | SEQ  |   ✅️   | for OPN                      |
| OVI  | SEQ  |   ✅️   | for OPNA                     |
| OZI  | SEQ  |   ✅️   | for OPN or OPNA w/ PPZ8      |
| PVI  | PCM  |   ✅️   | ADPCM for FMP, PMDPPZ, PMDB2 |

- PPZ ... SSGPCM PCM 3ch
- PPZ8 ... driving by 86/WSS = OZI (✅️ plugin)
- PDZF, Z8X ... remaining PPZ8 channels XYZ driving by FM3Extend part
- .OWI, .PWI ... [FMP7](http://guu.fmp.jp/archives/493)

## References

 - https://www.aosoft.jp/pdzfz8x/
 - https://github.com/yuuqilin/RetroJapSound
 - https://github.com/myon98/98fmplayer/blob/master/fmdriver/fmdriver_fmp.c (c, reverse engineered)

## TODO

 - ~~fmp7~~
   - ~~dosbox?~~ 
