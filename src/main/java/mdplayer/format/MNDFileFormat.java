package mdplayer.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dotnet4j.io.Path;
import dotnet4j.util.compat.Tuple;
import mdplayer.PlayList;
import mdplayer.driver.Vgm;
import mdplayer.driver.mndrv.MnDrv;
import mdplayer.plugin.MNDPlugin;
import mdplayer.plugin.Plugin;
import mdplayer.properties.Resources;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * SIDFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class MNDFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".mnd"};
    }

    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile/* = null*/, Archive archive, Entry entry/* = null*/) {
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        Vgm.Gd3 gd3 = (new MnDrv()).getGD3Info(buf);
        music.title = gd3.trackName.isEmpty() ? Path.getFileName(file) : gd3.trackName;
        music.titleJ = gd3.trackName.isEmpty() ? Path.getFileName(file) : gd3.trackNameJ;
        music.game = gd3.gameName;
        music.gameJ = gd3.gameNameJ;
        music.composer = gd3.composer;
        music.composerJ = gd3.composerJ;
        music.vgmby = gd3.vgmBy;

        music.converted = gd3.converted;
        music.notes = gd3.notes;
        return Collections.singletonList(music);
    }

    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile/* = null*/) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public List<Tuple<String, byte[]>> getExtendFile(String fn, byte[] srcBuf, Archive archive, Entry entry) {
        List<Tuple<String, byte[]>> ret = new ArrayList<>();
        byte[] buf;

        int hs = (srcBuf[0x06] << 8) + srcBuf[0x07];
        int pcmptr[] = new int[] {(srcBuf[0x14] << 24) + (srcBuf[0x15] << 16) + (srcBuf[0x16] << 8) + srcBuf[0x17]};
        if (hs < 0x18) pcmptr[0] = 0;
        if (pcmptr[0] != 0) {
            int pcmnum = (srcBuf[pcmptr[0]] << 8) + srcBuf[pcmptr[0] + 1];
            pcmptr[0] += 2;
            for (int i = 0; i < pcmnum; i++) {
                String mndPcmFn = mdplayer.Common.getNRDString(srcBuf, pcmptr);
                buf = getExtendFileAllBytes(fn, mndPcmFn, archive, entry);
                if (buf != null) ret.add(new Tuple<>(".PND", buf));
            }
        }

        return ret;
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {
                "DriverBalance_MND.mbc",
                Resources.getDefaultVolumeBalance_MND()
        };
    }

    @Override
    public Plugin getPlugin() {
        return new MNDPlugin();
    }
}
