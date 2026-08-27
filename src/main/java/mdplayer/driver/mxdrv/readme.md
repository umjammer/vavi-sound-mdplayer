# mdplayer.driver.mxdrv

MXDRV

## references

- https://github.com/kuma4649/MDPlayer/issues/33#issuecomment-522221372

### LZX compressed MDX

`LZX.X` is an X68000 packer, and a good few of the MDXs in circulation went through it - 1191 of
the 119635 in the collection this was worked out against, `LZX 0.32` and `LZX 0.42` between them.
It leaves the text header alone, so a packed song still shows its title and still names its PDX,
and packs only the body. A player that follows the part offsets at the head of that body lands in
68000 code instead, which is where the endless `index is out of bounds` such a song used to play
as came from. [`Lzx`](Lzx.java) expands one on the way in, so they simply play now.

Nothing describes the format but the stub the packer prepends, so it was read out of that. The
head of a packed body:

| offset | what                                                                          |
|--------|-------------------------------------------------------------------------------|
| +0x00  | `bra.s` to the bootstrap - where an unpacked MDX keeps its voice data offset   |
| +0x04  | `"LZX 0.32"` / `"LZX 0.42"`, overwritten with the load address at run time     |
| +0x12  | the size of the expanded body                                                  |
| +0x1e  | the size of the relocation table - zero for data, non-zero for an executable   |
| +0x22  | how far the packed stream is moved up before it is expanded over itself        |
| +0x26  | the bootstrap, ending in the `lea` that points at the stream                   |

The bootstrap moves the stream out of the way of its own output, points `a6` at it with
`lea (d8,pc,a6.l),a6` and expands from there down over the whole body, starting at +0x00. That
`lea` is the only thing that moves between the two versions - the stream starts 0x8c into the body
for 0.32 and 0xa4 for 0.42 - so `Lzx` reads the displacement out of the instruction rather than
keeping a table of versions.

The stream is LZSS over a bit stream read most significant bit first, with the bytes of a literal
and the operands of a match taken whole from between the bits:

| bits     | operands | what                                                                                                    |
|----------|----------|---------------------------------------------------------------------------------------------------------|
| `1`      | byte     | one literal byte                                                                                         |
| `0 0 nn` | byte     | match, distance `0x100 - byte` (1..256), length `nn + 2` (2..5)                                           |
| `0 1`    | word     | match, distance `-((0xffff0000 \| word) >> 3)` (1..8192), length `(word & 7) + 2` (3..9)                  |
| `0 1`    | word, byte | when the low three bits of the word are 0: length `byte + 1` (2..256), and a byte of 0 ends the stream |

A match is copied a byte at a time and is allowed to run off the end of its own output, so a
distance of 1 is how a run is written. When the stream ends short of the size at +0x12 the stub
zero fills the rest, and songs rely on it.

There is no test data in the tree for this - the samples live wherever the collection is - so
`LzxTest` carries a hand made song holding one of every token, and sweeps a whole collection when
`mdplayer.mdx.dir` in `local.properties` points at one. Of the 1191 packed songs there, 1189
expand to exactly the size they declare - the two that do not are the same truncated `ONIL.MDX`
twice - and 496 of them have an unpacked copy of the same song elsewhere in the collection to be
checked against, 477 of which come back byte for byte. The other 19 are different arrangements
that happen to share a title and a length with the packed song, which is all there is to pair them
by.
