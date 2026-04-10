package mdplayer.properties;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.Icon;


/**
 */
public class Resources {

    private static final ResourceBundle resourceMan = ResourceBundle.getBundle("mdplayer/properties/resources", Locale.getDefault());

    private static Locale resourceCulture;

    //@System.Diagnostics.CodeAnalysis.SuppressMessageAttribute("Microsoft.Performance", "CA1811:AvoidUncalledPrivateCode")]
    private Resources() {
    }

    /**
     */
    public static ResourceBundle getResourceManager() {
        return resourceMan;
    }

    /**
     */
    public static Locale getCulture() {

        return resourceCulture;
    }

    void setCulture(Locale value) {
        resourceCulture = value;
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getImage(String name) {
        try {
            return ImageIO.read(Resources.class.getResourceAsStream("/mdplayer/resources/" + name + ".png"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getAddFolderPL() {

        return getImage("addFolderPL");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getAddPL() {

        return getImage("addPL");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcFadeout() {

        return getImage("ccFadeout");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcFast() {

        return getImage("ccFast");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcInformation() {

        return getImage("ccInformation");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcKBD() {

        return getImage("ccKBD");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcLoop() {

        return getImage("ccLoop");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcLoopOne() {

        return getImage("ccLoopOne");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcMIDIKBD() {

        return getImage("ccMIDIKBD");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcMixer() {

        return getImage("ccMixer");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcNext() {

        return getImage("ccNext");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcOpenFolder() {

        return getImage("ccOpenFolder");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcPause() {

        return getImage("ccPause");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcPlay() {

        return getImage("ccPlay");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcPlayList() {

        return getImage("ccPlayList");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcPrevious() {

        return getImage("ccPrevious");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcRandom() {

        return getImage("ccRandom");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcSetting() {

        return getImage("ccSetting");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcSlow() {

        return getImage("ccSlow");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcStep() {

        return getImage("ccStep");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcStop() {

        return getImage("ccStop");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcVST() {

        return getImage("ccVST");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCcZoom() {

        return getImage("ccZoom");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChFadeout() {

        return getImage("chFadeout");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChFast() {

        return getImage("chFast");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChInformation() {

        return getImage("chInformation");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChKBD() {

        return getImage("chKBD");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChLoop() {

        return getImage("chLoop");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChLoopOne() {

        return getImage("chLoopOne");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChMIDIKBD() {
        return getImage("chMIDIKBD");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChMixer() {
        return getImage("chMixer");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChNext() {
        return getImage("chNext");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChOpenFolder() {
        return getImage("chOpenFolder");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChPause() {
        return getImage("chPause");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChPlay() {
        return getImage("chPlay");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChPlayList() {
        return getImage("chPlayList");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChPrevious() {
        return getImage("chPrevious");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChRandom() {
        return getImage("chRandom");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChSetting() {
        return getImage("chSetting");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChSlow() {

        return getImage("chSlow");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChStep() {

        return getImage("chStep");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChStop() {

        return getImage("chStop");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChVST() {

        return getImage("chVST");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getChZoom() {

        return getImage("chZoom");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiFadeout() {

        return getImage("ciFadeout");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiFast() {

        return getImage("ciFast");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiInformation() {

        return getImage("ciInformation");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiKBD() {

        return getImage("ciKBD");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiLoop() {

        return getImage("ciLoop");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiLoopOne() {

        return getImage("ciLoopOne");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiMIDIKBD() {

        return getImage("ciMIDIKBD");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiMixer() {

        return getImage("ciMixer");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiNext() {

        return getImage("ciNext");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiOpenFolder() {

        return getImage("ciOpenFolder");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiPause() {

        return getImage("ciPause");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiPlay() {

        return getImage("ciPlay");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiPlayList() {

        return getImage("ciPlayList");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiPrevious() {

        return getImage("ciPrevious");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiRandom() {

        return getImage("ciRandom");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiSetting() {

        return getImage("ciSetting");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiSlow() {

        return getImage("ciSlow");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiStep() {

        return getImage("ciStep");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiStop() {

        return getImage("ciStop");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiVST() {

        return getImage("ciVST");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getCiZoom() {

        return getImage("ciZoom");
    }

    /**
     * MDPlayer
     * Player for VGM files (a performance tool that emulates the Mega Drive sound chip, etc.)
     * <p>
     * [Summary]
     * This tool plays VGM files while displaying a keyboard.
     * (Compatible with NRD, XGM, S98, MID, RCP, NSF, HES, Sid, MDR, MDX, MND, MUC(TBD), MUB(TBD) files.)
     * <p>
     * [Caution]
     * - Please be careful of the volume when playing. There may be cases where noise caused by a bug is played at a loud volume.
     * (Especially when trying a file that has never been played before or when updating the program.)
     * <p>
     * - If you find any problems while using the tool, please contact us on Twitter (@kumakumakumaT_T).
     * (Please do not contact the creators of VGMPlay, NRTDRV, and other great software
     * about MDPlayer directly.)
     * We will do our best to accommodate your requests, but there are many cases where we cannot meet your requests. Thank you for your understanding.
     * <p>
     * [The remaining string has been truncated]&quot;; Finds a localized string similar to ←.
     */
    public static String getCntDescription() {

        return resourceMan.getString("cntDescription");
    }

    /**
     * Exception occurred:\n- Type ------\n%d\n- Message ------\n%d\n- Source ------\n%d\n- StackTrace ------\n%d\n Finds a localized string similar to ←.
     */
    public static String getCntExceptionFormat() {

        return resourceMan.getString("cntExceptionFormat");
    }

    /**
     * Internal Error:\n- Type ------\n%d\n- Message ------\n%d\n- Source ------\n%d\n- StackTrace ------\n%d\n Finds a localized string similar to ←.
     */
    public static String getCntInnerExceptionFormat() {

        return resourceMan.getString("cntInnerExceptionFormat");
    }

    /**
     * Log.txt Finds a localized string similar to ←.
     */
    public static String getCntLogFilename() {

        return resourceMan.getString("cntLogFilename");
    }

    /**
     * Setting.xml Finds a localized string similar to ←.
     */
    public static String getCntSettingFileName() {

        return resourceMan.getString("cntSettingFileName");
    }

    /**
     * VGM file(*.Vgm;*.vgz)|*.Vgm;*.vgz|
     * XGM file(*.Xgm)|*.Xgm|
     * ZGM file(*.Zgm)|*.Zgm|
     * HES file(*.Hes)|*.Hes|
     * MDR file(*.mdr)|*.mdr|
     * MDX file(*.mdx)|*.mdx|
     * MGS file(*.mgs)|*.mgs|
     * MND file(*.mnd)|*.mnd|
     * MUCOM88 file(*.mub;*.muc)|*.mub;*.muc|
     * NRD file(*.nrd)|*.nrd|
     * NSF file(*.Nsf)|*.Nsf|
     * PMD file(*.m;*.m2;*.mz;*.mml)|*.m;*.m2;*.mz;*.mml|
     * RCP file(*.rcp)|*.rcp|
     * S98 file(*.s98)|*.s98|
     * SID file(*.Sid)|*.Sid|
     * StandardMIDI file(*.mid)|*.mid|
     * WAV file(*.wav)|*.wav|
     * MP3 file(*.mp3)|*.mp3|
     * AIFF file(*.aiff)|*.aiff|
     * M3U file [The remaining string has been truncated]&quot;; Finds a localized string similar to ←.
     */
    public static String getCntSupportFile() {

        return resourceMan.getString("cntSupportFile");
    }

    /**
     * yyyy/MM/dd HH:mm:ss     Finds a localized string similar to ←.
     */
    public static String getCntTimeFormat() {

        return resourceMan.getString("cntTimeFormat");
    }

    /**
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;
     * &lt;Balance xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:xsd=&quot;http://www.w3.org/2001/XMLSchema&quot;&gt;
     * &lt;MasterVolume&gt;0&lt;/MasterVolume&gt;
     * &lt;YM2612Volume&gt;-192&lt;/YM2612Volume&gt;
     * &lt;SN76489Volume&gt;-192&lt;/SN76489Volume&gt;
     * &lt;RF5C68Volume&gt;-192&lt;/RF5C68Volume&gt;
     * &lt;RF5C164Volume&gt;-192&lt;/RF5C164Volume&gt;
     * &lt;PWMVolume&gt;-192&lt;/PWMVolume&gt;
     * &lt;C140Volume&gt;-192&lt;/C140Volume&gt;
     * &lt;OKIM6258Volume&gt;-192&lt;/OKIM6258Volume&gt;
     * &lt;OKIM6295Volume&gt;-192&lt;/OKIM6295Volume&gt;
     * &lt;SEGAPCMVolume&gt;-192&lt;/SEGAPCM [The remaining string has been truncated]&quot;; Finds a localized string similar to ←.
     */
    public static String getDefaultVolumeBalance_HES() {

        return resourceMan.getString("DefaultVolumeBalance_HES");
    }

    /**
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;
     * &lt;Balance xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:xsd=&quot;http://www.w3.org/2001/XMLSchema&quot;&gt;
     * &lt;MasterVolume&gt;0&lt;/MasterVolume&gt;
     * &lt;YM2612Volume&gt;-192&lt;/YM2612Volume&gt;
     * &lt;SN76489Volume&gt;-192&lt;/SN76489Volume&gt;
     * &lt;RF5C68Volume&gt;-192&lt;/RF5C68Volume&gt;
     * &lt;RF5C164Volume&gt;-192&lt;/RF5C164Volume&gt;
     * &lt;PWMVolume&gt;-192&lt;/PWMVolume&gt;
     * &lt;C140Volume&gt;-192&lt;/C140Volume&gt;
     * &lt;OKIM6258Volume&gt;-192&lt;/OKIM6258Volume&gt;
     * &lt;OKIM6295Volume&gt;-192&lt;/OKIM6295Volume&gt;
     * &lt;SEGAPCMVolume&gt;-192&lt;/SEGAPCM [The remaining string has been truncated]&quot;; Finds a localized string similar to ←.
     */
    public static String getDefaultVolumeBalance_MDR() {

        return resourceMan.getString("DefaultVolumeBalance_MDR");
    }

    /**
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;
     * &lt;Balance xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:xsd=&quot;http://www.w3.org/2001/XMLSchema&quot;&gt;
     * &lt;MasterVolume&gt;0&lt;/MasterVolume&gt;
     * &lt;YM2612Volume&gt;-192&lt;/YM2612Volume&gt;
     * &lt;SN76489Volume&gt;-192&lt;/SN76489Volume&gt;
     * &lt;RF5C68Volume&gt;-192&lt;/RF5C68Volume&gt;
     * &lt;RF5C164Volume&gt;-192&lt;/RF5C164Volume&gt;
     * &lt;PWMVolume&gt;-192&lt;/PWMVolume&gt;
     * &lt;C140Volume&gt;-192&lt;/C140Volume&gt;
     * &lt;OKIM6258Volume&gt;0&lt;/OKIM6258Volume&gt;
     * &lt;OKIM6295Volume&gt;-192&lt;/OKIM6295Volume&gt;
     * &lt;SEGAPCMVolume&gt;-192&lt;/SEGAPCMVol [The remaining string has been truncated]&quot;; Finds a localized string similar to ←.
     */
    public static String getDefaultVolumeBalance_MDX() {

        return resourceMan.getString("DefaultVolumeBalance_MDX");
    }

    /**
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;
     * &lt;Balance xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:xsd=&quot;http://www.w3.org/2001/XMLSchema&quot;&gt;
     * &lt;MasterVolume&gt;0&lt;/MasterVolume&gt;
     * &lt;YM2612Volume&gt;-192&lt;/YM2612Volume&gt;
     * &lt;SN76489Volume&gt;-192&lt;/SN76489Volume&gt;
     * &lt;RF5C68Volume&gt;-192&lt;/RF5C68Volume&gt;
     * &lt;RF5C164Volume&gt;-192&lt;/RF5C164Volume&gt;
     * &lt;PWMVolume&gt;-192&lt;/PWMVolume&gt;
     * &lt;C140Volume&gt;-192&lt;/C140Volume&gt;
     * &lt;OKIM6258Volume&gt;0&lt;/OKIM6258Volume&gt;
     * &lt;OKIM6295Volume&gt;-192&lt;/OKIM6295Volume&gt;
     * &lt;SEGAPCMVolume&gt;-192&lt;/SEGAPCMVol [The remaining string has been truncated]&quot;; Finds a localized string similar to ←.
     */
    public static String getDefaultVolumeBalance_MND() {

        return resourceMan.getString("DefaultVolumeBalance_MND");
    }

    /**
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;
     * &lt;Balance xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:xsd=&quot;http://www.w3.org/2001/XMLSchema&quot;&gt;
     * &lt;MasterVolume&gt;0&lt;/MasterVolume&gt;
     * &lt;YM2612Volume&gt;-192&lt;/YM2612Volume&gt;
     * &lt;SN76489Volume&gt;-192&lt;/SN76489Volume&gt;
     * &lt;RF5C68Volume&gt;-192&lt;/RF5C68Volume&gt;
     * &lt;RF5C164Volume&gt;-192&lt;/RF5C164Volume&gt;
     * &lt;PWMVolume&gt;-192&lt;/PWMVolume&gt;
     * &lt;C140Volume&gt;-192&lt;/C140Volume&gt;
     * &lt;OKIM6258Volume&gt;-192&lt;/OKIM6258Volume&gt;
     * &lt;OKIM6295Volume&gt;-192&lt;/OKIM6295Volume&gt;
     * &lt;SEGAPCMVolume&gt;-192&lt;/SEGAPCM [The remaining string has been truncated]&quot;; Finds a localized string similar to ←.
     */
    public static String getDefaultVolumeBalance_MUB() {

        return resourceMan.getString("DefaultVolumeBalance_MUB");
    }

    /**
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;
     * &lt;Balance xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:xsd=&quot;http://www.w3.org/2001/XMLSchema&quot;&gt;
     * &lt;MasterVolume&gt;0&lt;/MasterVolume&gt;
     * &lt;YM2612Volume&gt;-192&lt;/YM2612Volume&gt;
     * &lt;SN76489Volume&gt;-192&lt;/SN76489Volume&gt;
     * &lt;RF5C68Volume&gt;-192&lt;/RF5C68Volume&gt;
     * &lt;RF5C164Volume&gt;-192&lt;/RF5C164Volume&gt;
     * &lt;PWMVolume&gt;-192&lt;/PWMVolume&gt;
     * &lt;C140Volume&gt;-192&lt;/C140Volume&gt;
     * &lt;OKIM6258Volume&gt;-192&lt;/OKIM6258Volume&gt;
     * &lt;OKIM6295Volume&gt;-192&lt;/OKIM6295Volume&gt;
     * &lt;SEGAPCMVolume&gt;-192&lt;/SEGAPCM [The remaining string has been truncated]&quot;; Finds a localized string similar to ←.
     */
    public static String getDefaultVolumeBalance_MUC() {

        return resourceMan.getString("DefaultVolumeBalance_MUC");
    }

    /**
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;
     * &lt;Balance xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:xsd=&quot;http://www.w3.org/2001/XMLSchema&quot;&gt;
     * &lt;MasterVolume&gt;0&lt;/MasterVolume&gt;
     * &lt;YM2612Volume&gt;-192&lt;/YM2612Volume&gt;
     * &lt;SN76489Volume&gt;-192&lt;/SN76489Volume&gt;
     * &lt;RF5C68Volume&gt;-192&lt;/RF5C68Volume&gt;
     * &lt;RF5C164Volume&gt;-192&lt;/RF5C164Volume&gt;
     * &lt;PWMVolume&gt;-192&lt;/PWMVolume&gt;
     * &lt;C140Volume&gt;-192&lt;/C140Volume&gt;
     * &lt;OKIM6258Volume&gt;-192&lt;/OKIM6258Volume&gt;
     * &lt;OKIM6295Volume&gt;-192&lt;/OKIM6295Volume&gt;
     * &lt;SEGAPCMVolume&gt;-192&lt;/SEGAPCM [The remaining string has been truncated]&quot;; Finds a localized string similar to ←.
     */
    public static String getDefaultVolumeBalance_NRT() {

        return resourceMan.getString("DefaultVolumeBalance_NRT");
    }

    /**
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;
     * &lt;Balance xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:xsd=&quot;http://www.w3.org/2001/XMLSchema&quot;&gt;
     * &lt;MasterVolume&gt;0&lt;/MasterVolume&gt;
     * &lt;YM2612Volume&gt;-192&lt;/YM2612Volume&gt;
     * &lt;SN76489Volume&gt;-192&lt;/SN76489Volume&gt;
     * &lt;RF5C68Volume&gt;-192&lt;/RF5C68Volume&gt;
     * &lt;RF5C164Volume&gt;-192&lt;/RF5C164Volume&gt;
     * &lt;PWMVolume&gt;-192&lt;/PWMVolume&gt;
     * &lt;C140Volume&gt;-192&lt;/C140Volume&gt;
     * &lt;OKIM6258Volume&gt;-192&lt;/OKIM6258Volume&gt;
     * &lt;OKIM6295Volume&gt;-192&lt;/OKIM6295Volume&gt;
     * &lt;SEGAPCMVolume&gt;-192&lt;/SEGAPCM [The remaining string has been truncated]&quot;; Finds a localized string similar to ←.
     */
    public static String getDefaultVolumeBalance_NSF() {

        return resourceMan.getString("DefaultVolumeBalance_NSF");
    }

    /**
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;
     * &lt;Balance xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:xsd=&quot;http://www.w3.org/2001/XMLSchema&quot;&gt;
     * &lt;MasterVolume&gt;0&lt;/MasterVolume&gt;
     * &lt;YM2612Volume&gt;0&lt;/YM2612Volume&gt;
     * &lt;SN76489Volume&gt;-192&lt;/SN76489Volume&gt;
     * &lt;RF5C68Volume&gt;-192&lt;/RF5C68Volume&gt;
     * &lt;RF5C164Volume&gt;-192&lt;/RF5C164Volume&gt;
     * &lt;PWMVolume&gt;-192&lt;/PWMVolume&gt;
     * &lt;C140Volume&gt;-192&lt;/C140Volume&gt;
     * &lt;OKIM6258Volume&gt;-192&lt;/OKIM6258Volume&gt;
     * &lt;OKIM6295Volume&gt;-192&lt;/OKIM6295Volume&gt;
     * &lt;SEGAPCMVolume&gt;-192&lt;/SEGAPCMVol [The remaining string has been truncated]&quot;; Finds a localized string similar to ←.
     */
    public static String getDefaultVolumeBalance_S98() {

        return resourceMan.getString("DefaultVolumeBalance_S98");
    }

    /**
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;
     * &lt;Balance xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:xsd=&quot;http://www.w3.org/2001/XMLSchema&quot;&gt;
     * &lt;MasterVolume&gt;0&lt;/MasterVolume&gt;
     * &lt;YM2612Volume&gt;-192&lt;/YM2612Volume&gt;
     * &lt;SN76489Volume&gt;-192&lt;/SN76489Volume&gt;
     * &lt;RF5C68Volume&gt;-192&lt;/RF5C68Volume&gt;
     * &lt;RF5C164Volume&gt;-192&lt;/RF5C164Volume&gt;
     * &lt;PWMVolume&gt;-192&lt;/PWMVolume&gt;
     * &lt;C140Volume&gt;-192&lt;/C140Volume&gt;
     * &lt;OKIM6258Volume&gt;-192&lt;/OKIM6258Volume&gt;
     * &lt;OKIM6295Volume&gt;-192&lt;/OKIM6295Volume&gt;
     * &lt;SEGAPCMVolume&gt;-192&lt;/SEGAPCM [The remaining string has been truncated]&quot;; Finds a localized string similar to ←.
     */
    public static String getDefaultVolumeBalance_SID() {

        return resourceMan.getString("DefaultVolumeBalance_SID");
    }

    /**
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;
     * &lt;Balance xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:xsd=&quot;http://www.w3.org/2001/XMLSchema&quot;&gt;
     * &lt;MasterVolume&gt;0&lt;/MasterVolume&gt;
     * &lt;YM2612Volume&gt;0&lt;/YM2612Volume&gt;
     * &lt;SN76489Volume&gt;0&lt;/SN76489Volume&gt;
     * &lt;RF5C68Volume&gt;0&lt;/RF5C68Volume&gt;
     * &lt;RF5C164Volume&gt;0&lt;/RF5C164Volume&gt;
     * &lt;PWMVolume&gt;0&lt;/PWMVolume&gt;
     * &lt;C140Volume&gt;0&lt;/C140Volume&gt;
     * &lt;OKIM6258Volume&gt;0&lt;/OKIM6258Volume&gt;
     * &lt;OKIM6295Volume&gt;0&lt;/OKIM6295Volume&gt;
     * &lt;SEGAPCMVolume&gt;0&lt;/SEGAPCMVolume&gt;
     * &lt;AY8910Volume&gt;0&lt; [The remaining string has been truncated]&quot;; Finds a localized string similar to ←.
     */
    public static String getDefaultVolumeBalance_VGM() {

        return resourceMan.getString("DefaultVolumeBalance_VGM");
    }

    /**
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;
     * &lt;Balance xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:xsd=&quot;http://www.w3.org/2001/XMLSchema&quot;&gt;
     * &lt;MasterVolume&gt;0&lt;/MasterVolume&gt;
     * &lt;YM2612Volume&gt;0&lt;/YM2612Volume&gt;
     * &lt;SN76489Volume&gt;-15&lt;/SN76489Volume&gt;
     * &lt;RF5C68Volume&gt;-192&lt;/RF5C68Volume&gt;
     * &lt;RF5C164Volume&gt;-192&lt;/RF5C164Volume&gt;
     * &lt;PWMVolume&gt;-192&lt;/PWMVolume&gt;
     * &lt;C140Volume&gt;-192&lt;/C140Volume&gt;
     * &lt;OKIM6258Volume&gt;-192&lt;/OKIM6258Volume&gt;
     * &lt;OKIM6295Volume&gt;-192&lt;/OKIM6295Volume&gt;
     * &lt;SEGAPCMVolume&gt;-192&lt;/SEGAPCMVolu [The remaining string has been truncated]&quot;; Finds a localized string similar to ←.
     */
    public static String getDefaultVolumeBalance_XGM() {

        return resourceMan.getString("DefaultVolumeBalance_XGM");
    }

    /**
     * &lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;
     * &lt;Balance xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:xsd=&quot;http://www.w3.org/2001/XMLSchema&quot;&gt;
     * &lt;MasterVolume&gt;0&lt;/MasterVolume&gt;
     * &lt;YM2612Volume&gt;0&lt;/YM2612Volume&gt;
     * &lt;SN76489Volume&gt;-15&lt;/SN76489Volume&gt;
     * &lt;RF5C68Volume&gt;-192&lt;/RF5C68Volume&gt;
     * &lt;RF5C164Volume&gt;-192&lt;/RF5C164Volume&gt;
     * &lt;PWMVolume&gt;-192&lt;/PWMVolume&gt;
     * &lt;C140Volume&gt;-192&lt;/C140Volume&gt;
     * &lt;OKIM6258Volume&gt;-192&lt;/OKIM6258Volume&gt;
     * &lt;OKIM6295Volume&gt;-192&lt;/OKIM6295Volume&gt;
     * &lt;SEGAPCMVolume&gt;-192&lt;/SEGAPCMVolu [The remaining string has been truncated]&quot;; Finds a localized string similar to ←.
     */
    public static String getDefaultVolumeBalance_ZGM() {

        return resourceMan.getString("DefaultVolumeBalance_ZGM");
    }

    public static String getDefaultVolumeBalance_GBS() {

        return resourceMan.getString("DefaultVolumeBalance_GBS");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getDownPL() {

        return getImage("downPL");
    }

    /**
     */
    public static Icon getFeli128() {

        Object obj = resourceMan.getObject("Feli128");
        return ((Icon) (obj));
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getFeliAndMD2() {

        return getImage("FeliAndMD2");
    }

    /**
     */
    public static Icon getFeliTop() {

        Object obj = resourceMan.getObject("FeliTop");
        return ((Icon) (obj));
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getImgPL() {

        return getImage("imgPL");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getJapPL() {

        return getImage("japPL");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getMmlPL() {

        return getImage("mmlPL");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getOpenPL() {

        return getImage("openPL");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlane() {

        return getImage("plane");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneAY8910() {

        return getImage("planeAY8910");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneB() {

        return getImage("planeB");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneC() {

        return getImage("planeC");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneC352() {

        return getImage("planeC352");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneControl() {

        return getImage("planeControl");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneD() {

        return getImage("planeD");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneDMG() {

        return getImage("planeDMG");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneE() {

        return getImage("planeE");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneF() {

        return getImage("planeF");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneFDS() {

        return getImage("planeFDS");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneHuC6280() {

        return getImage("planeHuC6280");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneK051649() {

        return getImage("planeK051649");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneMIDI_GM() {

        return getImage("planeMIDI_GM");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneMIDI_GS() {

        return getImage("planeMIDI_GS");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneMIDI_XG() {
        return getImage("planeMIDI_XG");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneMixer() {
        return getImage("planeMixer");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneMMC5() {

        return getImage("planeMMC5");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneMSM6258() {

        return getImage("planeMSM6258");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneMSM6295() {

        return getImage("planeMSM6295");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneMultiPCM() {

        return getImage("planeMultiPCM");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneN106() {

        return getImage("planeN106");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneNESDMC() {

        return getImage("planeNESDMC");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlanePPZ8() {

        return getImage("planePPZ8");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneQSound() {

        return getImage("planeQSound");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneS5B() {

        return getImage("planeS5B");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneSEGAPCM() {

        return getImage("planeSEGAPCM");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneSN76489() {

        return getImage("planeSN76489");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneVRC6() {

        return getImage("planeVRC6");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneVRC7() {

        return getImage("planeVRC7");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneY8950() {

        return getImage("planeY8950");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneYM2203() {

        return getImage("planeYM2203");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneYM2413() {

        return getImage("planeYM2413");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneYM2610() {

        return getImage("planeYM2610");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneYM2612() {

        return getImage("planeYM2612");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneYM2612MIDI() {

        return getImage("planeYM2612MIDI");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneYM3526() {

        return getImage("planeYM3526");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneYM3812() {

        return getImage("planeYM3812");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneYMF262() {

        return getImage("planeYMF262");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneYMF271() {

        return getImage("planeYMF271");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneYMF278B() {

        return getImage("planeYMF278B");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getPlaneYMZ280B() {

        return getImage("planeYMZ280B");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRChipName_01() {

        return getImage("rChipName_01");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRChipName_02() {

        return getImage("rChipName_02");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRChipName_03() {

        return getImage("rChipName_03");
    }

    /** */
    public static byte[] getREADME() {

        Object obj = resourceMan.getObject("README");
        return ((byte[]) (obj));
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRFader() {

        return getImage("rFader");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRFont_01() {

        return getImage("rFont_01");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRFont_02() {

        return getImage("rFont_02");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRFont_03() {

        return getImage("rFont_03");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRFont_04() {

        return getImage("rFont_04");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRFont_05() {

        return getImage("rFont_05");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRFont_06() {

        return getImage("rFont_06");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRKakko_00() {

        return getImage("rKakko_00");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRKBD_01() {

        return getImage("rKBD_01");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRKBD_02() {

        return getImage("rKBD_02");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRKBD_03() {

        return getImage("rKBD_03");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMenuButtons_01() {

        return getImage("rMenuButtons_01");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMenuButtons_02() {

        return getImage("rMenuButtons_02");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_01() {

        return getImage("rMIDILCD_01");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_02() {

        return getImage("rMIDILCD_02");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_03() {

        return getImage("rMIDILCD_03");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_Fader_01() {

        return getImage("rMIDILCD_Fader_01");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_Fader_02() {

        return getImage("rMIDILCD_Fader_02");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_Fader_03() {

        return getImage("rMIDILCD_Fader_03");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_Font_01() {

        return getImage("rMIDILCD_Font_01");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_Font_02() {

        return getImage("rMIDILCD_Font_02");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_Font_03() {

        return getImage("rMIDILCD_Font_03");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_Font_04() {

        return getImage("rMIDILCD_Font_04");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_Font_05() {

        return getImage("rMIDILCD_Font_05");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_Font_06() {

        return getImage("rMIDILCD_Font_06");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_KBD_01() {

        return getImage("rMIDILCD_KBD_01");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_Vol_01() {

        return getImage("rMIDILCD_Vol_01");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_Vol_02() {

        return getImage("rMIDILCD_Vol_02");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRMIDILCD_Vol_03() {

        return getImage("rMIDILCD_Vol_03");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRNESDMC() {

        return getImage("rNESDMC");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRPan_01() {

        return getImage("rPan_01");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRPan_02() {

        return getImage("rPan_02");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRPan_03() {

        return getImage("rPan_03");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRPan2_01() {

        return getImage("rPan2_01");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRPan2_02() {

        return getImage("rPan2_02");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRPSGEnv() {

        return getImage("rPSGEnv");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRPSGMode_01() {

        return getImage("rPSGMode_01");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRPSGMode_02() {

        return getImage("rPSGMode_02");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRPSGMode_03() {

        return getImage("rPSGMode_03");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRPSGMode_04() {

        return getImage("rPSGMode_04");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRPSGMode_05() {

        return getImage("rPSGMode_05");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRPSGMode_06() {

        return getImage("rPSGMode_06");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRType_01() {

        return getImage("rType_01");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRType_02() {

        return getImage("rType_02");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRType_03() {

        return getImage("rType_03");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRType_04() {

        return getImage("rType_04");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRType_05() {

        return getImage("rType_05");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRType_06() {

        return getImage("rType_06");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRType_YMF271() {

        return getImage("rType_YMF271");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRVol_01() {

        return getImage("rVol_01");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRVol_02() {

        return getImage("rVol_02");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRVol_03() {

        return getImage("rVol_03");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRWavGraph() {

        return getImage("rWavGraph");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getRWavGraph2() {

        return getImage("rWavGraph2");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getSavePL() {

        return getImage("savePL");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getTxtPL() {

        return getImage("txtPL");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getUpPL() {

        return getImage("upPL");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getVHeight1() {

        return getImage("vHeight1");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getVHeight2() {

        return getImage("vHeight2");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getVHeight3() {

        return getImage("vHeight3");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getVType1() {

        return getImage("vType1");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getVType2() {

        return getImage("vType2");
    }

    /**
     * Looks up localized resources of type BufferedImage.
     */
    public static BufferedImage getVType3() {

        return getImage("vType3");
    }
}
