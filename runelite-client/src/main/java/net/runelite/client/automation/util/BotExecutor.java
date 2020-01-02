package net.runelite.client.automation.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BotExecutor {

    private static ExecutorService executor = null;

    public static ExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }

        return executor;
    }
}
