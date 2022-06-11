package mdplayer;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;


/**
 * キーボードの操作をフックし、任意のメソッドを挿入する。
 *
 * @see "https://github.com/kwhat/jnativehook"
 */
public class KeyboardHook {

    static class GlobalKeyListenerExample implements NativeKeyListener {
        public void nativeKeyPressed(NativeKeyEvent e) {
            System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

            if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
                try {
                    GlobalScreen.unregisterNativeHook();
                } catch (NativeHookException nativeHookException) {
                    nativeHookException.printStackTrace();
                }
            }
        }

        public void nativeKeyReleased(NativeKeyEvent e) {
            System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        }

        public void nativeKeyTyped(NativeKeyEvent e) {
            System.out.println("Key Typed: " + e.getKeyText(e.getKeyCode()));
        }
    }

    /**
     * キーボードが操作されたときに発生する。
     */
    public void addKeyboardHooked(NativeKeyListener handler) {
        GlobalScreen.addNativeKeyListener(handler);
    }

    void removeKeyboardHooked(NativeKeyListener handler) {
        GlobalScreen.addNativeKeyListener(handler);
    }

    /**
     * 新しいインスタンスを作成する。
     */
    public KeyboardHook() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            throw new IllegalStateException("There was a problem registering the native hook.");
        }
    }

    /**
     * キーボードが操作されたときに実行するデリゲートを指定してインスタンスを作成する。
     *
     * @param handler キーボードが操作されたときに実行するメソッドを表すイベントハンドラ。
     */
    public KeyboardHook(NativeKeyListener handler) {
        this();
        GlobalScreen.addNativeKeyListener(handler);
    }
}

