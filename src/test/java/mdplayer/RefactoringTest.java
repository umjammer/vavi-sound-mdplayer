package mdplayer;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import mdplayer.form.sys.FormMain;
import mdplayer.form.sys.FormPlayList;


@EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
public class RefactoringTest {

    private static final Set<String> IGNORED_NAMES = Set.of(
        "tsmiOpenFile",
        "tsmiExit",
        "tsmiOption",
        "tsmiPlay",
        "tsmiStop",
        "tsmiPause",
        "tsmiFadeOut",
        "tsmiSlow",
        "tsmiFf",
        "tsmiNext",
        "tsmiPlayMode"
    );

    private static void getMenuItems(Component comp, List<JMenuItem> list) {
        if (comp instanceof JMenu) {
            JMenu menu = (JMenu) comp;
            for (Component sub : menu.getMenuComponents()) {
                getMenuItems(sub, list);
            }
        } else if (comp instanceof JMenuItem) {
            list.add((JMenuItem) comp);
        } else if (comp instanceof JPopupMenu) {
            JPopupMenu popup = (JPopupMenu) comp;
            for (Component sub : popup.getComponents()) {
                getMenuItems(sub, list);
            }
        } else if (comp instanceof Container) {
            Container container = (Container) comp;
            for (Component sub : container.getComponents()) {
                getMenuItems(sub, list);
            }
        }
    }

    private static void cleanupExtraWindows(FormMain mainFrame, FormPlayList playlistFrame) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (Window w : Window.getWindows()) {
                if (w != mainFrame && w != playlistFrame && w.isVisible()) {
                    w.setVisible(false);
                    w.dispose();
                }
            }
        });
    }

    @Test
    public void testAllFormExecutions() throws Exception {
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
        FormMain mainFrame = null;
        Field audioField = FormMain.class.getDeclaredField("audio");
        audioField.setAccessible(true);
        Field frmPlayListField = FormMain.class.getDeclaredField("frmPlayList");
        frmPlayListField.setAccessible(true);
        
        for (int i = 0; i < 100; i++) {
            Thread.sleep(100);
            for (Frame f : Frame.getFrames()) {
                if (f instanceof FormMain) {
                    FormMain fm = (FormMain) f;
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

        final FormMain finalMain = mainFrame;
        final FormPlayList playlistFrame = (FormPlayList) frmPlayListField.get(finalMain);

        System.out.println("frmMain found! Preparing test song in playlist...");
        File testSong = new File("src/test/resources/test.vgm");
        String songPath = testSong.getAbsolutePath();
        System.out.println("Song path: " + songPath);
        
        // Add file to playlist and refresh to populate UI table
        SwingUtilities.invokeAndWait(() -> {
            playlistFrame.getPlayList().getMusics().clear();
            playlistFrame.getPlayList().addFile(songPath);
            playlistFrame.refresh();
        });

        System.out.println("Starting playback of the song from playlist...");
        SwingUtilities.invokeAndWait(finalMain::play);

        Thread.sleep(2000); // Let it play for 2 seconds

        // Get cmsMenu and cmsOpenOtherPanel using reflection
        Field cmsMenuField = FormMain.class.getDeclaredField("cmsMenu");
        cmsMenuField.setAccessible(true);
        JPopupMenu cmsMenu = (JPopupMenu) cmsMenuField.get(finalMain);

        Field cmsOpenOtherPanelField = FormMain.class.getDeclaredField("cmsOpenOtherPanel");
        cmsOpenOtherPanelField.setAccessible(true);
        JPopupMenu cmsOpenOtherPanel = (JPopupMenu) cmsOpenOtherPanelField.get(finalMain);

        List<JMenuItem> items = new ArrayList<>();
        getMenuItems(cmsMenu, items);
        getMenuItems(cmsOpenOtherPanel, items);

        System.out.println("Found " + items.size() + " menu items to execute.");

        for (JMenuItem item : items) {
            String name = item.getName();
            if (name == null || IGNORED_NAMES.contains(name)) {
                continue;
            }

            System.out.println("Executing menu item: " + name + " (" + item.getText() + ")");
            try {
                SwingUtilities.invokeAndWait(item::doClick);
                Thread.sleep(200);
            } catch (Exception e) {
                System.err.println("Failed on menu item: " + name);
                throw e;
            } finally {
                cleanupExtraWindows(finalMain, playlistFrame);
                Thread.sleep(50);
            }
        }

        System.out.println("All form execution tests completed successfully!");
        SwingUtilities.invokeAndWait(finalMain::dispose);
        Thread.sleep(1000);
    }
}
