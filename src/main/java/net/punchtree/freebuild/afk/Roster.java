package net.punchtree.freebuild.afk;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class Roster {
    private final List<UUID> roster;
    private final Consumer<UUID> onAdd;
    private final Consumer<UUID> onRemove;

    public Roster(List<UUID> roster, Consumer<UUID> onAdd, Consumer<UUID> onRemove) {
        this.roster = roster;
        this.onAdd = onAdd;
        this.onRemove = onRemove;
    }

    public List<UUID> getRoster() {
        return roster;
    }

    public void addPlayer(UUID player) {
        boolean success = roster.add(player);
        if(success) {
            onAdd.accept(player);
        }
    }

    public void removePlayer(UUID player) {
        boolean success = roster.remove(player);
        if(success) {
            onRemove.accept(player);
        }
    }

    public boolean containsPlayer(UUID player) {
        return roster.contains(player);
    }

}
