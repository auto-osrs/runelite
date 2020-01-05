package net.runelite.client.automation.query;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.LocatableQueryResults;
import net.runelite.api.queries.GameObjectQuery;

public class GameObjectQueryBuilder extends GameObjectQuery {

    private Client client;

    public GameObjectQueryBuilder(Client client) {
        this.client = client;
    }

    public LocatableQueryResults<GameObject> results() {
        return result(client);
    }

    /**
     * Filters the query results by their available actions.
     *
     * @param actions The array of actions to be filtered.
     * @return The GameObjectQueryBuilder object to allow chaining.
     */
    public GameObjectQueryBuilder actions(String ... actions) {
        predicate = and(gameObject -> {
            String[] gameObjectActions = client.getObjectDefinition(gameObject.getId()).getActions();

            for (String action : actions) {
                for (String gameObjectAction : gameObjectActions) {
                    if (gameObjectAction != null && gameObjectAction.equals(action)) {
                        return true;
                    }
                }
            }
            return false;
        });

        return this;
    }

    /**
     * Filters the query results by their name.
     *
     * @param names The array of names to be matched.
     * @return The GameObjectQueryBuilder object to allow chaining.
     */
    public GameObjectQueryBuilder names(String ... names) {
        predicate = and(gameObject -> {
            String gameObjectName = client.getObjectDefinition(gameObject.getId()).getName();

            for (String name : names) {
                if (name.equals(gameObjectName)) {
                    return true;
                }
            }
            return false;
        });

        return this;
    }

}
