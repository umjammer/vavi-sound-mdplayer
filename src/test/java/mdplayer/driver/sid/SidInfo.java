package mdplayer.driver.sid;

import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTune;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;
import java.nio.file.Files;
import java.nio.file.Paths;


public class SidInfo {
    public static void main(String[] args) throws Exception {
        byte[] buf = Files.readAllBytes(Paths.get("../JSIDPlay2/tmp/Formula_1_Simulator.sid"));
        SidTune tune = new SidTune(buf, buf.length);
        SidTuneInfo info = tune.getInfo();
        System.out.println("Songs: " + info.songs());
        System.out.println("Clock Speed: " + info.clockSpeed());
        System.out.println("Song Speed: " + info.songSpeed());
    }
}
