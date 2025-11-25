package nika_nika_fruit_v7tnmscy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Global registry for devil fruit ownership and per-player fruit assignment.
 * This provides the world-unique possession rule per fruit and a single-fruit-per-player rule.
 */
public class DevilFruitRegistry {
    // Fruit keys used across the mod and commands
    public static final String FRUIT_NIKA = "nika";
    public static final String FRUIT_DARK = "dark";
    public static final String FRUIT_QUAKE = "quake";
    public static final String FRUIT_DARKXQUAKE = "darkxquake";
    public static final String FRUIT_SOUL = "soul";
    public static final String FRUIT_DRAGON = "dragon";
    public static final String FRUIT_OPERATION = "operation";

    // Server-wide: which player owns which fruit (only one owner per fruit in the world)
    private static final Map<String, UUID> FRUIT_OWNERS = new HashMap<>();

    // Per-player: which fruit a player currently possesses
    private static final Map<UUID, String> PLAYER_FRUIT = new HashMap<>();

    // Fusion unlock tracking: players who have unlocked DarkxQuake via the fusion ritual
    private static final Set<UUID> DARKXQUAKE_UNLOCKED = new HashSet<>();

    // Track which players, after earning The Mightiest Falls, have eaten Dark and Quake (post-advancement)
    private static final Set<UUID> ATE_DARK_AFTER_ADV = new HashSet<>();
    private static final Set<UUID> ATE_QUAKE_AFTER_ADV = new HashSet<>();

    // Only one player can ever be the DarkxQuake champion (first to complete the ritual)
    private static UUID DARKXQUAKE_CHAMPION = null;

    public static Optional<UUID> getDarkxQuakeChampion() {
        return Optional.ofNullable(DARKXQUAKE_CHAMPION);
    }

    public static boolean hasDarkxQuakeChampion() {
        return DARKXQUAKE_CHAMPION != null;
    }

    public static boolean trySetDarkxQuakeChampion(PlayerEntity player) {
        if (DARKXQUAKE_CHAMPION == null) {
            DARKXQUAKE_CHAMPION = player.getUuid();
            return true;
        }
        return false;
    }

    // Awakening unlock specifically for DarkxQuake (alternate condition)
    private static final Set<UUID> DARKXQUAKE_AWAKENING_UNLOCKED = new HashSet<>();

    public static boolean isDarkxQuakeAwakeningUnlocked(PlayerEntity player) {
        return DARKXQUAKE_AWAKENING_UNLOCKED.contains(player.getUuid());
    }

    public static void unlockDarkxQuakeAwakening(PlayerEntity player) {
        DARKXQUAKE_AWAKENING_UNLOCKED.add(player.getUuid());
        if (player instanceof ServerPlayerEntity sp) {
            sp.sendMessage(Text.literal("§8§lDarkxQuake Awakening Condition Fulfilled!"), false);
        }
    }

    public static boolean isFruitOwned(String fruitId) {
        return FRUIT_OWNERS.containsKey(fruitId);
    }

    public static Optional<UUID> getFruitOwner(String fruitId) {
        return Optional.ofNullable(FRUIT_OWNERS.get(fruitId));
    }

    public static boolean playerOwnsFruit(PlayerEntity player, String fruitId) {
        return getFruitOwner(fruitId).map(id -> id.equals(player.getUuid())).orElse(false);
    }

    public static String getPlayerFruit(PlayerEntity player) {
        return PLAYER_FRUIT.get(player.getUuid());
    }

    public static boolean hasAnyFruit(PlayerEntity player) {
        return getPlayerFruit(player) != null;
    }

    public static boolean canAcquireFruit(PlayerEntity player, String fruitId) {
        // Only one fruit per player at a time
        if (hasAnyFruit(player)) return false;
        // If fruit is already owned by someone else, cannot acquire
        return !isFruitOwned(fruitId) || playerOwnsFruit(player, fruitId);
    }

    public static void assignFruit(PlayerEntity player, String fruitId) {
        // Track post-advancement consumption flags
        if (FRUIT_DARK.equals(fruitId) && isDarkxQuakeUnlocked(player)) {
            ATE_DARK_AFTER_ADV.add(player.getUuid());
        } else if (FRUIT_QUAKE.equals(fruitId) && isDarkxQuakeUnlocked(player)) {
            ATE_QUAKE_AFTER_ADV.add(player.getUuid());
        }

        // Clear previous (should not happen if canAcquireFruit used)
        clearFruit(player);
        FRUIT_OWNERS.put(fruitId, player.getUuid());
        PLAYER_FRUIT.put(player.getUuid(), fruitId);
    }

    public static boolean hasEatenDarkAndQuakePostAdv(PlayerEntity player) {
        return ATE_DARK_AFTER_ADV.contains(player.getUuid()) && ATE_QUAKE_AFTER_ADV.contains(player.getUuid());
    }

    public static void clearPostAdvEatenFlags(PlayerEntity player) {
        ATE_DARK_AFTER_ADV.remove(player.getUuid());
        ATE_QUAKE_AFTER_ADV.remove(player.getUuid());
    }

    public static void clearFruit(PlayerEntity player) {
        String current = PLAYER_FRUIT.remove(player.getUuid());
        if (current != null) {
            UUID owner = FRUIT_OWNERS.get(current);
            if (owner != null && owner.equals(player.getUuid())) {
                FRUIT_OWNERS.remove(current);
            }
        }
    }

    public static void warnAndVanishFruit(PlayerEntity player, String fruitId) {
        if (player instanceof ServerPlayerEntity sp) {
            sp.sendMessage(Text.literal("§cWarning: Someone has already eaten this fruit. You cannot possess duplicates."), true);
        }
    }

    // Fusion unlock helpers
    public static boolean isDarkxQuakeUnlocked(PlayerEntity player) {
        return DARKXQUAKE_UNLOCKED.contains(player.getUuid());
    }

    public static boolean unlockDarkxQuake(PlayerEntity player) {
        // Only the first player to complete the ritual can ever unlock and claim the fusion
        if (hasDarkxQuakeChampion()) {
            if (player instanceof ServerPlayerEntity sp) {
                sp.sendMessage(Text.literal("§cAnother has already claimed the path to DARKxQUAKE."), false);
            }
            return false;
        }
        if (trySetDarkxQuakeChampion(player)) {
            DARKXQUAKE_UNLOCKED.add(player.getUuid());
            // Record that Dark was held at the time of the breakthrough so only Quake needs to be re-eaten post-advancement
            ATE_DARK_AFTER_ADV.add(player.getUuid());
            if (player instanceof ServerPlayerEntity sp) {
                sp.sendMessage(Text.literal("§8§lFusion Insight: The Mightiest Falls — You feel the path to DARKxQUAKE open."), false);
                sp.sendMessage(Text.literal("§7Only you may ever complete this fusion on this world."), false);
            }
            return true;
        }
        return false;
    }
}
