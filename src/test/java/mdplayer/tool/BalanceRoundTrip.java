package mdplayer.tool;

import java.nio.file.Files;
import java.nio.file.Path;
import mdplayer.Setting;
import mdplayer.chips.Ym2612Chip;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2608Chip;
import static mdsound.MDSound.Chip.MAIN_TAG;

/** ad-hoc verification of Setting.Balance XML round-trip. */
public class BalanceRoundTrip {
    public static void main(String[] args) throws Exception {
        // 1) load an existing preset xml straight from resources
        Path xml = Path.of("src/main/resources/mdplayer/resources/DefaultVolumeBalance_VGM.xml");
        // put a couple of non-zero values in a temp copy to prove round-trip
        Setting.Balance b = new Setting.Balance();
        b.setMasterVolume(-5);
        b.setVolume(MAIN_TAG, Ym2612Chip.class, -12);
        b.setVolume(MAIN_TAG, Sn76489Chip.class, 7);
        b.setVolume("FM", Ym2608Chip.class, -3);
        b.setGimicOPNVolume(31);

        Path tmp = Files.createTempFile("bal", ".xml");
        b.save(tmp);
        System.out.println("---- serialized ----");
        System.out.println(Files.readString(tmp));

        Setting.Balance r = Setting.Balance.load(tmp);
        System.out.println("---- reloaded ----");
        System.out.println("master=" + r.getMasterVolume() + " (exp -5)");
        System.out.println("YM2612=" + r.getVolume(MAIN_TAG, Ym2612Chip.class) + " (exp -12)");
        System.out.println("SN76489=" + r.getVolume(MAIN_TAG, Sn76489Chip.class) + " (exp 7)");
        System.out.println("YM2608.FM=" + r.getVolume("FM", Ym2608Chip.class) + " (exp -3)");
        System.out.println("GimicOPN=" + r.getGimicOPNVolume() + " (exp 31)");

        boolean ok = r.getMasterVolume()==-5 && r.getVolume(MAIN_TAG,Ym2612Chip.class)==-12
            && r.getVolume(MAIN_TAG,Sn76489Chip.class)==7 && r.getVolume("FM",Ym2608Chip.class)==-3
            && r.getGimicOPNVolume()==31;

        // 2) load the real resource file (all zeros) - should not throw, master 0
        Setting.Balance res = Setting.Balance.load(xml);
        System.out.println("---- real resource VGM ----");
        System.out.println("loaded=" + (res!=null) + " master=" + (res!=null?res.getMasterVolume():"?")
            + " YM2612=" + (res!=null?res.getVolume(MAIN_TAG,Ym2612Chip.class):"?"));

        System.out.println(ok && res != null ? "ROUNDTRIP: PASS" : "ROUNDTRIP: FAIL");
    }
}
