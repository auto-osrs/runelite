package net.runelite.client.automation;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.automation.query.GameObjectQueryBuilder;

public class GameObjects {

    @Inject
    private static Client client;

    public static GameObjectQueryBuilder query() {
        return new GameObjectQueryBuilder(client);
    }

}
