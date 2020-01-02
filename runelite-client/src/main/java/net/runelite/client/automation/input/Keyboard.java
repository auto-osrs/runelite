package net.runelite.client.automation.input;

import com.google.inject.Inject;
import java.awt.event.KeyEvent;
import net.runelite.api.Client;

public class Keyboard {

    @Inject
    private static Client client;

    /**
     * Presses the specified key.
     *
     * @param key The key to be pressed.
     */
    public static void pressKey(KeyboardKey key) {
        KeyEvent event = createKeyEvent(key, KeyEvent.KEY_PRESSED);
        client.getCanvas().dispatchEvent(event);
    }

    /**
     * Releases the specified key.
     *
     * @param key The key to be released.
     */
    public static void releaseKey(KeyboardKey key) {
        KeyEvent event = createKeyEvent(key, KeyEvent.KEY_RELEASED);
        client.getCanvas().dispatchEvent(event);
    }

    /**
     * Creates our KeyEvent that will be dispatched to the client.
     *
     * @param key The key to be used for the event.
     * @param keyEvent The type of KeyEvent that should be used.
     * @return The created KeyEvent that can be dispatched to the client.
     */
    private static KeyEvent createKeyEvent(KeyboardKey key, int keyEvent) {
        return new KeyEvent(client.getCanvas(), keyEvent, System.currentTimeMillis(), 0, key.getKeyCode());
    }
}
