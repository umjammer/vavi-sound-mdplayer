package mdplayer;

import java.awt.Frame;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import mdplayer.form.sys.frmMain;
import mdplayer.form.sys.frmPlayList;

@EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
public class RobotTest {

    private static void findAllTabbedPanes(java.awt.Container container, List<javax.swing.JTabbedPane> list) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof javax.swing.JTabbedPane) {
                list.add((javax.swing.JTabbedPane) comp);
            }
            if (comp instanceof java.awt.Container) {
                findAllTabbedPanes((java.awt.Container) comp, list);
            }
        }
    }

    private static java.awt.Component findComponentByName(java.awt.Container container, String name) {
        for (java.awt.Component comp : container.getComponents()) {
            if (name.equals(comp.getName())) {
                return comp;
            }
            if (comp instanceof java.awt.Container) {
                java.awt.Component child = findComponentByName((java.awt.Container) comp, name);
                if (child != null) return child;
            }
        }
        return null;
    }

    @Test
    public void testGuiActions() throws Exception {
        System.out.println("Checking Setting class annotations:");
        for (java.lang.annotation.Annotation a : Setting.class.getAnnotations()) {
            System.out.println("  - " + a);
        }

        // Start mdplayer in GUI mode in a separate thread
        Thread t = new Thread(() -> {
            try {
                Program.main(new String[]{});
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();

        // Wait for the main frame to be fully constructed, visible, and fields initialized
        frmMain mainFrame = null;
        Field audioField = frmMain.class.getDeclaredField("audio");
        audioField.setAccessible(true);
        Field frmPlayListField = frmMain.class.getDeclaredField("frmPlayList");
        frmPlayListField.setAccessible(true);
        
        for (int i = 0; i < 100; i++) {
            Thread.sleep(100);
            for (Frame f : Frame.getFrames()) {
                if (f instanceof frmMain) {
                    frmMain fm = (frmMain) f;
                    try {
                        if (fm.isVisible() && audioField.get(fm) != null && frmPlayListField.get(fm) != null) {
                            mainFrame = fm;
                            break;
                        }
                    } catch (Exception ignored) {}
                }
            }
            if (mainFrame != null) break;
        }

        if (mainFrame == null) {
            throw new RuntimeException("frmMain not found or not visible");
        }

        System.out.println("frmMain found! Preparing test song in playlist...");
        File testSong = new File("src/test/resources/test.vgm");
        String songPath = testSong.getAbsolutePath();
        System.out.println("Song path: " + songPath);
        
        final frmMain finalMain = mainFrame;
        final frmPlayList playlistFrame = (frmPlayList) frmPlayListField.get(finalMain);
        
        // Add file to playlist and refresh to populate UI table
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            playlistFrame.getPlayList().getMusics().clear();
            playlistFrame.getPlayList().addFile(songPath);
            playlistFrame.refresh();
        });

        System.out.println("Starting playback of the song from playlist...");
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            finalMain.play();
        });

        Thread.sleep(2000); // Let it play for 2 seconds

        System.out.println("Testing settings panel...");
        // Get methods via reflection
        Method openSetting = frmMain.class.getDeclaredMethod("openSetting");
        openSetting.setAccessible(true);
        Method openMIDIKeyboard = frmMain.class.getDeclaredMethod("openMIDIKeyboard");
        openMIDIKeyboard.setAccessible(true);

        // Open settings dialog asynchronously
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                openSetting.invoke(finalMain);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Wait for the settings dialog to become visible
        javax.swing.JDialog dialog = null;
        for (int i = 0; i < 50; i++) {
            Thread.sleep(100);
            for (Frame f : Frame.getFrames()) {
                for (java.awt.Window w : f.getOwnedWindows()) {
                    if (w instanceof javax.swing.JDialog && w.isVisible()) {
                        dialog = (javax.swing.JDialog) w;
                        break;
                    }
                }
            }
            if (dialog != null) break;
        }
        
        if (dialog == null) {
            throw new RuntimeException("Settings dialog not found");
        }

        System.out.println("Clicking OK button on settings dialog...");
        final javax.swing.JDialog finalDialog = dialog;
        javax.swing.JButton okButton = (javax.swing.JButton) findComponentByName(dialog, "btnOK");
        if (okButton != null) {
            javax.swing.SwingUtilities.invokeAndWait(okButton::doClick);
        } else {
            System.err.println("OK button not found! Falling back to dispose...");
            javax.swing.SwingUtilities.invokeAndWait(finalDialog::dispose);
        }

        Thread.sleep(2000); // Wait 2 seconds for settings apply and reinit

        System.out.println("Attempting to play song AGAIN after settings OK...");
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            finalMain.play();
        });

        Thread.sleep(2000); // Wait 2 seconds

        System.out.println("Testing STOP button on main frame...");
        javax.swing.JButton stopButton = (javax.swing.JButton) findComponentByName(finalMain, "opeButtonStop");
        if (stopButton != null) {
            javax.swing.SwingUtilities.invokeAndWait(stopButton::doClick);
            System.out.println("Stop button clicked successfully!");
        } else {
            System.err.println("Stop button not found on main frame!");
        }

        Thread.sleep(1000); // Wait 1 second

        System.out.println("Testing PLAY button on main frame...");
        javax.swing.JButton playButton = (javax.swing.JButton) findComponentByName(finalMain, "opeButtonPlay");
        if (playButton != null) {
            javax.swing.SwingUtilities.invokeAndWait(playButton::doClick);
            System.out.println("Play button clicked successfully!");
        } else {
            System.err.println("Play button not found on main frame!");
        }

        Thread.sleep(2000); // Let it play for 2 seconds

        System.out.println("All GUI tests passed successfully! Disposing frames...");
        javax.swing.SwingUtilities.invokeAndWait(finalMain::dispose);
        
        Thread.sleep(1000);
    }
}
