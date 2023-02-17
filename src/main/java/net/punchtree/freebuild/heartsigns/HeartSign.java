package net.punchtree.freebuild.heartsigns;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class HeartSign {
    private static final Component HEART_SIGN_LINE_0 = Component.text("[Heart My Build]", NamedTextColor.WHITE);
    private static final Component HEART_SIGN_LINE_1 = Component.text("❤", NamedTextColor.DARK_RED);
    private static final Component HEART_SIGN_LINE_3 = Component.text("[R-Click To Heart]", NamedTextColor.WHITE);
    private static final NamespacedKey OWNER_KEY = new NamespacedKey(PunchTreeFreebuildPlugin.getInstance(), "heartsign-owner");
    private static final NamespacedKey HEARTERS_KEY = new NamespacedKey(PunchTreeFreebuildPlugin.getInstance(), "heartsign-hearters");
    private final Sign sign;
    private final UUID owner;
    private final List<UUID> hearters;
    private final PersistentDataContainer signContainer;

    /**
     * Creates a new HeartSign from a Sign that has preexisting data
     * @param sign The Sign to create a HeartSign from
     *             The Sign must be a HeartSign
     *             The Sign must have a valid owner
     *             The Sign will be updated to have a valid list of hearters if it does not already
     */
    public HeartSign(@NotNull Sign sign) {
        this.sign = sign;
        this.signContainer = sign.getPersistentDataContainer();
        this.owner = getOwnerFromContainer(signContainer);

        if(this.owner == null) throw new IllegalArgumentException("Sign does not have a valid owner");

        this.hearters = getHeartersFromContainer(signContainer);
    }

    /**
     * Creates a new HeartSign from a Sign that has no preexisting data
     * @param sign The Sign to create a HeartSign from
     *             The Sign must be a HeartSign
     *             The Sign will be updated to have a valid owner
     *             The Sign will be given the owner as a hearter by default
     *             The Sign will be updated to have a valid list of hearters if it does not already
     * @param owner The owner of the HeartSign
     */
    public HeartSign(@NotNull Sign sign, @NotNull UUID owner) {
        this.sign = sign;
        this.signContainer = sign.getPersistentDataContainer();
        this.hearters = getHeartersFromContainer(signContainer);
        this.owner = owner;
        signContainer.set(OWNER_KEY, PersistentDataType.STRING, owner.toString());
        addHearter(owner);
        sign.line(0, HEART_SIGN_LINE_0);
        sign.line(1, HEART_SIGN_LINE_1);
        sign.line(2, Component.text("x" + hearters.size(), NamedTextColor.WHITE));
        sign.line(3, HEART_SIGN_LINE_3);
        sign.update();
    }

    /**
     * Checks if a Sign is a HeartSign
     * @param sign The Sign to check
     * @return True if the Sign has valid HeartSign owner data in its PersistentDataContainer
     */
    public static boolean isHeartSign(Sign sign) {
        return sign.getPersistentDataContainer().has(OWNER_KEY, PersistentDataType.STRING);
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getHearters() {
        return hearters;
    }

    public void addHearter(UUID hearter) {
        hearters.add(hearter);
        updateHearters();
    }

    public void removeHearter(UUID hearter) {
        hearters.remove(hearter);
        updateHearters();
    }

    private List<UUID> getHeartersFromContainer(PersistentDataContainer container) {
        List<UUID> hearters = new ArrayList<>();
        if(container.has(HEARTERS_KEY, PersistentDataType.STRING)) {
            String heartersString = container.get(HEARTERS_KEY, PersistentDataType.STRING);
            if(heartersString == null || heartersString.isEmpty()) {
                return hearters;
            }
            for(String heartersUUID : heartersString.split(",")) {
                hearters.add(UUID.fromString(heartersUUID));
            }
        }
        return hearters;
    }

    private @Nullable UUID getOwnerFromContainer(PersistentDataContainer container) {
        if(container.has(OWNER_KEY, PersistentDataType.STRING)) {
            try {
                return UUID.fromString(Objects.requireNonNull(container.get(OWNER_KEY, PersistentDataType.STRING)));
            } catch (NullPointerException e) {
                return null;
            }
        }
        return null;
    }

    private Component determineHeartCountForDisplay(List<UUID> hearters) {
        if(hearters.size() < 2) {
            return Component.text("❤", NamedTextColor.DARK_RED);
        }
        if(hearters.size() < 5) {
            return Component.text("❤❤", NamedTextColor.DARK_RED);
        }
        if(hearters.size() < 10) {
            return Component.text("❤❤❤", NamedTextColor.DARK_RED);
        }
        if(hearters.size() < 25) {
            return Component.text("❤❤❤❤", NamedTextColor.DARK_RED);
        }

        return Component.text("❤❤❤❤❤", NamedTextColor.DARK_RED);
    }

    private void updateHearters() {
        signContainer.set(HEARTERS_KEY, PersistentDataType.STRING, String.join(",", hearters.stream().map(UUID::toString).toArray(String[]::new)));
        sign.line(1, determineHeartCountForDisplay(hearters));
        sign.line(2, Component.text("x" + hearters.size(), NamedTextColor.WHITE));
        sign.update();
    }
}
