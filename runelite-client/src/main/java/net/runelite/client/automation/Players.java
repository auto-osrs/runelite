package net.runelite.client.automation;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;

import javax.annotation.Nullable;

public class Players {

    @Inject
    private static Client client;

    /**
     * Gets the logged in character.
     *
     * @return The Player object of the current character.
     */
    @Nullable
    public static Player getLocal() {
        return client.getLocalPlayer();
    }

}
