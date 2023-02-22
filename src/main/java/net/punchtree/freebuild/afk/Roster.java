package net.punchtree.freebuild.afk;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class Roster {
    private final Set<UUID> roster;
    private final Consumer<UUID> onAdd;
    private final Consumer<UUID> onRemove;

    public Roster(Set<UUID> roster, Consumer<UUID> onAdd, Consumer<UUID> onRemove) {
        this.roster = roster;
        this.onAdd = onAdd;
        this.onRemove = onRemove;
    }

    public Set<UUID> getRoster() {
        return roster;
    }

    public void addPlayer(UUID player) {
        if(roster.add(player)) {
            onAdd.accept(player);
        }
    }

    public void removePlayer(UUID player) {
        if(roster.remove(player)) {
            onRemove.accept(player);
        }
    }

    public boolean containsPlayer(UUID player) {
        return roster.contains(player);
    }

}
