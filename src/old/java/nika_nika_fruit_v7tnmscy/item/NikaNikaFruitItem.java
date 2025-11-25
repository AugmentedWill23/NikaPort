package nika_nika_fruit_v7tnmscy.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.advancement.AdvancementEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;

import nika_nika_fruit_v7tnmscy.NikaNikaFruit;
import nika_nika_fruit_v7tnmscy.DevilFruitRegistry;

public class NikaNikaFruitItem extends Item {
    // New mastery curve: 1-10 easy, 10-30 easy, 30-40 slightly easy, 40-70 medium, 70-100 hard, 100-150 very hard, 150-200 extremely hard
    private static float calcExpRequired(int level) {
        // Easier, smoother curve to make 200 realistically reachable in survival
        if (level < 10) return 30.0f + level * 12.0f;            // 1..9
        if (level < 30) return 150.0f + (level - 10) * 15.0f;    // 10..29
        if (level < 60) return 450.0f + (level - 30) * 25.0f;    // 30..59
        if (level < 100) return 1200.0f + (level - 60) * 35.0f;  // 60..99
        if (level < 150) return 2600.0f + (level - 100) * 50.0f; // 100..149
        if (level < 200) return 5100.0f + (level - 150) * 65.0f; // 150..199
        return 8350.0f; // cap safeguard
    }

    private static boolean hasAdvancement(PlayerEntity player, String idPath) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            AdvancementEntry adv = serverPlayer.getServer().getAdvancementLoader().get(Identifier.of(NikaNikaFruit.MOD_ID, idPath));
            if (adv != null) {
                return serverPlayer.getAdvancementTracker().getProgress(adv).isDone();
            }
        }
        return false;
    }
    // Ultimate transformation requirements (changed per edit request)
    private static final float DAMAGE_TAKEN_REQUIRED = 200.0f; // Rage requirement (taken)
    private static final float DAMAGE_DEALT_REQUIRED = 100.0f; // Dealt requirement

    // Static data storage for player data
    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    // Saved inventories for players killed by Soul Steal (so they keep items regardless of keepInventory)
    public static final java.util.Map<java.util.UUID, java.util.List<ItemStack>> SOUL_SAVED_INVENTORY = new java.util.HashMap<>();

    private static class PlayerData {
        boolean devilFruitUser = false;
        float damageTaken = 0.0f;
        float damageDealt = 0.0f;
        boolean transformationActive = false;
        long transformationEndTime = 0L; // When transformation expires
        long abilityCooldown = 0L;
        long bazookaGunCooldown = 0L; // Cooldown for Bazooka Gun (5 min non-ult, 10 min ult)
        long supremeHakiCooldown = 0L; // Cooldown for Supreme Haki move
        long ultimateCooldown = 0L; // Cooldown for fruit ultimate (Right Shift)
        int currentMoveIndex = 0; // Current selected move
        int quickMoveIndex = 0; // Quick-use move index
        boolean hasWhiteHair = false; // Track hair color change
        boolean canReactivateUlt = true; // Track if ult can be reactivated
        boolean chargingSupremeHaki = false; // Track if charging supreme haki
        long chargeStartTime = 0L; // When charge started
        float chargeLevel = 0.0f; // Charge level 0.0 to 1.0 (1/5 to 5/5)
        
        // Track last counted damage tick to respect i-frames (10 ticks default)
        long lastDamageTickCounted = 0L;
        // DarkxQuake awakening tracking
        int dxqConsecutiveHits = 0;
        int dxqPlayerKills = 0;
        long lastHitTickCounted = 0L;
        
        // Quake/Dark ultimate challenge tracking (SMP-friendly unique challenges)
        int quakeActions = 0;           // Incremented when using Quake moves
        boolean quakeBossKill = false;  // Set true when killing Warden/Elder Guardian/Wither
        int darkSlowActions = 0;        // Incremented when using Dark moves that slow/ensnare
        int darkPlayerKills = 0;        // Player kills while owning the Dark fruit
        
        // Removed: Dark ultimate tracking
        // int darkDarknessDeathCount = 0;
        
        // Mastery system fields - per fruit
        int masteryLevelNika = 1;
        int masteryLevelDark = 1;
        int masteryLevelQuake = 1;
        int masteryLevelDxq = 1;
        int masteryLevelSoul = 1;
        int masteryLevelDragon = 1;
        int masteryLevelOperation = 1;
        
        float masteryExpNika = 0.0f;
        float masteryExpDark = 0.0f;
        float masteryExpQuake = 0.0f;
        float masteryExpDxq = 0.0f;
        float masteryExpSoul = 0.0f;
        float masteryExpDragon = 0.0f;
        float masteryExpOperation = 0.0f;
        
        float expRequiredForNextLevelNika = 100.0f;
        float expRequiredForNextLevelDark = 100.0f;
        float expRequiredForNextLevelQuake = 100.0f;
        float expRequiredForNextLevelDxq = 100.0f;
        float expRequiredForNextLevelSoul = 100.0f;
        float expRequiredForNextLevelDragon = 100.0f;
        float expRequiredForNextLevelOperation = 100.0f;
        
        int attackUsageCountNika = 0;
        int attackUsageCountDark = 0;
        int attackUsageCountQuake = 0;
        int attackUsageCountDxq = 0;
        int attackUsageCountSoul = 0;
        int attackUsageCountDragon = 0;
        int attackUsageCountOperation = 0;
        // New ultimate unlock tracking per edit request
        int darkDarknessDeathCount = 0;           // Deaths while under Darkness (Dark fruit)
        int quakeDeathsToDrownedTrident = 0;      // Deaths to Drowned with a Trident
        int quakeDeathsWithDolphin = 0;           // Deaths while having Dolphin's Grace
        // Soul fruit tracking
        long soulStealCooldown = 0L;          // 30 min cooldown
        int soulStolenPlayers = 0;            // persistent counter for ult unlock
        long soulGuardEndTime = 0L;           // Spirit Guard active until
        String soulBorrowedFruit = null;      // borrowed fruit id
        String soulBorrowedMove = null;       // borrowed move name for display
        long soulBorrowEndTime = 0L;          // 25 min duration
        int soulGuardHitsRemaining = 0;      // Spirit Guard projectile HP pool (3 shields x 10 HP)
        long[] nikaMoveCooldowns = new long[11]; // Per-move cooldowns for Nika
        long[] darkMoveCooldowns = new long[6];  // Per-move cooldowns for Dark
        long[] quakeMoveCooldowns = new long[6]; // Per-move cooldowns for Quake
        long[] dxqMoveCooldowns = new long[11];  // Per-move cooldowns for DarkxQuake
        long[] soulMoveCooldowns = new long[9];  // Per-move cooldowns for Soul fruit
        long[] dragonMoveCooldowns = new long[6]; // Per-move cooldowns for Dragon fruit
        long[] operationMoveCooldowns = new long[7]; // Per-move cooldowns for Operation fruit
        long opRoomEndTime = 0L; // Operation Room active until
        int opRoomRadius = 10;   // Passive radius 10, 30 when Room is cast
    }

    public NikaNikaFruitItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        
        // World-unique ownership check and duplicate prevention
        if (!world.isClient) {
            // If someone else owns Nika, vanish and warn
            if (DevilFruitRegistry.isFruitOwned(DevilFruitRegistry.FRUIT_NIKA) && !DevilFruitRegistry.playerOwnsFruit(user, DevilFruitRegistry.FRUIT_NIKA)) {
                user.sendMessage(Text.literal("§cWarning: Someone has already eaten this fruit. You cannot possess duplicates."), true);
                if (!user.isCreative()) stack.decrement(1);
                return ActionResult.SUCCESS;
            }
            // If player already holds any fruit, instant death on trying to eat another
            if (DevilFruitRegistry.hasAnyFruit(user) && !DevilFruitRegistry.playerOwnsFruit(user, DevilFruitRegistry.FRUIT_NIKA)) {
                if (user instanceof ServerPlayerEntity sp) {
                    sp.sendMessage(Text.literal("§cYou attempted to possess a duplicate Devil Fruit!"), true);
                    sp.damage(sp.getServerWorld(), sp.getDamageSources().outOfWorld(), Float.MAX_VALUE);
                }
                if (!user.isCreative()) stack.decrement(1);
                return ActionResult.SUCCESS;
            }
        }
        
        // Check if player is already a devil fruit user (legacy flag for Nika systems)
        if (isDevilFruitUser(user)) {
            user.sendMessage(Text.literal("§cYou have already consumed a Devil Fruit!"), true);
            return ActionResult.FAIL;
        } else {
            // Consume the fruit
            if (!world.isClient) {
                // Assign world-unique ownership and mark as Nika user
                DevilFruitRegistry.assignFruit(user, DevilFruitRegistry.FRUIT_NIKA);
                makeDevilFruitUser(user);
                stack.decrement(1);
                user.sendMessage(Text.literal("§6You have consumed the Nika-Nika Fruit! You are now a rubber human!"), false);
                user.sendMessage(Text.literal("§eR: cycle | Z: use | O: ult reqs | RShift: awaken | X: quick-use"), false);
                // Grant advancement for consuming the fruit
                grantAdvancement(user, "fruit_consumed");
                
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
            return ActionResult.SUCCESS;
        }
    }

    // New keybind-based system methods
    public static void cycleMove(PlayerEntity player) {
        if (!isDevilFruitUser(player)) return;
        
        PlayerData data = getPlayerData(player);
        checkTransformationExpiry(player); // Check if transformation has expired
        boolean isTransformed = data.transformationActive;
        
        // Find next available move based on mastery level
        int startIndex = data.currentMoveIndex;
        String[] moveNames = getMoveNames(player, isTransformed);
        int maxMoves = moveNames.length;
        
        do {
            data.currentMoveIndex = (data.currentMoveIndex + 1) % maxMoves;
        } while (!canUseMove(player, data.currentMoveIndex) && data.currentMoveIndex != startIndex);
        
        // If no moves available, stay on first available move
        if (!canUseMove(player, data.currentMoveIndex)) {
            data.currentMoveIndex = 0;
        }
        
        // Send feedback to player
        
        if (canUseMove(player, data.currentMoveIndex)) {
            player.sendMessage(Text.literal("§eSelected: " + moveNames[data.currentMoveIndex]), true);
        } else {
            player.sendMessage(Text.literal("§cMove locked! Mastery Level " + getRequiredLevelFor(player, data.currentMoveIndex) + " required."), true);
        }
        
        // Play sound
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 0.5F, 1.2F);
    }
    
    // Dynamic move names per fruit
    public static String[] getMoveNames(PlayerEntity player, boolean isTransformed) {
        boolean isDxQ = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE);
        if (isDxQ) {
            return isTransformed ?
                new String[]{
                    "Black Vortex", "Quake Fist", "Dark Cage", "Seismic Burst",
                    "Gravity Crush", "Dark Matter Slam", "DarkxQuake Cataclysm",
                    "Event Horizon Quake", "Sea Splitter", "Void Implosion",
                    "Cataclysm Supreme"
                } :
                new String[]{
                    "Black Vortex", "Quake Fist", "Dark Cage", "Seismic Burst",
                    "Gravity Crush", "Dark Matter Slam", "DarkxQuake Cataclysm"
                };
        }
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE)) {
            return isTransformed ?
                new String[]{
                    "Tremor Punch", "Shockwave Push", "Seismic Smash",
                    "Sea Quake", "Airquake", "Crack Dome"
                } :
                new String[]{
                    "Tremor Punch", "Shockwave Push", "Seismic Smash",
                    "Sea Quake", "Airquake", "Crack Dome"
                };
        }
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARK)) {
            return isTransformed ?
                new String[]{
                    "Black Vortex", "Dark Matter Grab", "Black Hole", "Darkness Wave", "Gravity Crush", "Night Shroud"
                } :
                new String[]{
                    "Black Vortex", "Dark Matter Grab", "Black Hole", "Darkness Wave", "Gravity Crush", "Night Shroud"
                };
        }
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_SOUL)) {
            return new String[]{
                "Soul Bolt", "Spirit Guard", "Wraith Push", "Phantom Bind", "Spirit Barrage",
                "Soul Steal", "Soul Army", "Life Drain", "Soul Dominance"
            }; 
        }
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DRAGON)) {
            // Dragon (non-ult transform at 250)
            return new String[]{
                "Dragon Slash", "Heat Breath", "Thunder Bagua Smash", "Dragon Twister", "Flame Clouds", "Dragon's Roar"
            };
        }
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_OPERATION)) {
            // Operation (Room mechanics)
            return new String[]{
                "Room", "Shambles", "Scan", "Injection Shot", "Tact", "Gamma Knife"
            };
        }
        // Default to Nika names
        return isTransformed ?
            new String[]{"Gear Five - Gomu Gomu no Pistol", "Gear Five - Gomu Gomu no Balloon", "Gear Five - Gomu Gomu no Rocket",
                        "Gear Five - Gomu Gomu no Whip", "Gear Five - Gomu Gomu no Bazooka", "Gear Five - Gomu Gomu no Bell",
                        "Gear Five - Gomu Gomu no Barjan Gun", "Gear Five - Gomu Gomu no Gigantii", "Gear Five - Gomu Gomu no Lightning", "Gear Five - Toon Force", "Gear 5 Supreme Haki Infused Barjan Gun"} :
            new String[]{"Gomu Gomu no Pistol", "Gomu Gomu no Balloon", "Gomu Gomu no Rocket",
                        "Gomu Gomu no Whip", "Gomu Gomu no Bazooka", "Gomu Gomu no Bell",
                        "Gomu Gomu no Barjan Gun"};
    }
    
    public static int getRequiredLevelFor(PlayerEntity player, int moveIndex) {
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE)) {
            return switch (moveIndex) {
                case 0 -> 1;  // Black Vortex
                case 1 -> 3;  // Quake Fist
                case 2 -> 5;  // Dark Cage
                case 3 -> 10; // Seismic Burst
                case 4 -> 15; // Gravity Crush
                case 5 -> 20; // Dark Matter Slam
                case 6 -> 25; // Cataclysm
                case 7 -> 60; // Event Horizon Quake
                case 8 -> 75; // Sea Splitter
                case 9 -> 90; // Void Implosion
                case 10 -> 100; // Cataclysm Supreme
                default -> 1;
            };
        }
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE)) {
            return switch (moveIndex) {
                case 0 -> 1;  // Tremor Punch
                case 1 -> 5;  // Shockwave Push
                case 2 -> 10; // Seismic Smash
                case 3 -> 26; // Sea Quake
                case 4 -> 51; // Airquake
                case 5 -> 75; // Crack Dome
                case 6 -> 100; // reserved ultimate
                default -> 1;
            };
        }
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARK)) {
            return switch (moveIndex) {
                case 0 -> 1;  // Black Vortex
                case 1 -> 5;  // Dark Matter Grab
                case 2 -> 26; // Black Hole
                case 3 -> 51; // Darkness Wave
                case 4 -> 60; // Gravity Crush
                case 5 -> 26; // Night Shroud (new utility move)
                default -> 1;
            };
        }
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_SOUL)) {
            boolean ultUnlocked = getPlayerData(player).soulStolenPlayers >= 3 || getMasteryLevel(player) >= 200;
            return switch (moveIndex) {
                case 0 -> 1;    // Soul Bolt
                case 1 -> 25;   // Spirit Guard
                case 2 -> 50;   // Wraith Push
                case 3 -> 75;   // Phantom Bind
                case 4 -> 100;  // Spirit Barrage
                case 5 -> 1;    // Soul Steal (passive)
                case 6 -> ultUnlocked ? 125 : 9999;  // Soul Army
                case 7 -> ultUnlocked ? 150 : 9999;  // Life Drain
                case 8 -> ultUnlocked ? 175 : 9999;  // Soul Dominance
                default -> 1;
            };
        }
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DRAGON)) {
            return switch (moveIndex) {
                case 0 -> 0;    // Dragon Slash (now first move)
                case 1 -> 50;   // Heat Breath
                case 2 -> 120;  // Thunder Bagua Smash
                case 3 -> 170;  // Dragon Twister
                case 4 -> 200;  // Flame Clouds
                case 5 -> 230;  // Dragon's Roar
                default -> 1;
            };
        }
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_OPERATION)) {
            return switch (moveIndex) {
                case 0 -> 25;  // Room
                case 1 -> 75;  // Shambles
                case 2 -> 120; // Scan
                case 3 -> 160; // Injection Shot
                case 4 -> 200; // Tact
                case 5 -> 250; // Gamma Knife
                default -> 1;
            };
        }
        return switch (moveIndex) {
            case 0 -> 1; // Pistol
            case 1 -> 3; // Balloon
            case 2 -> 5; // Rocket
            case 3 -> 10; // Whip
            case 4 -> 15; // Bazooka
            case 5 -> 20; // Bell
            case 6 -> 25; // Bazooka Gun
            case 7 -> 60; // Gigant
            case 8 -> 75; // Lightning
            case 9 -> 90; // Toon Force
            case 10 -> 100; // Supreme Haki
            default -> 1;
        };
    }
    
    public static void useCurrentMove(PlayerEntity player) {
        if (!isDevilFruitUser(player) || player.getWorld().isClient) return;
        
        World world = player.getWorld();
        long currentTime = world.getTime();
        {
            int idxCheck = getPlayerData(player).currentMoveIndex;
            long remaining = getCurrentFruitMoveCooldown(player, idxCheck);
            if (remaining > 0) {
                long sec = remaining / 20;
                player.sendMessage(Text.literal("§cAbility on cooldown: " + sec + "s"), true);
                return;
            }
        }

        PlayerData data = getPlayerData(player);
        checkTransformationExpiry(player); // Check if transformation has expired
        int moveIndex = data.currentMoveIndex;
        boolean isTransformed = data.transformationActive;
        
        // Special cooldown check for Bazooka Gun
        if (moveIndex == 6) { // Bazooka Gun position
            if (currentTime < data.bazookaGunCooldown) {
                long remainingTicks = data.bazookaGunCooldown - currentTime;
                long remainingMinutes = remainingTicks / 1200; // 20 ticks per second, 60 seconds per minute
                player.sendMessage(Text.literal("§cBazooka Gun on cooldown! " + remainingMinutes + " minutes remaining."), true);
                return;
            }
        }
        
        // Special cooldown check for Supreme Haki (Gear 5 only)
        if (moveIndex == 10 && isTransformed) { // Supreme Haki position
            if (currentTime < data.supremeHakiCooldown) {
                long remainingTicks = data.supremeHakiCooldown - currentTime;
                long remainingMinutes = remainingTicks / 1200;
                player.sendMessage(Text.literal("§cSupreme Haki on cooldown! " + remainingMinutes + " minutes remaining."), true);
                return;
            }
        }
        
        // Check if trying to use awakened (transformation-only) moves for fruits that require it (Nika/DxQ)
        boolean ownsNika = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_NIKA);
        boolean ownsDxq  = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE);
        if (!isTransformed && moveIndex >= 7 && (ownsNika || ownsDxq)) {
            player.sendMessage(Text.literal("§cYou need to awaken first!"), true);
            return;
        }
        
        // Check if move is unlocked
        if (!canUseMove(player, moveIndex)) {
            int requiredLevel = getRequiredLevelFor(player, moveIndex);
            player.sendMessage(Text.literal("§cMove locked! Mastery Level " + requiredLevel + " required."), true);
            return;
        }
        
        // Set balanced cooldowns
        if (DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_SOUL)) {
            // use existing data
            long now = world.getTime();
            int idx = data.currentMoveIndex;
            long soulCd = getSoulCooldownForIndex(player, idx);
            if (idx == 5) { // Soul Steal slot has its own long cooldown
                soulCd = Math.max(soulCd, data.soulStealCooldown);
            }
            if (now < soulCd) {
                long sec = (soulCd - now) / 20;
                player.sendMessage(Text.literal("§cAbility on cooldown: " + sec + "s"), true);
                return;
            }
        }
        // Apply per-move cooldowns by category (per-ability; others remain available)
        {
            long baseSmall = 40;   // 2s - beginner
            long baseMedium = 100; // 5s - movement/utility
            long baseBig = 200;    // 10s - strong
            long baseUlt = 1200;   // 60s - ultimate-exclusive (unless special timers below)

            int idx = moveIndex;
            long cdToApply = 0L;

            if (DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_SOUL) ||
                DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_DRAGON) ||
                DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_OPERATION)) {
                // These fruits manage per-move cooldowns inside each move
            } else if (DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_NIKA)) {
                switch (idx) {
                    case 0 -> cdToApply = baseSmall;               // Pistol - beginner
                    case 1, 2 -> cdToApply = baseMedium;           // Balloon/Rocket - movement
                    case 3, 4, 5 -> cdToApply = baseBig;           // Whip/Bazooka/Bell - strong
                    case 6 -> {                                    // Bazooka Gun - exclusive
                        cdToApply = 60;                            // short local lock only
                        // Long CD for Bazooka Gun (special timer that doesn't block other moves)
                        if (isTransformed) {
                            data.bazookaGunCooldown = currentTime + 7200; // 6 minutes
                        } else {
                            data.bazookaGunCooldown = currentTime + 9600; // 8 minutes
                        }
                    }
                    case 7, 8, 9 -> cdToApply = baseBig;           // Awakened strong moves
                    case 10 -> {                                   // Supreme Haki - exclusive
                        cdToApply = 100;                           // short local lock
                        data.supremeHakiCooldown = currentTime + 12000; // 10 minutes
                    }
                    default -> cdToApply = baseSmall;
                }
            } else if (DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_DARK)) {
                switch (idx) {
                    case 0 -> cdToApply = baseSmall;              // Black Vortex - beginner
                    case 1, 2, 4 -> cdToApply = baseBig;          // Grab/Black Hole/Gravity Crush - strong
                    case 3, 5 -> cdToApply = baseMedium;          // Darkness Wave/Night Shroud - movement/utility
                    default -> cdToApply = baseSmall;
                }
            } else if (DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_QUAKE)) {
                switch (idx) {
                    case 0 -> cdToApply = baseSmall;              // Tremor Punch - beginner
                    case 1 -> cdToApply = baseMedium;             // Shockwave Push - movement
                    case 2 -> cdToApply = baseBig;                // Seismic Smash - strong
                    case 3, 5 -> cdToApply = baseBig;             // Sea Quake/Crack Dome - strong
                    case 4 -> cdToApply = baseMedium;             // Airquake - mobility/utility
                    default -> cdToApply = baseSmall;
                }
            } else if (DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_DARKXQUAKE)) {
                switch (idx) {
                    case 0 -> cdToApply = baseSmall;              // Black Vortex - beginner
                    case 1, 2 -> cdToApply = baseMedium;          // Quake Fist/Dark Cage - movement/utility
                    case 3, 4 -> cdToApply = baseBig;             // Seismic Burst/Gravity Crush - strong
                    case 5 -> cdToApply = baseBig;                // Dark Matter Slam - strong
                    case 6 -> cdToApply = baseUlt;                // Cataclysm (base ultimate)
                    case 7, 8, 9 -> cdToApply = baseBig;          // Awakened strongs
                    case 10 -> cdToApply = baseUlt + 600;         // Cataclysm Supreme - longest
                    default -> cdToApply = baseSmall;
                }
            } else {
                cdToApply = baseSmall; // fallback
            }

            if (cdToApply > 0) {
                setCurrentFruitMoveCooldown(player, idx, currentTime + cdToApply);
            }
        }

        // Add attack EXP for using moves
        addAttackExp(player);
        
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE)) {
            // DarkxQuake move set
            switch (moveIndex) {
                case 0 -> dxqBlackVortex(world, player);
                case 1 -> dxqQuakeFist(world, player);
                case 2 -> dxqDarkCage(world, player);
                case 3 -> dxqSeismicBurst(world, player);
                case 4 -> dxqGravityCrush(world, player);
                case 5 -> dxqDarkMatterSlam(world, player);
                case 6 -> dxqCataclysm(world, player); // High power base ultimate slot
                case 7 -> dxqEventHorizonQuake(world, player);
                case 8 -> dxqSeaSplitter(world, player);
                case 9 -> dxqVoidImplosion(world, player);
                case 10 -> dxqCataclysmSupreme(world, player);
            }
        } else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE)) {
            switch (moveIndex) {
                case 0 -> quakeTremorPunch(world, player);
                case 1 -> quakeShockwavePush(world, player);
                case 2 -> quakeSeismicSmash(world, player);
                case 3 -> quakeSeaQuake(world, player);
                case 4 -> quakeAirquake(world, player);
                case 5 -> quakeCrackDome(world, player);
                // rest reserved
            }
        } else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARK)) {
            switch (moveIndex) {
                case 0 -> darkBlackVortex(world, player);
                case 1 -> darkDarkMatterGrab(world, player);
                case 2 -> darkBlackHole(world, player);
                case 3 -> darkDarknessWave(world, player);
                case 4 -> darkGravityCrush(world, player);
                case 5 -> darkNightShroud(world, player);
            }
        } else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_SOUL)) {
            switch (moveIndex) {
                case 0 -> soulBolt(world, player);
                case 1 -> spiritGuard(world, player);
                case 2 -> wraithPush(world, player);
                case 3 -> phantomBind(world, player);
                case 4 -> spiritBarrage(world, player);
                case 5 -> soulSteal(world, player);
                case 6 -> soulArmy(world, player);
                case 7 -> lifeDrain(world, player);
                case 8 -> soulDominance(world, player);
            }
        } else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DRAGON)) {
            switch (moveIndex) {
                case 0 -> dragonSlash(world, player);
                case 1 -> dragonHeatBreath(world, player);
                case 2 -> dragonThunderBagua(world, player);
                case 3 -> dragonTwister(world, player);
                case 4 -> dragonFlameClouds(world, player);
                case 5 -> dragonRoar(world, player);
            }
        } else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_OPERATION)) {
            switch (moveIndex) {
                case 0 -> opRoom(world, player);
                case 1 -> opShambles(world, player);
                case 2 -> opScan(world, player);
                case 3 -> opInjectionShot(world, player);
                case 4 -> opTact(world, player);
                case 5 -> opGammaKnife(world, player);
            }
        } else {
            // Nika default move set
            switch (moveIndex) {
                case 0 -> gomuGomuPistol(world, player, isTransformed);
                case 1 -> gomuGomuBalloon(world, player, isTransformed);
                case 2 -> gomuGomuRocket(world, player, isTransformed);
                case 3 -> gomuGomuWhip(world, player, isTransformed);
                case 4 -> gomuGomuBazooka(world, player, isTransformed);
                case 5 -> gomuGomuBell(world, player, isTransformed);
                case 6 -> gomuGomuBazookaGun(world, player, isTransformed); // Variable cooldown move
                case 7 -> gomuGomuGigant(world, player); // Transformation only
                case 8 -> gomuGomuLightning(world, player); // Transformation only
                case 9 -> toonForce(world, player); // Transformation only
                case 10 -> supremeHakiBarjanGun(world, player); // Ultimate Gear 5 move
            }
        }
    }

    // Helper: cooldown reduction if Dragon Fury active
    private static long maybeReduceForDragon(PlayerEntity p, long ticks) {
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(p, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DRAGON) && isTransformed(p)) {
            return (long)Math.max(1, Math.floor(ticks * 0.75));
        }
        return ticks;
    }
    
    public static void attemptUltimateTransform(PlayerEntity player) {
        if (!isDevilFruitUser(player) || player.getWorld().isClient) return;
        
        PlayerData data = getPlayerData(player);
        checkTransformationExpiry(player); // Check if transformation has expired
        long now = player.getWorld().getTime();
        // Global ultimate cooldown check (Quake only)
        boolean __isQuake = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE);
        if (__isQuake && now < data.ultimateCooldown) {
            long remain = (data.ultimateCooldown - now) / 20;
            player.sendMessage(Text.literal("§cUltimate on cooldown: " + remain + "s"), true);
            return;
        }
        
        // Quake: unique SMP challenge ultimate (no transformation)
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE)) {
            if (!isQuakeUltimateReady(player)) {
                player.sendMessage(Text.literal("§cUltimate locked. Press O to view requirements."), true);
                return;
            }
            if (now < data.ultimateCooldown) {
                long remain = (data.ultimateCooldown - now) / 20;
                player.sendMessage(Text.literal("§cUltimate on cooldown: " + remain + "s"), true);
                return;
            }
            player.sendMessage(Text.literal("§f§lULT: Tremor Cataclysm!"), false);
            quakeCrackDome(player.getWorld(), player);
            quakeSeaQuake(player.getWorld(), player);
            data.ultimateCooldown = now + 6000; // 5 minutes
            return;
        }
        
        // Dark: no ultimate — pressing Right Shift does nothing for Dark users
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARK)) {
            player.sendMessage(Text.literal("§8Dark has no ultimate. Use its moves instead."), true);
            return;
        }
        // Soul: ult is not a transformation; unlock Soul Army/Life Drain/Soul Dominance by stealing 3 player souls or reaching Mastery 200
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_SOUL)) {
            player.sendMessage(Text.literal("§bSoul ult is not a transform. Steal 3 player souls or reach Mastery 200, then use your ultimate moves with Z."), true);
            return;
        }
        
        // Check if already transformed
        if (data.transformationActive) {
            player.sendMessage(Text.literal("§cYour ultimate is already active!"), true);
            return;
        }
        
        // Check if can reactivate
        if (!data.canReactivateUlt) {
            player.sendMessage(Text.literal("§cYou must wait for the transformation to naturally expire before using it again!"), true);
            return;
        }
        
        // Check mastery level requirement first (per fruit rules)
        if (!canTransformToGear5(player)) {
            // Dynamic ult name
            String ultName = "Awakening";
            if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE)) ultName = "The True Menace";
            else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE)) ultName = "Tremor Awakening";
            else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARK)) ultName = "Darkness Awakening";
            else ultName = "Gear 5";
            player.sendMessage(Text.literal("§cUltimate locked. Press O to view requirements."), true);
            return;
        }
        
        if (canTransform(player)) {
            activateTransformation(player, player.getWorld());
        } else {
            float damageTaken = getPlayerDamageTaken(player);
            float damageDealt = getPlayerDamageDealt(player);
            float needTaken = Math.max(0f, DAMAGE_TAKEN_REQUIRED - damageTaken);
            float needDealt = Math.max(0f, DAMAGE_DEALT_REQUIRED - damageDealt);
            player.sendMessage(Text.literal("§cUltimate locked. Press O to view requirements."), true);
        }
    }

    // Base Abilities
    private static void gomuGomuPistol(World world, PlayerEntity user, boolean isTransformed) {
        String moveName = isTransformed ? "§fGear Five - Gomu Gomu no Pistol!" : "§eGomu Gomu no Pistol!";
        user.sendMessage(Text.literal(moveName), true);
        
        Vec3d direction = user.getRotationVector();
        Vec3d knockback = direction.multiply(3.0); // Increased knockback
        
        // Apply knockback to nearby enemies with better range and damage
        float range = isTransformed ? 10.0f : 6.0f; // Balanced range in Gear 5
        float damage = isTransformed ? 14.0f : 8.0f; // Balanced damage in Gear 5
        Vec3d enhancedKnockback = isTransformed ? knockback.multiply(1.5) : knockback;
        
        world.getOtherEntities(user, user.getBoundingBox().expand(range),
            entity -> entity instanceof LivingEntity && entity != user)
            .forEach(entity -> {
                if (entity instanceof LivingEntity living && world instanceof ServerWorld serverWorld) {
                    // Check if entity is in front of the player (pistol direction)
                    Vec3d toEntity = living.getPos().subtract(user.getPos()).normalize();
                    double dotProduct = toEntity.dotProduct(direction);
                    if (dotProduct > 0.5) { // Entity is in front
                        living.damage(serverWorld, serverWorld.getDamageSources().playerAttack(user), damage);
                        living.addVelocity(enhancedKnockback.x, Math.max(enhancedKnockback.y, 0.7), enhancedKnockback.z);
                        living.velocityModified = true;
                    }
                }
            });
        
        // Enhanced particles with stretched arm effect
        if (world instanceof ServerWorld serverWorld) {
            Vec3d particlePos = user.getPos().add(direction.multiply(3.0));
            serverWorld.spawnParticles(ParticleTypes.POOF, 
                particlePos.x, particlePos.y, particlePos.z, 
                20, 0.5, 0.5, 0.5, 0.15); // More particles
            
            // Stretch effect particles
            for (int i = 1; i <= 5; i++) {
                Vec3d stretchPos = user.getPos().add(direction.multiply(i));
                serverWorld.spawnParticles(ParticleTypes.CLOUD, 
                    stretchPos.x, stretchPos.y, stretchPos.z, 
                    3, 0.1, 0.1, 0.1, 0.05);
            }
        }
        
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0F, 1.5F);
    }

    private static void gomuGomuBalloon(World world, PlayerEntity user, boolean isTransformed) {
        String moveName = isTransformed ? "§fGear Five - Gomu Gomu no Balloon!" : "§eGomu Gomu no Balloon!";
        user.sendMessage(Text.literal(moveName), true);
        
        // Enhanced effects with strong jump force and levitation - stronger in Gear 5
        int duration = isTransformed ? 300 : 200; // Longer duration in Gear 5
        int resistanceLevel = isTransformed ? 4 : 3; // Stronger resistance in Gear 5
        int jumpLevel = isTransformed ? 6 : 4; // Much stronger jump in Gear 5
        int levitationLevel = isTransformed ? 2 : 1; // Stronger levitation in Gear 5
        double upwardVelocity = isTransformed ? 2.0 : 1.5; // More upward force in Gear 5
        
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, duration, resistanceLevel));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, duration, jumpLevel));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 60, levitationLevel));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 400, 0));
        
        // Immediate upward boost
        user.addVelocity(0, upwardVelocity, 0);
        user.velocityModified = true;
        
        // Balloon effect particles
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.CLOUD, 
                user.getX(), user.getY() + 1, user.getZ(), 
                30, 1.0, 1.0, 1.0, 0.1);
            
            serverWorld.spawnParticles(ParticleTypes.POOF, 
                user.getX(), user.getY() + 0.5, user.getZ(), 
                20, 0.8, 0.8, 0.8, 0.15);
        }
        
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.BLOCK_SLIME_BLOCK_PLACE, SoundCategory.PLAYERS, 1.0F, 0.8F);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.PLAYERS, 0.8F, 1.2F);
    }

    private static void gomuGomuRocket(World world, PlayerEntity user, boolean isTransformed) {
        String moveName = isTransformed ? "§fGear Five - Gomu Gomu no Rocket!" : "§eGomu Gomu no Rocket!";
        user.sendMessage(Text.literal(moveName), true);
        
        Vec3d direction = user.getRotationVector();
        double multiplier = isTransformed ? 3.5 : 2.5; // Much more launch force in Gear 5
        double minVertical = isTransformed ? 1.5 : 1.2; // Higher vertical boost in Gear 5
        Vec3d velocity = direction.multiply(multiplier);
        
        user.addVelocity(velocity.x, Math.max(velocity.y, minVertical), velocity.z);
        user.velocityModified = true;
        
        // Damage enemies on impact - enhanced in Gear 5
        double impactRange = isTransformed ? 5.0 : 3.0;
        float impactDamage = isTransformed ? 16.0f : 10.0f;
        double knockbackMultiplier = isTransformed ? 3.0 : 2.0;
        
        world.getOtherEntities(user, user.getBoundingBox().expand(impactRange), 
            entity -> entity instanceof LivingEntity && entity != user)
            .forEach(entity -> {
                if (entity instanceof LivingEntity living && world instanceof ServerWorld serverWorld) {
                    living.damage(serverWorld, serverWorld.getDamageSources().playerAttack(user), impactDamage);
                    Vec3d knockback = direction.multiply(knockbackMultiplier);
                    living.addVelocity(knockback.x, Math.max(knockback.y, 0.8), knockback.z);
                    living.velocityModified = true;
                }
            });
        
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.CLOUD, 
                user.getX(), user.getY(), user.getZ(), 
                25, 0.8, 0.8, 0.8, 0.3); // More particles
                
            // Rocket trail effect
            serverWorld.spawnParticles(ParticleTypes.FLAME, 
                user.getX(), user.getY(), user.getZ(), 
                15, 0.3, 0.3, 0.3, 0.1);
        }
        
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, 1.0F, 1.2F);
    }

    private static void gomuGomuWhip(World world, PlayerEntity user, boolean isTransformed) {
        String moveName = isTransformed ? "§fGear Five - Gomu Gomu no Whip!" : "§eGomu Gomu no Whip!";
        user.sendMessage(Text.literal(moveName), true);
        
        // Enhanced AoE attack around player - much stronger in Gear 5
        double range = isTransformed ? 10.0 : 7.0; // Much larger range in Gear 5
        float damage = isTransformed ? 16.0f : 10.0f; // Higher damage in Gear 5
        double knockbackMultiplier = isTransformed ? 3.5 : 2.5; // More knockback in Gear 5
        
        world.getOtherEntities(user, user.getBoundingBox().expand(range),
            entity -> entity instanceof LivingEntity && entity != user)
            .forEach(entity -> {
                if (entity instanceof LivingEntity living && world instanceof ServerWorld serverWorld) {
                    living.damage(serverWorld, serverWorld.getDamageSources().playerAttack(user), damage);
                    Vec3d knockback = living.getPos().subtract(user.getPos()).normalize().multiply(knockbackMultiplier);
                    living.addVelocity(knockback.x, 0.8, knockback.z);
                    living.velocityModified = true;
                }
            });
        
        // Whip effect particles in a circle - more intense in Gear 5
        if (world instanceof ServerWorld serverWorld) {
            int particleCount = isTransformed ? 24 : 16; // More particles in Gear 5
            double circleRadius = isTransformed ? 8 : 5; // Larger circle in Gear 5
            
            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI * i) / particleCount;
                double x = user.getX() + circleRadius * Math.cos(angle);
                double z = user.getZ() + circleRadius * Math.sin(angle);
                serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, 
                    x, user.getY() + 1, z, 
                    isTransformed ? 4 : 2, 0.1, 0.1, 0.1, 0.05);
                    
                // Extra white particles for Gear 5
                if (isTransformed) {
                    serverWorld.spawnParticles(ParticleTypes.END_ROD, 
                        x, user.getY() + 1, z, 
                        2, 0.2, 0.2, 0.2, 0.03);
                }
            }
        }
        
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0F, 0.8F);
    }

    private static void gomuGomuBazooka(World world, PlayerEntity user, boolean isTransformed) {
        String moveName = isTransformed ? "§fGear Five - Gomu Gomu no Bazooka!" : "§eGomu Gomu no Bazooka!";
        user.sendMessage(Text.literal(moveName), true);
        
        Vec3d direction = user.getRotationVector();
        
        // Enhanced range and damage - much stronger in Gear 5
        double range = isTransformed ? 13.0 : 9.0; // Much larger range in Gear 5
        float damage = isTransformed ? 25.0f : 18.0f; // Higher damage in Gear 5
        double knockbackMultiplier = isTransformed ? 4.5 : 3.5; // More knockback in Gear 5
        int effectDuration = isTransformed ? 300 : 200; // Longer effects in Gear 5
        int effectLevel = isTransformed ? 2 : 1; // Stronger effects in Gear 5
        
        world.getOtherEntities(user, user.getBoundingBox().expand(range),
            entity -> entity instanceof LivingEntity && entity != user)
            .forEach(entity -> {
                if (entity instanceof LivingEntity living && world instanceof ServerWorld serverWorld) {
                    // Check if entity is in front direction for bazooka
                    Vec3d toEntity = living.getPos().subtract(user.getPos()).normalize();
                    double dotProduct = toEntity.dotProduct(direction);
                    if (dotProduct > 0.3) { // Wider cone than pistol
                        living.damage(serverWorld, serverWorld.getDamageSources().playerAttack(user), damage);
                        living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, effectDuration, effectLevel));
                        Vec3d knockback = direction.multiply(knockbackMultiplier);
                        living.addVelocity(knockback.x, Math.max(knockback.y, 1.2), knockback.z);
                        living.velocityModified = true;
                    }
                }
            });
        
        if (world instanceof ServerWorld serverWorld) {
            Vec3d explosionPos = user.getPos().add(direction.multiply(4));
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION, 
                explosionPos.x, explosionPos.y, explosionPos.z, 
                10, 1.5, 1.5, 1.5, 0.0); // More explosions
                
            // Double fist effect particles
            serverWorld.spawnParticles(ParticleTypes.POOF, 
                explosionPos.x, explosionPos.y, explosionPos.z, 
                30, 1.0, 1.0, 1.0, 0.2);
        }
        
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0F, 1.2F);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 1.0F, 0.8F);
    }

    private static void gomuGomuBell(World world, PlayerEntity user, boolean isTransformed) {
        String moveName = isTransformed ? "§fGear Five - Gomu Gomu no Bell!" : "§eGomu Gomu no Bell!";
        user.sendMessage(Text.literal(moveName), true);
        
        // Enhanced area and effects in Gear 5
        double range = isTransformed ? 5.0 : 3.0;
        float damage = isTransformed ? 15.0f : 10.0f;
        int nauseaDuration = isTransformed ? 160 : 100;
        int slownessDuration = isTransformed ? 100 : 60;
        int effectLevel = isTransformed ? 2 : 1;
        
        world.getOtherEntities(user, user.getBoundingBox().expand(range), 
            entity -> entity instanceof LivingEntity && entity != user)
            .forEach(entity -> {
                if (entity instanceof LivingEntity living && world instanceof ServerWorld serverWorld) {
                    living.damage(serverWorld, serverWorld.getDamageSources().playerAttack(user), damage);
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, nauseaDuration, effectLevel));
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, slownessDuration, effectLevel));
                    
                    // Extra confusion effect in Gear 5
                    if (isTransformed) {
                        living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 120, 1));
                    }
                }
            });
        
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.NOTE, 
                user.getX(), user.getY() + 2, user.getZ(), 
                20, 1.0, 1.0, 1.0, 0.0);
        }
        
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0F, 0.5F);
    }

    private static void gomuGomuBazookaGun(World world, PlayerEntity user, boolean isTransformed) {
        String moveName = isTransformed ? "§f§lGear Five - Gomu Gomu no BAZOOKA GUN!" : "§c§lGomu Gomu no BAZOOKA GUN!";
        user.sendMessage(Text.literal(moveName), true);
        
        Vec3d direction = user.getRotationVector();
        double distance = isTransformed ? 12.0 : 8.0; // Increased range in Gear 5
        Vec3d impactPos = user.getPos().add(direction.multiply(distance));
        
        if (world instanceof ServerWorld serverWorld) {
            // Huge explosion at impact point - bigger in Gear 5
            float explosionPower = isTransformed ? 4.0f : 3.0f;
            world.createExplosion(user, impactPos.x, impactPos.y, impactPos.z, explosionPower, World.ExplosionSourceType.NONE);
            
            // Launch blocks everywhere around impact point
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    for (int y = -2; y <= 2; y++) {
                        double offsetX = impactPos.x + x;
                        double offsetY = impactPos.y + y;
                        double offsetZ = impactPos.z + z;
                        
                        // Random chance to break blocks and create particles
                        if (world.random.nextFloat() < 0.25f) {
                            serverWorld.spawnParticles(ParticleTypes.EXPLOSION, 
                                offsetX, offsetY, offsetZ, 
                                5, 0.5, 0.5, 0.5, 0.1);
                        }
                    }
                }
            }
            
            // White sparky particles everywhere
            for (int i = 0; i < 40; i++) {
                double particleX = impactPos.x + (world.random.nextGaussian() * 5);
                double particleY = impactPos.y + (world.random.nextGaussian() * 3);
                double particleZ = impactPos.z + (world.random.nextGaussian() * 5);
                
                serverWorld.spawnParticles(ParticleTypes.END_ROD, 
                    particleX, particleY, particleZ, 
                    1, 0, 0, 0, 0.3);
                    
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, 
                    particleX, particleY, particleZ, 
                    2, 0.2, 0.2, 0.2, 0.1);
            }
            
            // Massive damage to all entities in huge radius - stronger in Gear 5
            double damageRadius = isTransformed ? 12.0 : 10.0; // Larger radius in Gear 5
            float baseDamage = isTransformed ? 30.0f : 24.0f; // Higher base damage in Gear 5
            double knockbackMultiplier = isTransformed ? 3.0 : 2.5; // More knockback in Gear 5
            
            world.getOtherEntities(user, user.getBoundingBox().expand(damageRadius), 
                entity -> entity instanceof LivingEntity && entity != user)
                .forEach(entity -> {
                    if (entity instanceof LivingEntity living) {
                        double entityDistance = living.getPos().distanceTo(impactPos);
                        if (entityDistance <= damageRadius) {
                            float damage = (float) (baseDamage * (1.0 - (entityDistance / damageRadius))); // Damage decreases with distance
                            living.damage(serverWorld, serverWorld.getDamageSources().explosion(user, user), damage);
                            
                            // Massive knockback
                            Vec3d knockback = living.getPos().subtract(impactPos).normalize().multiply(knockbackMultiplier);
                            living.addVelocity(knockback.x, Math.max(knockback.y, 2.0), knockback.z);
                            living.velocityModified = true;
                            
                            // Status effects - stronger in Gear 5
                            int effectDuration = isTransformed ? 240 : 180;
                            int effectLevel = isTransformed ? 1 : 0;
                            living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, effectDuration, effectLevel));
                            int slowDur = Math.max(60, effectDuration - 60);
                            int slowAmp = Math.max(0, effectLevel);
                            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, slowDur, slowAmp));
                        }
                    }
                });
        }
        
        // Multiple explosion sounds
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.0F, 0.5F);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.5F, 0.8F);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 1.0F, 0.3F);
    }

    // Transformation Abilities
    private static void gomuGomuGigant(World world, PlayerEntity user) {
        user.sendMessage(Text.literal("§6Gomu Gomu no Gigantii!"), true);
        
        // Create explosion around player
        if (world instanceof ServerWorld serverWorld) {
            world.createExplosion(user, user.getX(), user.getY(), user.getZ(), 4.0f, World.ExplosionSourceType.NONE);
            
            // Giant effect simulation with massive knockback
            world.getOtherEntities(user, user.getBoundingBox().expand(8.0), 
                entity -> entity instanceof LivingEntity && entity != user)
                .forEach(entity -> {
                    if (entity instanceof LivingEntity living) {
                        living.damage(serverWorld, serverWorld.getDamageSources().playerAttack(user), 20.0f);
                        Vec3d knockback = living.getPos().subtract(user.getPos()).normalize().multiply(3.0);
                        living.addVelocity(knockback.x, 2.0, knockback.z);
                        living.velocityModified = true;
                    }
                });
        }
        
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 1.0F, 0.5F);
    }

    private static void gomuGomuLightning(World world, PlayerEntity user) {
        user.sendMessage(Text.literal("§6Gomu Gomu no Lightning!"), true);
        
        Vec3d direction = user.getRotationVector();
        double maxDistance = 60.0;
        net.minecraft.util.hit.HitResult hit = user.raycast(maxDistance, 1.0F, false);
        Vec3d targetPos;
        if (hit != null && hit.getType() != net.minecraft.util.hit.HitResult.Type.MISS) {
            targetPos = hit.getPos();
        } else {
            targetPos = user.getPos().add(direction.multiply(maxDistance));
            if (world instanceof ServerWorld sw) {
                int topY = sw.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, (int)Math.floor(targetPos.x), (int)Math.floor(targetPos.z));
                targetPos = new Vec3d(targetPos.x, topY, targetPos.z);
            }
        }
        
        // Lightning strike where cursor is aimed - use real lightning entities and AoE damage
        if (world instanceof ServerWorld serverWorld) {
            // Spawn actual lightning bolt at target location
            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, 
                targetPos.x, targetPos.y + 15, targetPos.z, 
                75, 1.5, 8.0, 1.5, 0.3); // Particles from sky down
                
            // Lightning bolt animation effect - longer bolt
            for (int i = 0; i < 15; i++) {
                double y = targetPos.y + (15 - i);
                serverWorld.spawnParticles(ParticleTypes.END_ROD, 
                    targetPos.x, y, targetPos.z, 
                    8, 0.8, 0.2, 0.8, 0.15);
            }
            
            // Spawn actual lightning bolts at/around target so they hit and deal damage
            net.minecraft.entity.LightningEntity mainBolt = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(serverWorld, net.minecraft.entity.SpawnReason.TRIGGERED);
            if (mainBolt != null) {
                mainBolt.refreshPositionAfterTeleport(targetPos.x, targetPos.y, targetPos.z);
                serverWorld.spawnEntity(mainBolt);
            }
            for (int i = 0; i < 2; i++) {
                double ox = targetPos.x + (serverWorld.random.nextDouble() - 0.5) * 4.0;
                double oz = targetPos.z + (serverWorld.random.nextDouble() - 0.5) * 4.0;
                net.minecraft.entity.LightningEntity extra = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(serverWorld, net.minecraft.entity.SpawnReason.TRIGGERED);
                if (extra != null) {
                    extra.refreshPositionAfterTeleport(ox, targetPos.y, oz);
                    serverWorld.spawnEntity(extra);
                }
            }
            
            // Damage all entities near the lightning strike - expanded search area for aimable lightning
            Vec3d finalTargetPos = targetPos;
            world.getOtherEntities(user, user.getBoundingBox().expand(60.0), 
                entity -> entity instanceof LivingEntity && entity != user)
                .forEach(entity -> {
                    if (entity instanceof LivingEntity living) {
                        double distance = living.getPos().distanceTo(finalTargetPos);
                        if (distance <= 12.0) { // Larger lightning strike radius
                            float damage = (float) (30.0f * (1.0 - (distance / 12.0))); // Damage decreases with distance, higher damage
                            living.damage(serverWorld, serverWorld.getDamageSources().lightningBolt(), damage);
                            living.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 300, 0));
                            living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 1));
                            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1));
                            
                            // Lightning knockback effect - stronger
                            Vec3d knockback = living.getPos().subtract(finalTargetPos).normalize().multiply(3.0);
                            living.addVelocity(knockback.x, 1.5, knockback.z);
                            living.velocityModified = true;
                        }
                    }
                });
                
            // Ground impact effect with larger crater creation
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION, 
                targetPos.x, targetPos.y, targetPos.z, 
                20, 3.0, 2.0, 3.0, 0.0);
                
            // Create larger crater at impact point
            world.createExplosion(user, targetPos.x, targetPos.y, targetPos.z, 4.0f, World.ExplosionSourceType.NONE);
            
            // Electric residue particles
            for (int i = 0; i < 50; i++) {
                double particleX = targetPos.x + (world.random.nextGaussian() * 5.0);
                double particleY = targetPos.y + (world.random.nextGaussian() * 2.0);
                double particleZ = targetPos.z + (world.random.nextGaussian() * 5.0);
                
                serverWorld.spawnParticles(ParticleTypes.END_ROD, 
                    particleX, particleY, particleZ, 
                    1, 0, 0, 0, 0.2);
            }
        }
        
        world.playSound(null, targetPos.x, targetPos.y, targetPos.z,
            SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 2.5F, 1.0F);
        world.playSound(null, targetPos.x, targetPos.y, targetPos.z,
            SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS, 2.0F, 1.2F);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.5F, 1.5F);
    }

    private static void toonForce(World world, PlayerEntity user) {
        user.sendMessage(Text.literal("§6Toon Force!"), true);
        
        // Reality-bending effects
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 200, 0)); // 10 seconds
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 200, 3));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 2));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, 2));
        
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.ENCHANTED_HIT, 
                user.getX(), user.getY() + 1, user.getZ(), 
                50, 2.0, 2.0, 2.0, 0.2);
        }
        
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 2.0F);
    }

    private static void supremeHakiBarjanGun(World world, PlayerEntity user) {
        user.sendMessage(Text.literal("§f§l§oGEAR 5 SUPREME HAKI INFUSED BARJAN GUN!"), false);
        
        PlayerData data = getPlayerData(user);
        
        // No charging - instant full force activation
        data.chargingSupremeHaki = false; // Make sure charging is off
        user.sendMessage(Text.literal("§f§l§k§oMAXIMUM POWER!!!"), false);
        
        // Float the player upward for 3 seconds
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 60, 3)); // 3 seconds, strong levitation
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 120, 0)); // Safe landing
        user.addVelocity(0, 1.5, 0); // Initial upward boost
        user.velocityModified = true;
        
        // Set timer to execute ground slam after 3 seconds (60 ticks)
        data.chargeStartTime = world.getTime() + 60; // Use this as slam time
        data.chargingSupremeHaki = true; // Use this as slam pending flag
        
        if (world instanceof ServerWorld serverWorld) {
            // Floating effects - white particles around player
            serverWorld.spawnParticles(ParticleTypes.END_ROD, 
                user.getX(), user.getY() + 1, user.getZ(), 
                50, 2.0, 3.0, 2.0, 0.2);
                
            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, 
                user.getX(), user.getY() + 1, user.getZ(), 
                30, 1.5, 2.0, 1.5, 0.15);
                
            // Lightning effects around player while floating
            for (int i = 0; i < 8; i++) {
                double angle = (2 * Math.PI * i) / 8;
                double radius = 3.0;
                double x = user.getX() + radius * Math.cos(angle);
                double z = user.getZ() + radius * Math.sin(angle);
                
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, 
                    x, user.getY() + 1, z, 
                    5, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 2.0F, 1.5F);
    }
    
    // =========================
    // DARKxQUAKE MOVES
    // =========================
    private static void dxqBlackVortex(World world, PlayerEntity user) {
        user.sendMessage(Text.literal("§8Black Vortex!"), true);
        int level = getMasteryLevel(user);
        double radius = 6.0 + Math.min(level, 100) * 0.04; // scale up to +4 blocks
        if (world instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.SMOKE, user.getX(), user.getY() + 1, user.getZ(), 40, radius/2, 1.0, radius/2, 0.05);
            sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1.0F, 0.6F);
            world.getOtherEntities(user, user.getBoundingBox().expand(radius), e -> e instanceof LivingEntity && e != user)
                .forEach(e -> {
                    LivingEntity le = (LivingEntity)e;
                    Vec3d dir = user.getPos().subtract(le.getPos()).normalize();
                    le.addVelocity(dir.x * 0.6, 0.2, dir.z * 0.6);
                    le.velocityModified = true;
                    le.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1)); // Slowness II for 3s (60 ticks)
                });
        }
    }

    private static void dxqQuakeFist(World world, PlayerEntity user) {
        user.sendMessage(Text.literal("§fQuake Fist!"), true);
        if (!(world instanceof ServerWorld sw)) return;
        int level = getMasteryLevel(user);
        double length = 8.0 + Math.min(level, 100) * 0.05; // up to +5 blocks
        Vec3d dir = user.getRotationVector().normalize();
        Vec3d start = user.getPos().add(0, user.getStandingEyeHeight(), 0);
        for (double t = 1; t <= length; t += 0.5) {
            Vec3d p = start.add(dir.multiply(t));
            sw.spawnParticles(ParticleTypes.EXPLOSION, p.x, p.y, p.z, 1, 0.1, 0.1, 0.1, 0.0);
        }
        // Damage entities along the line
        world.getOtherEntities(user, user.getBoundingBox().expand(length), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le = (LivingEntity) e;
                Vec3d to = le.getPos().add(0, le.getStandingEyeHeight()/2, 0).subtract(start);
                double proj = to.dotProduct(dir);
                if (proj > 0 && proj < length) {
                    double dist = to.subtract(dir.multiply(proj)).length();
                    if (dist < 2.0) {
                        le.damage(sw, sw.getDamageSources().playerAttack(user), 12.0f);
                        Vec3d kb = dir.multiply(1.5);
                        le.addVelocity(kb.x, 0.6, kb.z);
                        le.velocityModified = true;
                    }
                }
            });
        // Break weak blocks by a small non-flaming explosion at the end
        Vec3d end = start.add(dir.multiply(length));
        world.createExplosion(user, end.x, end.y, end.z, 2.0f, World.ExplosionSourceType.NONE);
        sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.PLAYERS, 1.0F, 0.7F);
    }

    private static void dxqDarkCage(World world, PlayerEntity user) {
        user.sendMessage(Text.literal("§8Dark Cage!"), true);
        if (!(world instanceof ServerWorld sw)) return;
        double r = 12.0;
        LivingEntity target = sw.getOtherEntities(user, user.getBoundingBox().expand(r), e -> e instanceof LivingEntity && e != user)
            .stream().map(e -> (LivingEntity)e).min((a,b)-> Double.compare(a.squaredDistanceTo(user), b.squaredDistanceTo(user))).orElse(null);
        if (target != null) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 4)); // 3s immobilize-ish
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 1));
            // Draw a particle sphere around target
            for (int i=0;i<48;i++){
                double ang = (2*Math.PI*i)/48.0;
                double x = target.getX()+Math.cos(ang)*2.0;
                double z = target.getZ()+Math.sin(ang)*2.0;
                sw.spawnParticles(ParticleTypes.SMOKE, x, target.getY()+1, z, 3, 0.1,0.4,0.1,0.01);
            }
            sw.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.PLAYERS, 1.0F, 0.6F);
        }
    }

    private static void dxqSeismicBurst(World world, PlayerEntity user) {
        user.sendMessage(Text.literal("§fSeismic Burst!"), true);
        if (!(world instanceof ServerWorld sw)) return;
        int level = getMasteryLevel(user);
        double radius = 6.0 + Math.min(level, 100) * 0.04;
        world.createExplosion(user, user.getX(), user.getY(), user.getZ(), 3.0f, World.ExplosionSourceType.NONE);
        sw.spawnParticles(ParticleTypes.EXPLOSION, user.getX(), user.getY(), user.getZ(), 20, 1.0, 0.2, 1.0, 0.0);
        sw.getOtherEntities(user, user.getBoundingBox().expand(radius), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le = (LivingEntity)e;
                Vec3d kb = le.getPos().subtract(user.getPos()).normalize().multiply(1.5);
                le.damage(sw, sw.getDamageSources().playerAttack(user), 10.0f);
                le.addVelocity(kb.x, 1.0, kb.z);
                le.velocityModified = true;
            });
        sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.2F, 0.9F);
    }

    private static void dxqGravityCrush(World world, PlayerEntity user) {
        user.sendMessage(Text.literal("§8Gravity Crush!"), true);
        if (!(world instanceof ServerWorld sw)) return;
        int level = getMasteryLevel(user);
        double radius = 6.0 + Math.min(level, 100) * 0.05;
        // Pull
        sw.getOtherEntities(user, user.getBoundingBox().expand(radius), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le = (LivingEntity)e;
                Vec3d dir = user.getPos().subtract(le.getPos()).normalize();
                le.addVelocity(dir.x * 0.6, 0.2, dir.z * 0.6);
                le.velocityModified = true;
            });
        // Detonate
        world.createExplosion(user, user.getX(), user.getY(), user.getZ(), 4.0f, World.ExplosionSourceType.NONE);
        sw.getOtherEntities(user, user.getBoundingBox().expand(radius), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le = (LivingEntity)e;
                le.damage(sw, sw.getDamageSources().explosion(user, user), 18.0f);
                Vec3d up = le.getPos().subtract(user.getPos()).normalize().multiply(2.0);
                le.addVelocity(up.x, 1.8, up.z);
                le.velocityModified = true;
            });
        sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.0F, 0.6F);
    }

    private static void dxqDarkMatterSlam(World world, PlayerEntity user) {
        user.sendMessage(Text.literal("§8Dark Matter Slam!"), true);
        if (!(world instanceof ServerWorld sw)) return;
        user.addVelocity(0, 1.2, 0);
        user.velocityModified = true;
        Vec3d pos = user.getPos();
        world.createExplosion(user, pos.x, pos.y, pos.z, 5.0f, World.ExplosionSourceType.NONE);
        sw.spawnParticles(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 30, 1.8, 0.4, 1.8, 0.0);
        sw.getOtherEntities(user, user.getBoundingBox().expand(8.0), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le = (LivingEntity)e;
                le.damage(sw, sw.getDamageSources().explosion(user, user), 22.0f);
                Vec3d kb = le.getPos().subtract(pos).normalize().multiply(2.5);
                le.addVelocity(kb.x, 1.2, kb.z);
                le.velocityModified = true;
            });
        sw.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 1.0F, 0.5F);
    }

    private static void dxqCataclysm(World world, PlayerEntity user) {
        user.sendMessage(Text.literal("§8§lDarkxQuake Cataclysm!"), false);
        if (!(world instanceof ServerWorld sw)) return;
        Vec3d pos = user.getPos();
        world.createExplosion(user, pos.x, pos.y, pos.z, 8.0f, World.ExplosionSourceType.NONE);
        for (int ring = 1; ring <= 4; ring++) {
            double rad = ring * 5.0;
            int count = ring * 16;
            for (int i=0;i<count;i++){
                double a = (2*Math.PI*i)/count;
                double x = pos.x + Math.cos(a)*rad;
                double z = pos.z + Math.sin(a)*rad;
                sw.spawnParticles(ParticleTypes.EXPLOSION, x, pos.y, z, 3, 0.2, 0.2, 0.2, 0.05);
                sw.spawnParticles(ParticleTypes.WHITE_ASH, x, pos.y+1, z, 2, 0.2, 0.2, 0.2, 0.01);
            }
        }
        // Heavier cooldown
        setCurrentFruitMoveCooldown(user, getPlayerData(user).currentMoveIndex, world.getTime() + 400);
        sw.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.0F, 0.5F);
    }

    private static void dxqEventHorizonQuake(World world, PlayerEntity user) {
        user.sendMessage(Text.literal("§8§lEvent Horizon Quake!"), false);
        if (!(world instanceof ServerWorld sw)) return;
        double r = 12.0;
        // Massive pull
        sw.getOtherEntities(user, user.getBoundingBox().expand(r), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le = (LivingEntity)e;
                Vec3d dir = user.getPos().subtract(le.getPos()).normalize();
                le.addVelocity(dir.x * 1.2, 0.1, dir.z * 1.2);
                le.velocityModified = true;
                le.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 3));
            });
        // Detonation
        world.createExplosion(user, user.getX(), user.getY(), user.getZ(), 6.0f, World.ExplosionSourceType.NONE);
        sw.getOtherEntities(user, user.getBoundingBox().expand(r), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> ((LivingEntity)e).damage(sw, sw.getDamageSources().explosion(user, user), 28.0f));
        sw.spawnParticles(ParticleTypes.EXPLOSION, user.getX(), user.getY(), user.getZ(), 60, 2.0, 0.5, 2.0, 0.0);
        setCurrentFruitMoveCooldown(user, getPlayerData(user).currentMoveIndex, world.getTime() + 2400); // 2 min major cooldown
        sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.3F, 0.5F);
    }

    private static void dxqSeaSplitter(World world, PlayerEntity user) {
        user.sendMessage(Text.literal("§fSea Splitter!"), true);
        if (!(world instanceof ServerWorld sw)) return;
        Vec3d dir = user.getRotationVector().normalize();
        Vec3d base = user.getPos();
        for (int i = 1; i <= 10; i++) {
            Vec3d p = base.add(dir.multiply(i * 2.0));
            world.createExplosion(user, p.x, p.y, p.z, 2.5f, World.ExplosionSourceType.NONE);
            sw.spawnParticles(ParticleTypes.EXPLOSION, p.x, p.y, p.z, 6, 0.5, 0.2, 0.5, 0.0);
        }
        sw.getOtherEntities(user, user.getBoundingBox().expand(16.0), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le = (LivingEntity)e;
                Vec3d to = le.getPos().subtract(base);
                double proj = to.dotProduct(dir);
                if (proj > 0 && proj < 20.0) {
                    le.damage(sw, sw.getDamageSources().playerAttack(user), 16.0f);
                    le.addVelocity(0, 1.2, 0);
                    le.velocityModified = true;
                }
            });
        setCurrentFruitMoveCooldown(user, getPlayerData(user).currentMoveIndex, world.getTime() + 200);
        sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.2F, 1.1F);
    }

    private static void dxqVoidImplosion(World world, PlayerEntity user) {
        user.sendMessage(Text.literal("§8Void Implosion!"), true);
        if (!(world instanceof ServerWorld sw)) return;
        double r = 10.0;
        sw.getOtherEntities(user, user.getBoundingBox().expand(r), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le = (LivingEntity)e;
                le.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 4));
                le.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 80, 3));
                le.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, 2));
                // Near-instant kill on low HP
                if (le.getHealth() <= 10.0f) {
                    le.damage(sw, sw.getDamageSources().outOfWorld(), Float.MAX_VALUE);
                } else {
                    le.damage(sw, sw.getDamageSources().magic(), 24.0f);
                }
            });
        sw.spawnParticles(ParticleTypes.EXPLOSION, user.getX(), user.getY(), user.getZ(), 30, 1.0, 0.4, 1.0, 0.0);
        setCurrentFruitMoveCooldown(user, getPlayerData(user).currentMoveIndex, world.getTime() + 400);
        sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WITHER_HURT, SoundCategory.PLAYERS, 1.0F, 0.4F);
    }

    private static void dxqCataclysmSupreme(World world, PlayerEntity user) {
        // This shares the same charge pattern as Supreme Haki; will slam in checkTransformationExpiry
        
        user.sendMessage(Text.literal("§8§lCATAcLYSM SUPREME!"), false);
        PlayerData data = getPlayerData(user);
        data.chargingSupremeHaki = true;
        data.chargeStartTime = world.getTime() + 60; // 3s float
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 60, 2));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 120, 0));
        if (world instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.SMOKE, user.getX(), user.getY() + 1, user.getZ(), 50, 1.5, 1.5, 1.5, 0.05);
            sw.spawnParticles(ParticleTypes.END_ROD, user.getX(), user.getY() + 1, user.getZ(), 40, 1.2, 1.2, 1.2, 0.1);
            sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_ROAR, SoundCategory.PLAYERS, 1.2F, 0.6F);
        }
    }

    // =========================
    // QUAKE-QUAKE MOVES
    // =========================
    private static void quakeTremorPunch(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§fTremor Punch!"), true);
        incrementQuakeAction(user);
        double r = 3.0;
        sw.getOtherEntities(user, user.getBoundingBox().expand(r), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le = (LivingEntity)e;
                le.damage(sw, sw.getDamageSources().playerAttack(user), 14.0f);
                Vec3d kb = le.getPos().subtract(user.getPos()).normalize().multiply(2.0);
                le.addVelocity(kb.x, 0.8, kb.z);
                le.velocityModified = true;
            });
        sw.spawnParticles(ParticleTypes.EXPLOSION, user.getX(), user.getY(), user.getZ(), 12, 0.6, 0.2, 0.6, 0.0);
        sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 1.0F, 0.6F);
    }

    private static void quakeShockwavePush(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§fShockwave Push!"), true);
        incrementQuakeAction(user);
        Vec3d dir = user.getRotationVector().normalize();
        double coneLen = 10.0;
        double coneWidth = 0.8; // cosine threshold
        sw.getOtherEntities(user, user.getBoundingBox().expand(coneLen), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le = (LivingEntity)e;
                Vec3d to = le.getPos().subtract(user.getPos()).normalize();
                if (to.dotProduct(dir) > coneWidth) {
                    le.damage(sw, sw.getDamageSources().playerAttack(user), 10.0f);
                    Vec3d kb = dir.multiply(2.8);
                    le.addVelocity(kb.x, 0.5, kb.z);
                    le.velocityModified = true;
                }
            });
        for (int i=1;i<=8;i++){
            Vec3d p = user.getPos().add(dir.multiply(i*1.5));
            sw.spawnParticles(ParticleTypes.EXPLOSION, p.x, p.y, p.z, 2, 0.2, 0.1, 0.2, 0.0);
        }
        sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0F, 1.2F);
    }

    private static void quakeSeismicSmash(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§fSeismic Smash!"), true);
        incrementQuakeAction(user);
        world.createExplosion(user, user.getX(), user.getY(), user.getZ(), 3.5f, World.ExplosionSourceType.NONE);
        sw.getOtherEntities(user, user.getBoundingBox().expand(7.0), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le = (LivingEntity)e;
                le.damage(sw, sw.getDamageSources().playerAttack(user), 16.0f);
                Vec3d up = le.getPos().subtract(user.getPos()).normalize().multiply(1.2);
                le.addVelocity(up.x, 1.5, up.z);
                le.velocityModified = true;
            });
        sw.spawnParticles(ParticleTypes.EXPLOSION, user.getX(), user.getY(), user.getZ(), 20, 1.0, 0.2, 1.0, 0.0);
    }

    private static void quakeSeaQuake(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§fSea Quake!"), true);
        incrementQuakeAction(user);
        Vec3d dir = user.getRotationVector().normalize();
        Vec3d base = user.getPos();
        for (int i=1;i<=12;i++){
            Vec3d p = base.add(dir.multiply(i*2.0));
            world.createExplosion(user, p.x, p.y, p.z, 3.0f, World.ExplosionSourceType.NONE);
            sw.spawnParticles(ParticleTypes.EXPLOSION, p.x, p.y, p.z, 4, 0.4, 0.2, 0.4, 0.0);
        }
        sw.getOtherEntities(user, user.getBoundingBox().expand(18.0), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le = (LivingEntity)e;
                Vec3d to = le.getPos().subtract(base);
                double proj = to.dotProduct(dir);
                if (proj > 0 && proj < 24.0) {
                    le.damage(sw, sw.getDamageSources().playerAttack(user), 18.0f);
                    le.addVelocity(0, 1.0, 0);
                    le.velocityModified = true;
                }
            });
    }

    private static void quakeAirquake(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§fAirquake!"), true);
        incrementQuakeAction(user);
        Vec3d dir = user.getRotationVector().normalize();
        Vec3d start = user.getPos().add(0, user.getStandingEyeHeight(), 0);
        for (int wave=0; wave<3; wave++){
            for (double t=2; t<=20; t+=2) {
                Vec3d p = start.add(dir.multiply(t + wave*0.5));
                sw.spawnParticles(ParticleTypes.WHITE_ASH, p.x, p.y, p.z, 3, 0.2,0.2,0.2, 0.01);
            }
        }
        sw.getOtherEntities(user, user.getBoundingBox().expand(20.0), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le=(LivingEntity)e;
                double dist = le.getPos().distanceTo(user.getPos());
                if (dist <= 20.0) {
                    le.damage(sw, sw.getDamageSources().playerAttack(user), 14.0f);
                    Vec3d kb = le.getPos().subtract(user.getPos()).normalize().multiply(1.8);
                    le.addVelocity(kb.x, 0.8, kb.z);
                    le.velocityModified = true;
                }
            });
    }

    private static void quakeCrackDome(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§fCrack Dome!"), true);
        incrementQuakeAction(user);
        double r=10.0;
        for (int i=0;i<36;i++){
            double a=(2*Math.PI*i)/36;
            sw.spawnParticles(ParticleTypes.WHITE_ASH, user.getX()+Math.cos(a)*r, user.getY()+1, user.getZ()+Math.sin(a)*r, 6, 0.2,0.4,0.2,0.01);
        }
        sw.getOtherEntities(user, user.getBoundingBox().expand(r), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le=(LivingEntity)e;
                le.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 2));
                le.damage(sw, sw.getDamageSources().playerAttack(user), 22.0f);
            });
    }

    // =========================
    // DARK-DARK MOVES
    // =========================
    private static void darkBlackVortex(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§8Black Vortex!"), true);
        incrementDarkSlowAction(user);
        double r=6.0;
        sw.getOtherEntities(user, user.getBoundingBox().expand(r), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le=(LivingEntity)e;
                Vec3d dir = user.getPos().subtract(le.getPos()).normalize();
                le.addVelocity(dir.x*0.7, 0.2, dir.z*0.7);
                le.velocityModified = true;
                le.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 3));
            });
        sw.spawnParticles(ParticleTypes.SMOKE, user.getX(), user.getY()+1, user.getZ(), 30, 0.8,0.6,0.8,0.05);
        sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1.0F, 0.5F);
    }

    private static void darkDarkMatterGrab(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§8Dark Matter Grab!"), true);
        incrementDarkSlowAction(user);
        double r=12.0;
        LivingEntity target = sw.getOtherEntities(user, user.getBoundingBox().expand(r), e-> e instanceof LivingEntity && e!=user)
            .stream().map(e->(LivingEntity)e).min((a,b)-> Double.compare(a.squaredDistanceTo(user), b.squaredDistanceTo(user))).orElse(null);
        if (target!=null){
            Vec3d dir = user.getPos().subtract(target.getPos()).normalize();
            target.addVelocity(dir.x*1.2, 0.4, dir.z*1.2);
            target.velocityModified = true;
            target.damage(sw, sw.getDamageSources().magic(), 14.0f);
            sw.spawnParticles(ParticleTypes.SMOKE, target.getX(), target.getY()+1, target.getZ(), 20, 0.6,0.6,0.6,0.05);
            sw.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 0.6F);
        }
    }

    private static void darkBlackHole(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§8Black Hole!"), true);
        incrementDarkSlowAction(user);
        double r=10.0;
        sw.getOtherEntities(user, user.getBoundingBox().expand(r), e-> e instanceof LivingEntity && e!=user)
            .forEach(e -> {
                LivingEntity le=(LivingEntity)e;
                Vec3d dir = user.getPos().subtract(le.getPos()).normalize();
                le.addVelocity(dir.x*0.9, 0.15, dir.z*0.9);
                le.velocityModified = true;
                le.damage(sw, sw.getDamageSources().magic(), 8.0f);
            });
        sw.spawnParticles(ParticleTypes.SMOKE, user.getX(), user.getY()+1, user.getZ(), 50, 1.2, 1.0, 1.2, 0.08);
        sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.0F, 0.5F);
    }

    private static void darkDarknessWave(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§8Darkness Wave!"), true);
        incrementDarkSlowAction(user);
        Vec3d dir = user.getRotationVector().normalize();
        for (int i=1;i<=10;i++){
            Vec3d p = user.getPos().add(dir.multiply(i*1.5));
            sw.spawnParticles(ParticleTypes.SMOKE, p.x, p.y+1, p.z, 4, 0.3,0.3,0.3,0.05);
        }
        sw.getOtherEntities(user, user.getBoundingBox().expand(12.0), e-> e instanceof LivingEntity && e!=user)
            .forEach(e -> {
                LivingEntity le=(LivingEntity)e;
                double dp = le.getPos().subtract(user.getPos()).normalize().dotProduct(dir);
                if (dp>0.5){
                    le.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1));
                    le.damage(sw, sw.getDamageSources().magic(), 10.0f);
                }
            });
    }

    private static void darkGravityCrush(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§8Gravity Crush!"), true);
        incrementDarkSlowAction(user);
        double r=8.0;
        sw.getOtherEntities(user, user.getBoundingBox().expand(r), e-> e instanceof LivingEntity && e!=user)
            .forEach(e -> {
                LivingEntity le=(LivingEntity)e;
                Vec3d dir = user.getPos().subtract(le.getPos()).normalize();
                le.addVelocity(dir.x*0.8, 0.2, dir.z*0.8);
                le.velocityModified = true;
            });
        world.createExplosion(user, user.getX(), user.getY(), user.getZ(), 3.5f, World.ExplosionSourceType.NONE);
        sw.getOtherEntities(user, user.getBoundingBox().expand(r), e-> e instanceof LivingEntity && e!=user)
            .forEach(e -> ((LivingEntity)e).damage(sw, sw.getDamageSources().explosion(user, user), 16.0f));
    }

    private static void darkNightShroud(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§8Night Shroud!"), true);
        // Grant powerful utility boons
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1200, 0)); // 60s Night Vision
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 1200, 0)); // 60s Fire Resistance
        // Subtle dark particles and a low hum
        sw.spawnParticles(ParticleTypes.SMOKE, user.getX(), user.getY() + 1, user.getZ(), 20, 0.6, 0.6, 0.6, 0.04);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 0.8F, 0.6F);
    }

    private static void darkEternalNightfall(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        // Darkness descends: pull, blind, weaken, then implode
        double r = 14.0;
        // Ambient darkness cloud
        sw.spawnParticles(ParticleTypes.SMOKE, user.getX(), user.getY() + 1, user.getZ(), 120, 2.5, 2.5, 2.5, 0.08);
        sw.spawnParticles(ParticleTypes.WHITE_ASH, user.getX(), user.getY() + 1, user.getZ(), 40, 2.0, 2.0, 2.0, 0.02);
        sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1.4F, 0.5F);
        sw.getOtherEntities(user, user.getBoundingBox().expand(r), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le = (LivingEntity)e;
                // Pull toward center
                Vec3d dir = user.getPos().subtract(le.getPos()).normalize();
                le.addVelocity(dir.x * 0.9, 0.1, dir.z * 0.9);
                le.velocityModified = true;
                // Harsh debuffs
                le.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0));
                le.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 1));
                le.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 2));
            });
        // Central implosion
        world.createExplosion(user, user.getX(), user.getY(), user.getZ(), 4.5f, World.ExplosionSourceType.NONE);
        sw.getOtherEntities(user, user.getBoundingBox().expand(r), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> ((LivingEntity)e).damage(sw, sw.getDamageSources().magic(), 20.0f));
        sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.PLAYERS, 1.2F, 0.5F);
    }

    // =========================
    // DRAGON-DRAGON MOVES
    // =========================
    private static void dragonHeatBreath(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§cHeat Breath!"), true);
        long now = world.getTime();
        PlayerData d = getPlayerData(user);
        int radius = 12;
        Vec3d dir = user.getRotationVector().normalize();
        // Flames in a wide cone
        for (int i = 0; i < 40; i++) {
            double yaw = (world.random.nextDouble() - 0.5) * Math.toRadians(50);
            double pitch = (world.random.nextDouble() - 0.5) * Math.toRadians(10);
            Vec3d spread = new Vec3d(dir.x, dir.y, dir.z)
                .rotateY((float)yaw)
                .rotateX((float)pitch)
                .multiply(1.0 + world.random.nextDouble()*0.5);
            Vec3d p = user.getPos().add(0, user.getStandingEyeHeight()*0.8, 0).add(spread.multiply(2));
            sw.spawnParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 4, 0.1,0.1,0.1, 0.02);
        }
        // Damage and ignite entities in front
        sw.getOtherEntities(user, user.getBoundingBox().expand(radius), e -> e instanceof LivingEntity && e != user).forEach(e -> {
            LivingEntity le = (LivingEntity)e;
            Vec3d to = le.getPos().subtract(user.getPos()).normalize();
            if (to.dotProduct(dir) > 0.25) {
                le.setOnFireFor(5);
                le.damage(sw, sw.getDamageSources().playerAttack(user), 10.0f);
            }
        });
        d.dragonMoveCooldowns[1] = now + maybeReduceForDragon(user, 20L * 20L); // 20s (index 1: Heat Breath)
    }

    private static void dragonThunderBagua(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§6Thunder Bagua Smash!"), true);
        long now = world.getTime();
        PlayerData d = getPlayerData(user);
        Vec3d dir = user.getRotationVector().normalize();
        // Dash forward
        user.addVelocity(dir.x * 2.5, 0.2, dir.z * 2.5);
        user.velocityModified = true;
        // Hit in a short line
        sw.getOtherEntities(user, user.getBoundingBox().expand(6.0), e -> e instanceof LivingEntity && e != user).forEach(e -> {
            LivingEntity le = (LivingEntity)e;
            Vec3d to = le.getPos().subtract(user.getPos()).normalize();
            if (to.dotProduct(dir) > 0.3) {
                le.damage(sw, sw.getDamageSources().playerAttack(user), 16.0f);
                Vec3d kb = dir.multiply(2.5);
                le.addVelocity(kb.x, 0.8, kb.z);
                le.velocityModified = true;
            }
        });
        sw.spawnParticles(ParticleTypes.EXPLOSION, user.getX(), user.getY(), user.getZ(), 15, 0.6,0.2,0.6, 0.0);
        sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0F, 1.1F);
        d.dragonMoveCooldowns[2] = now + maybeReduceForDragon(user, 20L * 25L); // 25s (index 2: Thunder Bagua)
    }

    private static void dragonTwister(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§bDragon Twister!"), true);
        long now = world.getTime();
        PlayerData d = getPlayerData(user);
        double r = 6.0;
        // Spiral particles
        for (int i = 0; i < 40; i++) {
            double ang = (2*Math.PI*i)/20.0;
            double y = i * 0.1;
            double x = user.getX() + Math.cos(ang)*r*(i/40.0);
            double z = user.getZ() + Math.sin(ang)*r*(i/40.0);
            sw.spawnParticles(ParticleTypes.CLOUD, x, user.getY()+y, z, 2, 0.02,0.02,0.02, 0.02);
        }
        // Launch enemies up
        sw.getOtherEntities(user, user.getBoundingBox().expand(r), e -> e instanceof LivingEntity && e != user).forEach(e -> {
            LivingEntity le = (LivingEntity)e;
            le.addVelocity(0, 1.2, 0);
            le.velocityModified = true;
            le.damage(sw, sw.getDamageSources().playerAttack(user), 8.0f);
        });
        d.dragonMoveCooldowns[3] = now + maybeReduceForDragon(user, 20L * 30L); // 30s (index 3: Dragon Twister)
    }

    private static void dragonFlameClouds(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§6Flame Clouds!"), true);
        long now = world.getTime();
        PlayerData d = getPlayerData(user);
        // 20s of graceful movement
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 20*20, 0));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 20*20, 2));
        // fiery trail
        for (int i=0;i<30;i++) sw.spawnParticles(ParticleTypes.FLAME, user.getX(), user.getY()+0.2, user.getZ(), 2, 0.4,0.2,0.4, 0.01);
        d.dragonMoveCooldowns[4] = now + maybeReduceForDragon(user, 20L * 40L); // 40s (index 4: Flame Clouds)
    }

    private static void dragonRoar(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§eDragon's Roar!"), true);
        long now = world.getTime();
        PlayerData d = getPlayerData(user);
        double r = 10.0;
        sw.spawnParticles(ParticleTypes.SONIC_BOOM, user.getX(), user.getY()+1, user.getZ(), 1, 0,0,0, 0.0);
        sw.getOtherEntities(user, user.getBoundingBox().expand(r), e -> e instanceof LivingEntity && e != user).forEach(e -> {
            LivingEntity le = (LivingEntity)e;
            Vec3d kb = le.getPos().subtract(user.getPos()).normalize().multiply(2.5);
            le.addVelocity(kb.x, 0.8, kb.z);
            le.velocityModified = true;
            le.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20*5, 0));
            le.damage(sw, sw.getDamageSources().playerAttack(user), 10.0f);
        });
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.0F, 0.6F);
        d.dragonMoveCooldowns[5] = now + maybeReduceForDragon(user, 20L * 45L); // 45s (index 5: Dragon's Roar)
    }

    private static void dragonSlash(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§aDragon Slash!"), true);
        long now = world.getTime();
        PlayerData d = getPlayerData(user);
        Vec3d dir = user.getRotationVector().normalize();
        // Sweeping arc visual
        for (int i = -6; i <= 6; i++) {
            double angle = Math.toRadians(i * 5);
            Vec3d base = new Vec3d(dir.x, 0, dir.z);
            Vec3d rotated = base.rotateY((float) angle).normalize();
            Vec3d p = user.getPos().add(0, user.getStandingEyeHeight() * 0.6, 0).add(rotated.multiply(3.0));
            sw.spawnParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 3, 0.1, 0.1, 0.1, 0.02);
        }
        // Damage in a narrow forward cone
        double coneLen = 8.0; double coneDot = 0.7;
        sw.getOtherEntities(user, user.getBoundingBox().expand(coneLen), e -> e instanceof LivingEntity && e != user)
            .forEach(e -> {
                LivingEntity le = (LivingEntity) e;
                Vec3d to = le.getPos().subtract(user.getPos()).normalize();
                if (to.dotProduct(dir) > coneDot && le.squaredDistanceTo(user) <= coneLen * coneLen) {
                    le.damage(sw, sw.getDamageSources().playerAttack(user), 18.0f);
                    Vec3d kb = dir.multiply(2.2);
                    le.addVelocity(kb.x, 0.6, kb.z);
                    le.velocityModified = true;
                }
            });
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0F, 0.9F);
        // Per-move cooldown, reduced if transformed
        d.dragonMoveCooldowns[0] = now + maybeReduceForDragon(user, 20L * 35L); // 35s (index 0: Dragon Slash)
    }

    // =========================
    // OPERATION (OPE OPE) MOVES
    // =========================
    private static int getOpRoomRadius(PlayerEntity p) {
        PlayerData d = getPlayerData(p);
        long now = p.getWorld().getTime();
        return now < d.opRoomEndTime ? 30 : 10;
    }

    private static void opRoom(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        long now = world.getTime();
        PlayerData d = getPlayerData(user);
        int idx = d.currentMoveIndex;
        // Cooldown check is handled before, set cooldown now
        d.opRoomEndTime = now + 20L * 30L; // 30s active room
        d.opRoomRadius = 30;
        d.operationMoveCooldowns[idx] = now + 20L * 15L; // 15s CD
        // Particles ring
        int ring = 48; double r = 30;
        for (int i=0;i<ring;i++){
            double a=(2*Math.PI*i)/ring; double x=user.getX()+Math.cos(a)*r; double z=user.getZ()+Math.sin(a)*r;
            sw.spawnParticles(ParticleTypes.END_ROD, x, user.getY()+0.2, z, 1, 0.05,0.05,0.05, 0.01);
        }
        user.sendMessage(Text.literal("§3Room expanded!"), true);
    }

    private static void opShambles(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        long now = world.getTime();
        PlayerData d = getPlayerData(user);
        int rad = getOpRoomRadius(user);
        if (rad <= 10) { user.sendMessage(Text.literal("§7Use Room first."), true); return; }
        LivingEntity target = sw.getOtherEntities(user, user.getBoundingBox().expand(rad), e-> e instanceof LivingEntity && e != user)
            .stream().map(e->(LivingEntity)e).min((a,b)-> Double.compare(a.squaredDistanceTo(user), b.squaredDistanceTo(user))).orElse(null);
        if (target == null) { user.sendMessage(Text.literal("§7No target inside Room."), true); return; }
        Vec3d up = new Vec3d(user.getX(), user.getY(), user.getZ());
        Vec3d tp = new Vec3d(target.getX(), target.getY(), target.getZ());
        user.requestTeleport(tp.x, tp.y, tp.z);
        target.requestTeleport(up.x, up.y, up.z);
        sw.spawnParticles(ParticleTypes.POOF, up.x, up.y+1, up.z, 12, 0.4,0.4,0.4, 0.1);
        sw.spawnParticles(ParticleTypes.POOF, tp.x, tp.y+1, tp.z, 12, 0.4,0.4,0.4, 0.1);
        d.operationMoveCooldowns[1] = now + 20L * 20L; // 20s
    }

    private static void opScan(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        long now = world.getTime();
        PlayerData d = getPlayerData(user);
        int rad = getOpRoomRadius(user);
        for (var e : sw.getOtherEntities(user, user.getBoundingBox().expand(rad), en-> en instanceof LivingEntity && en != user)) {
            ((LivingEntity)e).addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 20*10, 0));
        }
        sw.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.PLAYERS, 0.8F, 1.5F);
        d.operationMoveCooldowns[2] = now + 20L * 25L; // 25s
    }

    private static void opInjectionShot(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        long now = world.getTime();
        PlayerData d = getPlayerData(user);
        int rad = getOpRoomRadius(user);
        if (rad <= 10) { user.sendMessage(Text.literal("§7Use Room first."), true); return; }
        user.sendMessage(Text.literal("§3Injection Shot!"), true);
        // Beam like Soul Bolt but stronger
        double max = 30.0; Vec3d dir = user.getRotationVector().normalize(); Vec3d start = user.getPos().add(0, user.getStandingEyeHeight(), 0);
        LivingEntity hit = null; double best = max+1;
        for (LivingEntity le : sw.getEntitiesByClass(LivingEntity.class, user.getBoundingBox().expand(rad), e-> e != user)) {
            Vec3d to = le.getPos().add(0, le.getStandingEyeHeight()*0.5, 0).subtract(start);
            double proj = to.dotProduct(dir);
            if (proj > 0 && proj <= max) {
                double off = to.subtract(dir.multiply(proj)).length();
                if (off < 1.0 && proj < best) { best = proj; hit = le; }
            }
        }
        for (double t=0; t<Math.min(max, best); t+=0.5){ Vec3d p = start.add(dir.multiply(t)); sw.spawnParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 2, 0.02,0.02,0.02, 0.0);}        
        if (hit != null) { hit.damage(sw, sw.getDamageSources().magic(), 16.0f); }
        d.operationMoveCooldowns[3] = now + 20L * 30L; // 30s
    }

    private static void opTact(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        long now = world.getTime();
        PlayerData d = getPlayerData(user);
        int rad = getOpRoomRadius(user);
        if (rad <= 10) { user.sendMessage(Text.literal("§7Use Room first."), true); return; }
        user.sendMessage(Text.literal("§3Tact!"), true);
        for (var e : sw.getOtherEntities(user, user.getBoundingBox().expand(rad), en -> en instanceof LivingEntity && en != user)) {
            LivingEntity le = (LivingEntity)e;
            le.addVelocity(0, 1.2, 0); le.velocityModified = true;
            le.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 0));
        }
        d.operationMoveCooldowns[4] = now + 20L * 35L; // 35s
    }

    private static void opGammaKnife(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        long now = world.getTime();
        PlayerData d = getPlayerData(user);
        int rad = getOpRoomRadius(user);
        if (rad <= 10) { user.sendMessage(Text.literal("§7Use Room first."), true); return; }
        LivingEntity target = sw.getOtherEntities(user, user.getBoundingBox().expand(rad), e-> e instanceof LivingEntity && e != user)
            .stream().map(e->(LivingEntity)e).min((a,b)-> Double.compare(a.squaredDistanceTo(user), b.squaredDistanceTo(user))).orElse(null);
        if (target == null) { user.sendMessage(Text.literal("§7No target."), true); return; }
        float dmg = 18.0f;
        if (target instanceof PlayerEntity pe && DevilFruitRegistry.hasAnyFruit(pe)) dmg *= 2.0f; // double vs devil fruit users
        target.damage(sw, sw.getDamageSources().outOfWorld(), dmg); // true damage-like
        sw.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getY()+1, target.getZ(), 20, 0.4,0.4,0.4, 0.1);
        d.operationMoveCooldowns[5] = now + 20L * 45L; // 45s
    }

    // New method to execute the ground slam
    // =========================
    // SOUL-SOUL MOVES
    // =========================
    private static void soulBolt(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§bSoul Bolt!"), true);
        double max = 25.0;
        Vec3d dir = user.getRotationVector().normalize();
        Vec3d start = user.getPos().add(0, user.getStandingEyeHeight(), 0);
        LivingEntity hit = null;
        double hitDist = max + 1;
        for (LivingEntity le : sw.getEntitiesByClass(LivingEntity.class, user.getBoundingBox().expand(max), e -> e != user)) {
            Vec3d to = le.getPos().add(0, le.getStandingEyeHeight() * 0.5, 0).subtract(start);
            double proj = to.dotProduct(dir);
            if (proj > 0 && proj <= max) {
                double distLine = to.subtract(dir.multiply(proj)).length();
                if (distLine < 1.2 && proj < hitDist) { hitDist = proj; hit = le; }
            }
        }
        for (double t = 0; t < Math.min(max, hitDist); t += 0.5) {
            Vec3d p = start.add(dir.multiply(t));
            sw.spawnParticles(ParticleTypes.SOUL, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.0);
            sw.spawnParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (hit != null) {
            hit.damage(sw, sw.getDamageSources().magic(), 7.0f); // buffed from 5
            hit.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1)); // 2s Slowness II
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PHANTOM_AMBIENT, SoundCategory.PLAYERS, 0.7F, 1.6F);
        setSoulCooldownForCurrent(user, world.getTime() + 80); // 4s (per-move)
    }

    private static void spiritGuard(World world, PlayerEntity user) {
        if (world.isClient) return;
        PlayerData d = getPlayerData(user);
        long now = world.getTime();
        d.soulGuardEndTime = now + 120; // 6s
        d.soulGuardHitsRemaining = 30;  // 3 shields x 10 HP
        if (world instanceof ServerWorld sw) {
            for (int i = 0; i < 3; i++) {
                double ang = (2 * Math.PI * i) / 3.0;
                double r = 1.5;
                sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, user.getX() + Math.cos(ang) * r, user.getY() + 1, user.getZ() + Math.sin(ang) * r, 15, 0.2, 0.2, 0.2, 0.01);
            }
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 0.4F);
        setSoulCooldownForCurrent(user, world.getTime() + 300); // 15s (per-move)
    }

    private static void wraithPush(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§bWraith Push!"), true);
        double r = 6.0;
        sw.spawnParticles(ParticleTypes.SOUL, user.getX(), user.getY() + 1, user.getZ(), 40, 1.2, 0.2, 1.2, 0.02);
        sw.getOtherEntities(user, user.getBoundingBox().expand(r), e -> e instanceof LivingEntity && e != user).forEach(e -> {
            LivingEntity le = (LivingEntity) e;
            Vec3d kb = le.getPos().subtract(user.getPos()).normalize().multiply(1.8);
            le.addVelocity(kb.x, 0.5, kb.z);
            le.velocityModified = true;
            le.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0)); // 3s Weak I
            le.damage(sw, sw.getDamageSources().magic(), 9.0f); // buffed from 7
        });
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.8F, 1.2F);
        setSoulCooldownForCurrent(user, world.getTime() + 240); // 12s (per-move)
    }

    private static void phantomBind(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        LivingEntity target = sw.getOtherEntities(user, user.getBoundingBox().expand(12.0), e -> e instanceof LivingEntity && e != user)
            .stream().map(e -> (LivingEntity) e).min((a, b) -> Double.compare(a.squaredDistanceTo(user), b.squaredDistanceTo(user))).orElse(null);
        if (target == null) { user.sendMessage(Text.literal("§7No target."), true); return; }
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 6)); // near-root 3s
        sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1, target.getZ(), 30, 0.6, 0.6, 0.6, 0.02);
        world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.PLAYERS, 1.0F, 0.6F);
        setSoulCooldownForCurrent(user, world.getTime() + 360); // 18s (per-move)
    }

    private static void spiritBarrage(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        user.sendMessage(Text.literal("§bSpirit Barrage!"), true);
        for (int i = 0; i < 5; i++) {
            soulBolt(world, user);
        }
        setSoulCooldownForCurrent(user, world.getTime() + 300); // 15s (per-move)
    }

    private static void soulSteal(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        PlayerData d = getPlayerData(user);
        long now = world.getTime();
        if (now < d.soulStealCooldown) {
            long sec = (d.soulStealCooldown - now) / 20;
            user.sendMessage(Text.literal("§cSoul Steal on cooldown: " + sec + "s"), true);
            return;
        }
        LivingEntity target = sw.getOtherEntities(user, user.getBoundingBox().expand(8.0), e -> e instanceof LivingEntity && e != user)
            .stream().map(e -> (LivingEntity) e).min((a, b) -> Double.compare(a.squaredDistanceTo(user), b.squaredDistanceTo(user))).orElse(null);
        if (target == null) { user.sendMessage(Text.literal("§7No target to steal."), true); return; }
        d.soulStealCooldown = now + 20L * 60L * 30L; // 30m
        // Buffs for 5 minutes
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 20 * 60 * 5, 0));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20 * 60 * 5, 1));
        // Visuals & sound
        for (int i = 0; i < 20; i++) {
            double t = i / 20.0;
            Vec3d p = target.getPos().lerp(user.getPos(), t).add(0, 0.8, 0);
            sw.spawnParticles(ParticleTypes.SOUL, p.x, p.y, p.z, 2, 0.02, 0.02, 0.02, 0.0);
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1.0F, 0.7F);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_GHAST_SCREAM, SoundCategory.PLAYERS, 0.5F, 1.8F);
        // Borrow ability metadata (lightweight)
        if (target instanceof PlayerEntity tp) {
            // Operation Fruit Soul Protection
            if (DevilFruitRegistry.playerOwnsFruit(tp, DevilFruitRegistry.FRUIT_OPERATION)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 20 * 5, 1)); // Wither II 5s
                user.sendMessage(Text.literal("§cSoul Protection! Your Soul Steal was repelled."), true);
                return;
            }
            int before = d.soulStolenPlayers;
            d.soulStolenPlayers = Math.min(5, d.soulStolenPlayers + 1);
            if (before < 3 && d.soulStolenPlayers >= 3 && DevilFruitRegistry.playerOwnsFruit(user, DevilFruitRegistry.FRUIT_SOUL)) {
                if (!hasAdvancement(user, "grim_soul_keeper")) {
                    grantAdvancement(user, "grim_soul_keeper");
                    user.sendMessage(Text.literal("§bChallenge Complete: The Grim Soul Keeper"), false);
                }
            }
            d.soulBorrowEndTime = now + 20L * 60L * 25L; // 25m
            d.soulBorrowedFruit = DevilFruitRegistry.getPlayerFruit(tp);
            d.soulBorrowedMove = "One ability copied";
            user.sendMessage(Text.literal("§bSoul copied from " + tp.getName().getString() + "!"), false);

            // Immediate soul execution with inventory preservation override
            if (tp instanceof ServerPlayerEntity sp) {
                // Save a deep copy of all items (main, armor, offhand)
                java.util.List<ItemStack> saved = new ArrayList<>();
                // main inventory
                for (int i = 0; i < sp.getInventory().size(); i++) {
                    ItemStack st = sp.getInventory().getStack(i);
                    if (!st.isEmpty()) saved.add(st.copy());
                }
                // armor
                ItemStack head = sp.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD);
                ItemStack chest = sp.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);
                ItemStack legs = sp.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS);
                ItemStack feet = sp.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET);
                if (!head.isEmpty()) saved.add(head.copy());
                if (!chest.isEmpty()) saved.add(chest.copy());
                if (!legs.isEmpty()) saved.add(legs.copy());
                if (!feet.isEmpty()) saved.add(feet.copy());
                // offhand
                ItemStack off = sp.getOffHandStack();
                if (!off.isEmpty()) saved.add(off.copy());
                SOUL_SAVED_INVENTORY.put(sp.getUuid(), saved);
                // Clear all to prevent drops
                sp.getInventory().clear();
                sp.equipStack(net.minecraft.entity.EquipmentSlot.HEAD, ItemStack.EMPTY);
                sp.equipStack(net.minecraft.entity.EquipmentSlot.CHEST, ItemStack.EMPTY);
                sp.equipStack(net.minecraft.entity.EquipmentSlot.LEGS, ItemStack.EMPTY);
                sp.equipStack(net.minecraft.entity.EquipmentSlot.FEET, ItemStack.EMPTY);
                sp.setStackInHand(net.minecraft.util.Hand.OFF_HAND, ItemStack.EMPTY);
                // Prevent drops by clearing now
                sp.getInventory().clear();
                // Kill the player instantly (bypasses armor/totems)
                sp.damage(sp.getServerWorld(), sp.getDamageSources().outOfWorld(), Float.MAX_VALUE);
            }
        } else {
            // Non-player target: instantly kill
            target.damage(sw, sw.getDamageSources().outOfWorld(), Float.MAX_VALUE);
        }
        setSoulCooldownForCurrent(user, world.getTime() + 60); // minor activation delay (per-move)
    }

    private static void soulArmy(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        // Approximation: strike 6 nearest enemies for 3 damage repeatedly (single burst)
        var list = sw.getOtherEntities(user, user.getBoundingBox().expand(15.0), e -> e instanceof LivingEntity && e != user)
            .stream().map(e -> (LivingEntity)e).sorted((a,b)-> Double.compare(a.squaredDistanceTo(user), b.squaredDistanceTo(user))).limit(6).toList();
        for (LivingEntity le : list) {
            le.damage(sw, sw.getDamageSources().magic(), 9.0f); // 3 warriors x 3 damage per hit equivalent
            sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, le.getX(), le.getY()+1, le.getZ(), 20, 0.3,0.3,0.3,0.02);
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.PLAYERS, 1.0F, 0.6F);
        setSoulCooldownForCurrent(user, world.getTime() + 20 * 90); // 90s (per-move)
    }

    private static void lifeDrain(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        double r = 8.0;
        float total = 0f;
        for (var e : sw.getOtherEntities(user, user.getBoundingBox().expand(r), en -> en instanceof LivingEntity && en != user)) {
            LivingEntity le = (LivingEntity) e;
            le.damage(sw, sw.getDamageSources().magic(), 10.0f);
            total += 10.0f;
            sw.spawnParticles(ParticleTypes.SOUL, le.getX(), le.getY()+1, le.getZ(), 12, 0.2,0.3,0.2, 0.01);
        }
        if (total > 0) {
            float heal = total * 0.5f;
            user.heal(heal);
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1.2F, 0.5F);
        setSoulCooldownForCurrent(user, world.getTime() + 20 * 120); // 120s (per-move)
    }

    private static void soulDominance(World world, PlayerEntity user) {
        if (!(world instanceof ServerWorld sw)) return;
        double r = 10.0;
        sw.spawnParticles(ParticleTypes.SOUL, user.getX(), user.getY()+1, user.getZ(), 100, 2.0,1.0,2.0, 0.05);
        for (var e : sw.getOtherEntities(user, user.getBoundingBox().expand(r), en -> en instanceof LivingEntity && en != user)) {
            LivingEntity le = (LivingEntity) e;
            le.damage(sw, sw.getDamageSources().outOfWorld(), 10.0f);
            le.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 4));
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.5F, 0.9F);
        setSoulCooldownForCurrent(user, world.getTime() + 20 * 180); // 180s (per-move)
    }

    public static boolean tryBlockProjectileWithSoulGuard(PlayerEntity player, String sourceName) {
        PlayerData d = getPlayerData(player);
        long now = player.getWorld().getTime();
        boolean proj = sourceName.contains("arrow") || sourceName.contains("projectile") || sourceName.contains("trident") || sourceName.contains("fireball");
        if (DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_SOUL) && now < d.soulGuardEndTime && proj && d.soulGuardHitsRemaining > 0) {
            d.soulGuardHitsRemaining--;
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 0.6F, 1.8F);
            return true;
        }
        return false;
    }

    public static void executeSupremeHakiSlam(World world, PlayerEntity user) {
        if (world instanceof ServerWorld serverWorld) {
            Vec3d impactPos = user.getPos();
            
            // MASSIVE EXPLOSION at ground impact
            world.createExplosion(user, impactPos.x, impactPos.y - 1, impactPos.z, 8.0f, World.ExplosionSourceType.NONE);
            
            // Create huge crater - break blocks in large area
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    for (int y = -4; y <= 1; y++) {
                        double distance = Math.sqrt(x*x + y*y + z*z);
                        if (distance <= 8.0 && world.random.nextFloat() < 0.3f) {
                            double offsetX = impactPos.x + x;
                            double offsetY = impactPos.y + y - 1;
                            double offsetZ = impactPos.z + z;
                            
                            // Massive particle explosion
                            serverWorld.spawnParticles(ParticleTypes.EXPLOSION, 
                                offsetX, offsetY, offsetZ, 
                                12, 1.5, 1.5, 1.5, 0.0);
                        }
                    }
                }
            }
            
            // Shockwave effect - rings of particles expanding outward
            for (int ring = 1; ring <= 5; ring++) {
                double radius = ring * 4.0;
                int particleCount = ring * 8;
                for (int i = 0; i < particleCount; i++) {
                    double angle = (2 * Math.PI * i) / particleCount;
                    double x = impactPos.x + radius * Math.cos(angle);
                    double z = impactPos.z + radius * Math.sin(angle);
                    
                    serverWorld.spawnParticles(ParticleTypes.EXPLOSION, 
                        x, impactPos.y, z, 
                        5, 0.5, 0.5, 0.5, 0.1);
                    serverWorld.spawnParticles(ParticleTypes.END_ROD, 
                        x, impactPos.y + 1, z, 
                        3, 0.3, 0.3, 0.3, 0.05);
                }
            }
            
            // Lightning strikes everywhere around impact
            for (int i = 0; i < 30; i++) {
                double randomX = impactPos.x + (world.random.nextGaussian() * 15);
                double randomY = impactPos.y + (world.random.nextGaussian() * 5);
                double randomZ = impactPos.z + (world.random.nextGaussian() * 15);
                
                // Lightning bolt effects
                for (int j = 0; j < 20; j++) {
                    double y = randomY + (20 - j);
                    serverWorld.spawnParticles(ParticleTypes.END_ROD, 
                        randomX, y, randomZ, 
                        5, 0.2, 0.1, 0.2, 0.05);
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, 
                        randomX, y, randomZ, 
                        8, 0.5, 0.1, 0.5, 0.1);
                }
            }
            
            // Catastrophic damage to all entities in massive radius
            double damageRadius = 22.0; // PvP-friendly radius
            world.getOtherEntities(user, user.getBoundingBox().expand(damageRadius), 
                entity -> entity instanceof LivingEntity && entity != user)
                .forEach(entity -> {
                    if (entity instanceof LivingEntity living) {
                        double distance = living.getPos().distanceTo(impactPos);
                        if (distance <= damageRadius) {
                            // Immense damage scaled by distance
                            float maxDamage = 60.0f; // PvP-friendly max damage
                            float damage = (float) (maxDamage * (1.0 - (distance / damageRadius)));
                            living.damage(serverWorld, serverWorld.getDamageSources().explosion(user, user), damage);
                            
                            // Extreme knockback from shockwave
                            Vec3d knockback = living.getPos().subtract(impactPos).normalize().multiply(10.0);
                            living.addVelocity(knockback.x, Math.max(knockback.y, 4.0), knockback.z);
                            living.velocityModified = true;
                            
                            // Devastating status effects
                            living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 1800, 4)); // 1.5 minutes, level 5
                            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 1200, 3)); // 1 minute, level 4
                            living.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 1000, 2)); // 50 seconds, level 3
                            living.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 600, 0)); // 30 seconds
                        }
                    }
                });
                
            // Massive white sparky particles everywhere
            for (int i = 0; i < 120; i++) {
                double particleX = impactPos.x + (world.random.nextGaussian() * 20);
                double particleY = impactPos.y + (world.random.nextGaussian() * 10);
                double particleZ = impactPos.z + (world.random.nextGaussian() * 20);
                
                serverWorld.spawnParticles(ParticleTypes.END_ROD, 
                    particleX, particleY, particleZ, 
                    1, 0, 0, 0, 0.7);
                    
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, 
                    particleX, particleY, particleZ, 
                    4, 0.5, 0.5, 0.5, 0.3);
                    
                serverWorld.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, 
                    particleX, particleY, particleZ, 
                    3, 0.8, 0.8, 0.8, 0.4);
            }
        }
        
        // Epic sound effects for ground impact
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 4.0F, 0.1F);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 4.0F, 0.3F);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 4.0F, 0.2F);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 3.0F, 0.1F);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 2.0F, 0.5F);
    }

    private static void activateTransformation(PlayerEntity user, World world) {
        if (world.isClient) return;
        
        PlayerData data = getPlayerData(user);
        setTransformed(user, true);
        setWhiteHair(user, true);
        
        // Set transformation timer (15 minutes = 18000 ticks)
        data.transformationEndTime = world.getTime() + 18000;
        data.canReactivateUlt = false; // Cannot reactivate until it naturally expires
        data.damageTaken = 0.0f; // Reset rage (taken)
        data.damageDealt = 0.0f;  // Reset dealt
        
        boolean isDxQ = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(user, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE);
        boolean isQuake = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(user, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE);
        boolean isDark = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(user, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARK);
        if (isDxQ) {
            user.sendMessage(Text.literal("§8§lTHE TRUE MENACE — DARKxQUAKE AWAKENING ACTIVATED!"), false);
            // Passive buffs per design
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 18000, 1)); // Absorption II
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 18000, 1)); // Speed II
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 18000, 1)); // Strength II
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 18000, 0));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 18000, 0));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 18000, 0)); // Knockback resistance approx
            if (world instanceof ServerWorld serverWorld) {
                // Black mist + white quake cracks
                serverWorld.spawnParticles(ParticleTypes.SMOKE, user.getX(), user.getY() + 1, user.getZ(), 80, 1.8, 2.0, 1.8, 0.08);
                serverWorld.spawnParticles(ParticleTypes.END_ROD, user.getX(), user.getY() + 1, user.getZ(), 50, 1.5, 1.5, 1.5, 0.2);
                // Dark fog hands around
                for (int i = 0; i < 8; i++) {
                    double angle = (2 * Math.PI * i) / 8.0;
                    double r = 2.0;
                    serverWorld.spawnParticles(ParticleTypes.SMOKE, user.getX() + Math.cos(angle) * r, user.getY() + 1, user.getZ() + Math.sin(angle) * r, 6, 0.2, 0.6, 0.2, 0.01);
                }
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_ROAR, SoundCategory.PLAYERS, 1.2F, 0.6F);
        } else if (isQuake) {
            user.sendMessage(Text.literal("§f§lTREMOR AWAKENING ACTIVATED!"), false);
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 18000, 2));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 18000, 0));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 18000, 1));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 18000, 0));
            if (world instanceof ServerWorld sw) {
                // White crack aura
                sw.spawnParticles(ParticleTypes.END_ROD, user.getX(), user.getY()+1, user.getZ(), 60, 1.4, 1.4, 1.4, 0.2);
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 1.0F, 0.8F);
        } else if (isDark) {
            user.sendMessage(Text.literal("§8§lDARKNESS AWAKENING ACTIVATED!"), false);
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 18000, 1));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 18000, 0));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 18000, 0));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 18000, 0));
            if (world instanceof ServerWorld sw) {
                // Dark fog
                sw.spawnParticles(ParticleTypes.SMOKE, user.getX(), user.getY()+1, user.getZ(), 80, 1.8, 2.0, 1.8, 0.08);
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1.0F, 0.6F);
        } else {
            user.sendMessage(Text.literal("§f§lGEAR 5 - NIKA AWAKENING ACTIVATED!"), false);
            user.sendMessage(Text.literal("§fYou now have access to 3 god-level moves for 15 minutes!"), false);
            
            // Balanced transformation effects - good but not unbeatable
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 18000, 6)); // 15 minutes, reduced level
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 18000, 1)); // Reduced level
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 18000, 1)); // Keep same
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 18000, 0));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 18000, 1)); // Keep same
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 18000, 0));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 18000, 1)); // Reduced resistance
            
            if (world instanceof ServerWorld serverWorld) {
                // White sparks and particles
                serverWorld.spawnParticles(ParticleTypes.END_ROD, 
                    user.getX(), user.getY() + 1, user.getZ(), 
                    50, 1.0, 1.5, 1.0, 0.1);
                
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, 
                    user.getX(), user.getY() + 1, user.getZ(), 
                    75, 1.5, 2.0, 1.5, 0.2);
                    
                serverWorld.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, 
                    user.getX(), user.getY() + 1, user.getZ(), 
                    100, 2.0, 2.0, 2.0, 0.3);
                
                // White cloud effects
                serverWorld.spawnParticles(ParticleTypes.CLOUD, 
                    user.getX(), user.getY() + 0.5, user.getZ(), 
                    30, 1.0, 0.5, 1.0, 0.1);
            }
            
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1.0F, 0.8F);
            
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.8F, 1.5F);
        }
    }

    // Helper methods using the static data storage
    private static PlayerData getPlayerData(PlayerEntity player) {
        return playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData());
    }

    private static boolean isDevilFruitUser(PlayerEntity player) {
        // Treat any devil fruit holder as a "devil fruit user" for shared UI/controls
        return getPlayerData(player).devilFruitUser || nika_nika_fruit_v7tnmscy.DevilFruitRegistry.hasAnyFruit(player);
    }

    private static void makeDevilFruitUser(PlayerEntity player) {
        PlayerData data = getPlayerData(player);
        data.devilFruitUser = true;
        data.damageTaken = 0.0f;
        data.transformationActive = false;
        data.currentMoveIndex = 0;
        data.hasWhiteHair = false;
    }

    private static boolean canTransform(PlayerEntity player) {
        // For DarkxQuake, awakening uses alternate conditions (hits+kills), so always return true here
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE)) {
            return true;
        }
        // Nika no longer needs damage gates once unlocked
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_NIKA)) {
            return true;
        }
        // Dragon: only requirement is Mastery 250 (handled in canTransformToGear5)
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DRAGON)) {
            return true;
        }
        PlayerData data = getPlayerData(player);
        return data.damageTaken >= DAMAGE_TAKEN_REQUIRED && data.damageDealt >= DAMAGE_DEALT_REQUIRED;
    }

    private static boolean isTransformed(PlayerEntity player) {
        return getPlayerData(player).transformationActive;
    }

    private static void setTransformed(PlayerEntity player, boolean transformed) {
        getPlayerData(player).transformationActive = transformed;
    }

    private static void setWhiteHair(PlayerEntity player, boolean hasWhiteHair) {
        getPlayerData(player).hasWhiteHair = hasWhiteHair;
    }

    private static long getAbilityCooldown(PlayerEntity player) {
        return getPlayerData(player).abilityCooldown;
    }

    private static void setAbilityCooldown(PlayerEntity player, long cooldown) {
        // Backwards-compatible: keep a global timestamp but also stamp the selected move for the player's current fruit
        PlayerData d = getPlayerData(player);
        d.abilityCooldown = cooldown; // legacy global (not used for gating anymore)
        int idx = d.currentMoveIndex;
        setCurrentFruitMoveCooldown(player, idx, cooldown);
    }
    
    private static long getSoulCooldownForIndex(PlayerEntity player, int idx) {
        PlayerData d = getPlayerData(player);
        if (idx < 0 || idx >= d.soulMoveCooldowns.length) return 0L;
        return d.soulMoveCooldowns[idx];
    }
    
    private static void setSoulCooldownForCurrent(PlayerEntity player, long untilTick) {
        PlayerData d = getPlayerData(player);
        int idx = Math.max(0, Math.min(d.soulMoveCooldowns.length - 1, d.currentMoveIndex));
        d.soulMoveCooldowns[idx] = untilTick;
    }

    // Generic per-fruit per-move cooldown helpers
    private static long getCurrentFruitMoveCooldown(PlayerEntity player, int moveIdx) {
        PlayerData d = getPlayerData(player);
        String fruit = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.getPlayerFruit(player);
        long now = player.getWorld().getTime();
        if (fruit == null) return Math.max(0, d.nikaMoveCooldowns[Math.max(0, Math.min(moveIdx, d.nikaMoveCooldowns.length-1))] - now);
        if (DevilFruitRegistry.FRUIT_SOUL.equals(fruit)) return Math.max(0, d.soulMoveCooldowns[Math.max(0, Math.min(moveIdx, d.soulMoveCooldowns.length-1))] - now);
        if (DevilFruitRegistry.FRUIT_DRAGON.equals(fruit)) return Math.max(0, d.dragonMoveCooldowns[Math.max(0, Math.min(moveIdx, d.dragonMoveCooldowns.length-1))] - now);
        if (DevilFruitRegistry.FRUIT_OPERATION.equals(fruit)) return Math.max(0, d.operationMoveCooldowns[Math.max(0, Math.min(moveIdx, d.operationMoveCooldowns.length-1))] - now);
        if (DevilFruitRegistry.FRUIT_DARK.equals(fruit)) return Math.max(0, d.darkMoveCooldowns[Math.max(0, Math.min(moveIdx, d.darkMoveCooldowns.length-1))] - now);
        if (DevilFruitRegistry.FRUIT_QUAKE.equals(fruit)) return Math.max(0, d.quakeMoveCooldowns[Math.max(0, Math.min(moveIdx, d.quakeMoveCooldowns.length-1))] - now);
        if (DevilFruitRegistry.FRUIT_DARKXQUAKE.equals(fruit)) return Math.max(0, d.dxqMoveCooldowns[Math.max(0, Math.min(moveIdx, d.dxqMoveCooldowns.length-1))] - now);
        return Math.max(0, d.nikaMoveCooldowns[Math.max(0, Math.min(moveIdx, d.nikaMoveCooldowns.length-1))] - now);
    }
    private static void setCurrentFruitMoveCooldown(PlayerEntity player, int moveIdx, long untilTick) {
        PlayerData d = getPlayerData(player);
        String fruit = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.getPlayerFruit(player);
        if (fruit == null || DevilFruitRegistry.FRUIT_NIKA.equals(fruit)) {
            if (moveIdx >= 0 && moveIdx < d.nikaMoveCooldowns.length) d.nikaMoveCooldowns[moveIdx] = untilTick; return;
        }
        if (DevilFruitRegistry.FRUIT_SOUL.equals(fruit)) { if (moveIdx >= 0 && moveIdx < d.soulMoveCooldowns.length) d.soulMoveCooldowns[moveIdx] = untilTick; return; }
        if (DevilFruitRegistry.FRUIT_DRAGON.equals(fruit)) { if (moveIdx >= 0 && moveIdx < d.dragonMoveCooldowns.length) d.dragonMoveCooldowns[moveIdx] = untilTick; return; }
        if (DevilFruitRegistry.FRUIT_OPERATION.equals(fruit)) { if (moveIdx >= 0 && moveIdx < d.operationMoveCooldowns.length) d.operationMoveCooldowns[moveIdx] = untilTick; return; }
        if (DevilFruitRegistry.FRUIT_DARK.equals(fruit)) { if (moveIdx >= 0 && moveIdx < d.darkMoveCooldowns.length) d.darkMoveCooldowns[moveIdx] = untilTick; return; }
        if (DevilFruitRegistry.FRUIT_QUAKE.equals(fruit)) { if (moveIdx >= 0 && moveIdx < d.quakeMoveCooldowns.length) d.quakeMoveCooldowns[moveIdx] = untilTick; return; }
        if (DevilFruitRegistry.FRUIT_DARKXQUAKE.equals(fruit)) { if (moveIdx >= 0 && moveIdx < d.dxqMoveCooldowns.length) d.dxqMoveCooldowns[moveIdx] = untilTick; return; }
    }

    // Static helper methods for mixins and UI
    public static void addDamageTaken(PlayerEntity player, float damage) {
        PlayerData data = playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData());
        long now = player.getWorld().getTime();
        // Only count once per i-frame window (10 ticks default)
        if (now - data.lastDamageTickCounted >= 10) {
            data.damageTaken += damage;
            data.lastDamageTickCounted = now;
        }
    }

    public static void addDamageDealt(PlayerEntity player, float damage) {
        PlayerData data = playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData());
        data.damageDealt += damage;
    }
    
    // DarkxQuake awakening hit tracking (respect i-frames)
    public static void recordDxqHit(PlayerEntity player) {
        if (!nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE)) return;
        PlayerData data = getPlayerData(player);
        long now = player.getWorld().getTime();
        if (now - data.lastHitTickCounted >= 10) {
            data.dxqConsecutiveHits = Math.min(10, data.dxqConsecutiveHits + 1);
            data.lastHitTickCounted = now;
        }
    }
    
    public static void recordDxqPlayerKill(PlayerEntity player) {
        if (!nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE)) return;
        PlayerData data = getPlayerData(player);
        data.dxqPlayerKills = Math.min(3, data.dxqPlayerKills + 1);
    }
    
    public static void resetDxqCombo(PlayerEntity player) {
        PlayerData data = getPlayerData(player);
        data.dxqConsecutiveHits = 0;
        data.lastHitTickCounted = 0L;
    }
    
    public static int getDxqConsecutiveHits(PlayerEntity player) {
        return getPlayerData(player).dxqConsecutiveHits;
    }
    
    public static int getDxqPlayerKills(PlayerEntity player) {
        return getPlayerData(player).dxqPlayerKills;
    }
    
    // Quake/Dark challenge helpers
    public static void incrementQuakeAction(PlayerEntity player) {
        getPlayerData(player).quakeActions++;
    }
    public static int getQuakeActions(PlayerEntity player) {
        return getPlayerData(player).quakeActions;
    }
    public static void recordQuakeBossKill(PlayerEntity player) {
        PlayerData d = getPlayerData(player);
        if (!d.quakeBossKill) {
            d.quakeBossKill = true;
            player.sendMessage(Text.literal("§f§lTremor Trial Complete: Boss defeated!"), false);
        }
    }
    public static boolean hasQuakeBossKill(PlayerEntity player) {
        return getPlayerData(player).quakeBossKill;
    }
    public static boolean isQuakeUltimateReady(PlayerEntity player) {
        PlayerData d = getPlayerData(player);
        // New requirement: die to Drowned with a Trident 3 times AND die while having Dolphin's Grace 5 times OR reach Mastery 60
        return ((d.quakeDeathsToDrownedTrident >= 3 && d.quakeDeathsWithDolphin >= 5) || getMasteryLevel(player) >= 60);
    }
    
    public static void incrementDarkSlowAction(PlayerEntity player) {
        getPlayerData(player).darkSlowActions++;
    }
    public static int getDarkSlowActions(PlayerEntity player) {
        return getPlayerData(player).darkSlowActions;
    }
    public static void recordDarkPlayerKill(PlayerEntity player) {
        PlayerData d = getPlayerData(player);
        d.darkPlayerKills++;
        player.sendMessage(Text.literal("§8§lShadow Trial: Player soul claimed (" + d.darkPlayerKills + "/2)"), true);
    }
    public static int getDarkPlayerKillCount(PlayerEntity player) {
        return getPlayerData(player).darkPlayerKills;
    }
    public static boolean isDarkUltimateReady(PlayerEntity player) {
        PlayerData d = getPlayerData(player);
        // New requirement: die while under Darkness 5 times OR reach Mastery 55 (night-only cast remains)
        return (d.darkDarknessDeathCount >= 5) || (getMasteryLevel(player) >= 55);
    }

    // New counters and helpers for ult requirements
    public static void recordDeathWithDarkness(PlayerEntity player) {
        PlayerData d = getPlayerData(player);
        d.darkDarknessDeathCount = Math.min(5, d.darkDarknessDeathCount + 1);
        player.sendMessage(Text.literal("§8Darkness Deaths: " + d.darkDarknessDeathCount + "/5"), true);
    }
    public static int getDarkDarknessDeathCount(PlayerEntity player) {
        return getPlayerData(player).darkDarknessDeathCount;
    }

    public static void recordDeathToDrownedTrident(PlayerEntity player) {
        PlayerData d = getPlayerData(player);
        d.quakeDeathsToDrownedTrident = Math.min(3, d.quakeDeathsToDrownedTrident + 1);
        player.sendMessage(Text.literal("§fDrowned (Trident) Deaths: " + d.quakeDeathsToDrownedTrident + "/3"), true);
    }
    public static int getQuakeDrownedTridentDeaths(PlayerEntity player) {
        return getPlayerData(player).quakeDeathsToDrownedTrident;
    }

    public static void recordDeathWithDolphinsGrace(PlayerEntity player) {
        PlayerData d = getPlayerData(player);
        d.quakeDeathsWithDolphin = Math.min(5, d.quakeDeathsWithDolphin + 1);
        player.sendMessage(Text.literal("§fDolphin's Grace Deaths: " + d.quakeDeathsWithDolphin + "/5"), true);
    }
    public static int getQuakeDolphinDeaths(PlayerEntity player) {
        return getPlayerData(player).quakeDeathsWithDolphin;
    }

    public static int getSoulStolenPlayers(PlayerEntity player) {
        return getPlayerData(player).soulStolenPlayers;
    }

    public static boolean isPlayerDevilFruitUser(PlayerEntity player) {
        return playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData()).devilFruitUser;
    }

    public static boolean isPlayerTransformed(PlayerEntity player) {
        return playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData()).transformationActive;
    }
    
    public static boolean hasWhiteHair(PlayerEntity player) {
        return playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData()).hasWhiteHair;
    }

    public static void deactivateTransformation(PlayerEntity player) {
        PlayerData data = playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData());
        data.transformationActive = false;
        data.hasWhiteHair = false; // Hair returns to normal on death
        data.canReactivateUlt = true; // Allow reactivation after natural expiry or death
        data.chargingSupremeHaki = false; // Stop charging if active
        data.damageTaken = 0.0f; // Reset rage (taken)
        data.damageDealt = 0.0f;  // Reset dealt
    }
    
    // Check if transformation should expire and handle Supreme Haki ground slam
    public static void checkTransformationExpiry(PlayerEntity player) {
        if (player.getWorld().isClient) return;
        
        PlayerData data = getPlayerData(player);
        World world = player.getWorld();
        long currentTime = world.getTime();
        
        // Check for Supreme Haki ground slam
        if (data.chargingSupremeHaki && currentTime >= data.chargeStartTime) {
            // Time to execute ground slam
            data.chargingSupremeHaki = false;
            executeSupremeHakiSlam(world, player);
        }
        
        if (data.transformationActive && currentTime >= data.transformationEndTime) {
            // Transformation has expired
            data.transformationActive = false;
            data.hasWhiteHair = false;
            data.canReactivateUlt = true; // Allow reactivation after natural expiry
            data.chargingSupremeHaki = false; // Stop charging if active
            data.damageTaken = 0.0f; // Reset rage (taken)
            data.damageDealt = 0.0f;  // Reset dealt
            
            // Remove transformation effects
            player.removeStatusEffect(StatusEffects.ABSORPTION);
            player.removeStatusEffect(StatusEffects.JUMP_BOOST);
            player.removeStatusEffect(StatusEffects.SPEED);
            player.removeStatusEffect(StatusEffects.FIRE_RESISTANCE);
            player.removeStatusEffect(StatusEffects.STRENGTH);
            player.removeStatusEffect(StatusEffects.GLOWING);
            player.removeStatusEffect(StatusEffects.RESISTANCE);
            
            player.sendMessage(Text.literal("§c§lAwakening expired! Use Right Shift to reawaken when ready."), false);
            
            // Expiry effects
            if (player.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.POOF, 
                    player.getX(), player.getY() + 1, player.getZ(), 
                    30, 1.0, 1.0, 1.0, 0.1);
            }
            
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.5F, 0.5F);
        }
    }
    
    // Mastery system methods
    public static void addMasteryExp(PlayerEntity player, float exp) {
        if (!isDevilFruitUser(player)) return;
        
        PlayerData data = getPlayerData(player);
        String fruit = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.getPlayerFruit(player);
        if (fruit == null) return;
        
        if (DevilFruitRegistry.FRUIT_DARKXQUAKE.equals(fruit)) {
            data.masteryExpDxq += exp;
            data.expRequiredForNextLevelDxq = calcExpRequired(data.masteryLevelDxq);
            while (data.masteryExpDxq >= data.expRequiredForNextLevelDxq && data.masteryLevelDxq < 1000) {
                data.masteryExpDxq -= data.expRequiredForNextLevelDxq;
                data.masteryLevelDxq++;
                data.expRequiredForNextLevelDxq = calcExpRequired(data.masteryLevelDxq);
                player.sendMessage(Text.literal("§6§lMastery Level Up! Level " + data.masteryLevelDxq), false);
                player.sendMessage(Text.literal("§eNext level: " + String.format("%.0f", data.expRequiredForNextLevelDxq) + " EXP"), false);
                checkMoveUnlocks(player, data.masteryLevelDxq);
                if (player.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 20, 1.0, 1.0, 1.0, 0.1);
                }
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
            if (data.masteryLevelDxq >= 1000) { data.masteryLevelDxq = 1000; data.masteryExpDxq = 0; data.expRequiredForNextLevelDxq = calcExpRequired(1000); }
        } else if (DevilFruitRegistry.FRUIT_QUAKE.equals(fruit)) {
            data.masteryExpQuake += exp;
            data.expRequiredForNextLevelQuake = calcExpRequired(data.masteryLevelQuake);
            while (data.masteryExpQuake >= data.expRequiredForNextLevelQuake && data.masteryLevelQuake < 1000) {
                data.masteryExpQuake -= data.expRequiredForNextLevelQuake;
                data.masteryLevelQuake++;
                data.expRequiredForNextLevelQuake = calcExpRequired(data.masteryLevelQuake);
                player.sendMessage(Text.literal("§6§lMastery Level Up! Level " + data.masteryLevelQuake), false);
                player.sendMessage(Text.literal("§eNext level: " + String.format("%.0f", data.expRequiredForNextLevelQuake) + " EXP"), false);
                checkMoveUnlocks(player, data.masteryLevelQuake);
                if (player.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 20, 1.0, 1.0, 1.0, 0.1);
                }
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
            if (data.masteryLevelQuake >= 1000) { data.masteryLevelQuake = 1000; data.masteryExpQuake = 0; data.expRequiredForNextLevelQuake = calcExpRequired(1000); }
        } else if (DevilFruitRegistry.FRUIT_DARK.equals(fruit)) {
            data.masteryExpDark += exp;
            data.expRequiredForNextLevelDark = calcExpRequired(data.masteryLevelDark);
            while (data.masteryExpDark >= data.expRequiredForNextLevelDark && data.masteryLevelDark < 1000) {
                data.masteryExpDark -= data.expRequiredForNextLevelDark;
                data.masteryLevelDark++;
                data.expRequiredForNextLevelDark = calcExpRequired(data.masteryLevelDark);
                player.sendMessage(Text.literal("§6§lMastery Level Up! Level " + data.masteryLevelDark), false);
                player.sendMessage(Text.literal("§eNext level: " + String.format("%.0f", data.expRequiredForNextLevelDark) + " EXP"), false);
                checkMoveUnlocks(player, data.masteryLevelDark);
                if (player.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 20, 1.0, 1.0, 1.0, 0.1);
                }
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
            if (data.masteryLevelDark >= 1000) { data.masteryLevelDark = 1000; data.masteryExpDark = 0; data.expRequiredForNextLevelDark = calcExpRequired(1000); }
        } else if (DevilFruitRegistry.FRUIT_SOUL.equals(fruit)) {
            data.masteryExpSoul += exp;
            data.expRequiredForNextLevelSoul = calcExpRequired(data.masteryLevelSoul);
            while (data.masteryExpSoul >= data.expRequiredForNextLevelSoul && data.masteryLevelSoul < 1000) {
                data.masteryExpSoul -= data.expRequiredForNextLevelSoul;
                data.masteryLevelSoul++;
                data.expRequiredForNextLevelSoul = calcExpRequired(data.masteryLevelSoul);
                player.sendMessage(Text.literal("§6§lMastery Level Up! Level " + data.masteryLevelSoul), false);
                player.sendMessage(Text.literal("§eNext level: " + String.format("%.0f", data.expRequiredForNextLevelSoul) + " EXP"), false);
                checkMoveUnlocks(player, data.masteryLevelSoul);
                if (player.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 20, 1.0, 1.0, 1.0, 0.1);
                }
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
            if (data.masteryLevelSoul >= 1000) { data.masteryLevelSoul = 1000; data.masteryExpSoul = 0; data.expRequiredForNextLevelSoul = calcExpRequired(1000); }
        } else if (DevilFruitRegistry.FRUIT_DRAGON.equals(fruit)) {
            data.masteryExpDragon += exp;
            data.expRequiredForNextLevelDragon = calcExpRequired(data.masteryLevelDragon);
            while (data.masteryExpDragon >= data.expRequiredForNextLevelDragon && data.masteryLevelDragon < 1000) {
                data.masteryExpDragon -= data.expRequiredForNextLevelDragon;
                data.masteryLevelDragon++;
                data.expRequiredForNextLevelDragon = calcExpRequired(data.masteryLevelDragon);
                player.sendMessage(Text.literal("§6§lMastery Level Up! Level " + data.masteryLevelDragon), false);
                player.sendMessage(Text.literal("§eNext level: " + String.format("%.0f", data.expRequiredForNextLevelDragon) + " EXP"), false);
                checkMoveUnlocks(player, data.masteryLevelDragon);
                if (player.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 20, 1.0, 1.0, 1.0, 0.1);
                }
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
            if (data.masteryLevelDragon >= 1000) { data.masteryLevelDragon = 1000; data.masteryExpDragon = 0; data.expRequiredForNextLevelDragon = calcExpRequired(1000); }
        } else if (DevilFruitRegistry.FRUIT_OPERATION.equals(fruit)) {
            data.masteryExpOperation += exp;
            data.expRequiredForNextLevelOperation = calcExpRequired(data.masteryLevelOperation);
            while (data.masteryExpOperation >= data.expRequiredForNextLevelOperation && data.masteryLevelOperation < 1000) {
                data.masteryExpOperation -= data.expRequiredForNextLevelOperation;
                data.masteryLevelOperation++;
                data.expRequiredForNextLevelOperation = calcExpRequired(data.masteryLevelOperation);
                player.sendMessage(Text.literal("§6§lMastery Level Up! Level " + data.masteryLevelOperation), false);
                player.sendMessage(Text.literal("§eNext level: " + String.format("%.0f", data.expRequiredForNextLevelOperation) + " EXP"), false);
                checkMoveUnlocks(player, data.masteryLevelOperation);
                if (player.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 20, 1.0, 1.0, 1.0, 0.1);
                }
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
            if (data.masteryLevelOperation >= 1000) { data.masteryLevelOperation = 1000; data.masteryExpOperation = 0; data.expRequiredForNextLevelOperation = calcExpRequired(1000); }
        } else { // Nika
            data.masteryExpNika += exp;
            data.expRequiredForNextLevelNika = calcExpRequired(data.masteryLevelNika);
            while (data.masteryExpNika >= data.expRequiredForNextLevelNika && data.masteryLevelNika < 1000) {
                data.masteryExpNika -= data.expRequiredForNextLevelNika;
                data.masteryLevelNika++;
                data.expRequiredForNextLevelNika = calcExpRequired(data.masteryLevelNika);
                player.sendMessage(Text.literal("§6§lMastery Level Up! Level " + data.masteryLevelNika), false);
                player.sendMessage(Text.literal("§eNext level: " + String.format("%.0f", data.expRequiredForNextLevelNika) + " EXP"), false);
                checkMoveUnlocks(player, data.masteryLevelNika);
                if (player.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 20, 1.0, 1.0, 1.0, 0.1);
                }
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
            if (data.masteryLevelNika >= 1000) { data.masteryLevelNika = 1000; data.masteryExpNika = 0; data.expRequiredForNextLevelNika = calcExpRequired(1000); }
        }
    }
    
    private static void checkMoveUnlocks(PlayerEntity player, int level) {
        // Build dynamic unlock message based on the player's current fruit
        boolean showMajor = (level == 50 || level == 100);
        boolean transformed = true; // include awakened list when checking
        String[] names = getMoveNames(player, transformed);
        boolean any = false;
        for (int i = 0; i < names.length; i++) {
            if (getRequiredLevelFor(player, i) == level) {
                player.sendMessage(Text.literal("§a§lMove Unlocked: " + names[i] + "!"), false);
                any = true;
            }
        }
        // Generic milestone message if no specific move tied to this level
        if (!any) {
            player.sendMessage(Text.literal("§eMilestone reached: Level " + level), false);
        }
        // Nika-specific advancement at 50
        if (level == 200 && nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_NIKA)) {
            // Mastery 200 guarantees Joyboy unlock
            if (!hasAdvancement(player, "joyboy")) {
                grantAdvancement(player, "joyboy");
            }
        }
        // Generic mastery_X advancements if present
        grantAdvancement(player, "mastery_" + level);
        
        if (showMajor) {
            if (player.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 50, 2.0, 2.0, 2.0, 0.2);
                serverWorld.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1, player.getZ(), 30, 1.5, 1.5, 1.5, 0.15);
            }
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 2.0F, 1.0F);
        }
    }
    
    public static void addAttackExp(PlayerEntity player) {
        if (!isDevilFruitUser(player)) return;
        
        PlayerData data = getPlayerData(player);
        String fruit = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.getPlayerFruit(player);
        if (fruit == null) fruit = DevilFruitRegistry.FRUIT_NIKA;
        int usage;
        if (DevilFruitRegistry.FRUIT_DARKXQUAKE.equals(fruit)) {
            data.attackUsageCountDxq++;
            usage = data.attackUsageCountDxq;
        } else if (DevilFruitRegistry.FRUIT_QUAKE.equals(fruit)) {
            data.attackUsageCountQuake++;
            usage = data.attackUsageCountQuake;
        } else if (DevilFruitRegistry.FRUIT_DARK.equals(fruit)) {
            data.attackUsageCountDark++;
            usage = data.attackUsageCountDark;
        } else if (DevilFruitRegistry.FRUIT_SOUL.equals(fruit)) {
            data.attackUsageCountSoul++;
            usage = data.attackUsageCountSoul;
        } else if (DevilFruitRegistry.FRUIT_DRAGON.equals(fruit)) {
            data.attackUsageCountDragon++;
            usage = data.attackUsageCountDragon;
        } else if (DevilFruitRegistry.FRUIT_OPERATION.equals(fruit)) {
            data.attackUsageCountOperation++;
            usage = data.attackUsageCountOperation;
        } else {
            data.attackUsageCountNika++;
            usage = data.attackUsageCountNika;
        }
        
        // Progressive EXP: first attack = 5 exp, second = 10 exp, etc. but cap at 50 to prevent abuse
        float expGain = Math.min(3.0f * usage, 24.0f);
        addMasteryExp(player, expGain);
        
        // Reset count every 10 attacks to prevent infinite scaling
        if (usage >= 8) {
            if (DevilFruitRegistry.FRUIT_DARKXQUAKE.equals(fruit)) data.attackUsageCountDxq = 0;
            else if (DevilFruitRegistry.FRUIT_QUAKE.equals(fruit)) data.attackUsageCountQuake = 0;
            else if (DevilFruitRegistry.FRUIT_DARK.equals(fruit)) data.attackUsageCountDark = 0;
            else if (DevilFruitRegistry.FRUIT_SOUL.equals(fruit)) data.attackUsageCountSoul = 0;
            else if (DevilFruitRegistry.FRUIT_DRAGON.equals(fruit)) data.attackUsageCountDragon = 0;
            else if (DevilFruitRegistry.FRUIT_OPERATION.equals(fruit)) data.attackUsageCountOperation = 0;
            else data.attackUsageCountNika = 0;
        }
    }
    
    public static void addKillExp(PlayerEntity player, LivingEntity killedEntity) {
        if (!isDevilFruitUser(player)) return;
        
        // EXP based on entity max health, but with reasonable scaling
        float maxHealth = killedEntity.getMaxHealth();
        float expGain = Math.min(maxHealth * 1.0f, 120.0f); // 1.5 EXP per HP point, capped at 200
        
        // Minimum EXP for any kill
        expGain = Math.max(expGain, 8.0f);
        
        addMasteryExp(player, expGain);
        player.sendMessage(Text.literal("§e+" + String.format("%.0f", expGain) + " Mastery EXP from kill!"), true);
    }
    
    private static boolean canUseMove(PlayerEntity player, int moveIndex) {
        PlayerData data = getPlayerData(player);
        int level = getMasteryLevel(player);
        boolean isTransformed = data.transformationActive;
        
        // If DarkxQuake owner, use DarkxQuake progression
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE)) {
            return switch (moveIndex) {
                case 0 -> true; // Black Vortex
                case 1 -> level >= 3; // Quake Fist
                case 2 -> level >= 5; // Dark Cage
                case 3 -> level >= 10; // Seismic Burst
                case 4 -> level >= 15; // Gravity Crush
                case 5 -> level >= 20; // Dark Matter Slam
                case 6 -> level >= 25; // DarkxQuake Cataclysm (base ult)
                case 7 -> isTransformed && level >= 60; // Event Horizon Quake
                case 8 -> isTransformed && level >= 75; // Sea Splitter
                case 9 -> isTransformed && level >= 90; // Void Implosion
                case 10 -> isTransformed && level >= 100; // Cataclysm Supreme
                default -> false;
            };
        }
        // Fruit-specific move unlock progression
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE)) {
            // Quake-Quake
            return switch (moveIndex) {
                case 0 -> true;               // Tremor Punch
                case 1 -> level >= 5;         // Shockwave Push
                case 2 -> level >= 10;        // Seismic Smash
                case 3 -> level >= 26;        // Sea Quake
                case 4 -> level >= 51;        // Airquake
                case 5 -> level >= 75;        // Crack Dome
                default -> false;
            };
        }
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARK)) {
            // Dark-Dark
            return switch (moveIndex) {
                case 0 -> true;               // Black Vortex
                case 1 -> level >= 5;         // Dark Matter Grab
                case 2 -> level >= 26;        // Black Hole
                case 3 -> level >= 51;        // Darkness Wave
                case 4 -> level >= 60;        // Gravity Crush
                case 5 -> level >= 26;        // Night Shroud
                default -> false;
            };
        } else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_SOUL)) {
            boolean ultUnlocked = getPlayerData(player).soulStolenPlayers >= 3 || getMasteryLevel(player) >= 200;
            // Soul-Soul
            return switch (moveIndex) {
                case 0 -> level >= 1;     // Soul Bolt
                case 1 -> level >= 25;    // Spirit Guard
                case 2 -> level >= 50;    // Wraith Push
                case 3 -> level >= 75;    // Phantom Bind
                case 4 -> level >= 100;   // Spirit Barrage
                case 5 -> true;           // Soul Steal (always available, has its own 30m cooldown)
                case 6 -> ultUnlocked && level >= 125; // Soul Army
                case 7 -> ultUnlocked && level >= 150; // Life Drain
                case 8 -> ultUnlocked && level >= 175; // Soul Dominance
                default -> false;
            };
        }
        else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DRAGON)) {
            // Dragon-Dragon
            return switch (moveIndex) {
                case 0 -> true;          // Dragon Slash (Lv 0)
                case 1 -> level >= 50;   // Heat Breath
                case 2 -> level >= 120;  // Thunder Bagua Smash
                case 3 -> level >= 170;  // Dragon Twister
                case 4 -> level >= 200;  // Flame Clouds
                case 5 -> level >= 230;  // Dragon's Roar
                default -> false;
            };
        }
        // Nika base moves unlock progression
        switch (moveIndex) {
            case 0 -> { return true; } // Pistol - always available
            case 1 -> { return level >= 3; } // Balloon - level 3
            case 2 -> { return level >= 5; } // Rocket - level 5
            case 3 -> { return level >= 10; } // Whip - level 10
            case 4 -> { return level >= 15; } // Bazooka - level 15
            case 5 -> { return level >= 20; } // Bell - level 20
            case 6 -> { return level >= 25; } // Bazooka Gun - level 25
            case 7 -> { return isTransformed && level >= 60; } // Gigant - Gear 5 level 60
            case 8 -> { return isTransformed && level >= 75; } // Lightning - Gear 5 level 75
            case 9 -> { return isTransformed && level >= 90; } // Toon Force - Gear 5 level 90
            case 10 -> { return isTransformed && level >= 100; } // Supreme Haki - Gear 5 level 100
            default -> { return false; }
        }
    }
    
    private static boolean canTransformToGear5(PlayerEntity player) {
        // Awakening gate:
        // - Nika/Tremor/Dark: Mastery Level 50 minimum (existing behavior)
        // - DarkxQuake: Level 50 OR Warden kill while under Darkness/Blindness
        boolean isDxq = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE);
        if (isDxq) {
            // Requirement: Mastery 220 or Warden unlock (matches HUD/guide)
            return nika_nika_fruit_v7tnmscy.DevilFruitRegistry.isDarkxQuakeAwakeningUnlocked(player) || getMasteryLevel(player) >= 220;
        }
        // Nika: unlocked by Joyboy advancement or Mastery 200
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_NIKA)) {
            return hasAdvancement(player, "joyboy") || getMasteryLevel(player) >= 200;
        }
        // Dragon: Dragon's Fury at Mastery 250
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DRAGON)) {
            return getMasteryLevel(player) >= 250;
        }
        boolean masteryReady = getMasteryLevel(player) >= 50;
        return masteryReady;
    }
    
    // UI Helper methods
    public static float getPlayerDamageTaken(PlayerEntity player) {
        return playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData()).damageTaken;
    }

    public static float getPlayerDamageDealt(PlayerEntity player) {
        return playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData()).damageDealt;
    }
    
    public static int getCurrentMoveIndex(PlayerEntity player) {
        return playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData()).currentMoveIndex;
    }
    
    public static long getTransformationTimeRemaining(PlayerEntity player) {
        PlayerData data = playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData());
        if (!data.transformationActive) return 0;
        long currentTime = player.getWorld().getTime();
        return Math.max(0, data.transformationEndTime - currentTime);
    }
    
    // Mastery UI Helper methods
    public static int getMasteryLevel(PlayerEntity player) {
        PlayerData data = playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData());
        String fruit = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.getPlayerFruit(player);
        if (DevilFruitRegistry.FRUIT_DARKXQUAKE.equals(fruit)) return data.masteryLevelDxq;
        if (DevilFruitRegistry.FRUIT_QUAKE.equals(fruit)) return data.masteryLevelQuake;
        if (DevilFruitRegistry.FRUIT_DARK.equals(fruit)) return data.masteryLevelDark;
        if (DevilFruitRegistry.FRUIT_SOUL.equals(fruit)) return data.masteryLevelSoul;
        if (DevilFruitRegistry.FRUIT_DRAGON.equals(fruit)) return data.masteryLevelDragon;
        if (DevilFruitRegistry.FRUIT_OPERATION.equals(fruit)) return data.masteryLevelOperation;
        return data.masteryLevelNika;
    }

    // New helper: read mastery for a specific fruit regardless of what's equipped
    public static int getMasteryLevelForFruit(PlayerEntity player, String fruit) {
        PlayerData data = playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData());
        if (DevilFruitRegistry.FRUIT_DARKXQUAKE.equals(fruit)) return data.masteryLevelDxq;
        if (DevilFruitRegistry.FRUIT_QUAKE.equals(fruit)) return data.masteryLevelQuake;
        if (DevilFruitRegistry.FRUIT_DARK.equals(fruit)) return data.masteryLevelDark;
        if (DevilFruitRegistry.FRUIT_SOUL.equals(fruit)) return data.masteryLevelSoul;
        if (DevilFruitRegistry.FRUIT_DRAGON.equals(fruit)) return data.masteryLevelDragon;
        if (DevilFruitRegistry.FRUIT_OPERATION.equals(fruit)) return data.masteryLevelOperation;
        // Default to Nika when unknown
        return data.masteryLevelNika;
    }

    public static int getDarkMasteryLevel(PlayerEntity player) { return getMasteryLevelForFruit(player, DevilFruitRegistry.FRUIT_DARK); }
    public static int getQuakeMasteryLevel(PlayerEntity player) { return getMasteryLevelForFruit(player, DevilFruitRegistry.FRUIT_QUAKE); }
    
    public static float getMasteryExp(PlayerEntity player) {
        PlayerData data = playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData());
        String fruit = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.getPlayerFruit(player);
        if (DevilFruitRegistry.FRUIT_DARKXQUAKE.equals(fruit)) return data.masteryExpDxq;
        if (DevilFruitRegistry.FRUIT_QUAKE.equals(fruit)) return data.masteryExpQuake;
        if (DevilFruitRegistry.FRUIT_DARK.equals(fruit)) return data.masteryExpDark;
        if (DevilFruitRegistry.FRUIT_SOUL.equals(fruit)) return data.masteryExpSoul;
        if (DevilFruitRegistry.FRUIT_DRAGON.equals(fruit)) return data.masteryExpDragon;
        if (DevilFruitRegistry.FRUIT_OPERATION.equals(fruit)) return data.masteryExpOperation;
        return data.masteryExpNika;
    }
    
    public static float getExpRequiredForNextLevel(PlayerEntity player) {
        PlayerData data = playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData());
        String fruit = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.getPlayerFruit(player);
        if (DevilFruitRegistry.FRUIT_DARKXQUAKE.equals(fruit)) return data.expRequiredForNextLevelDxq;
        if (DevilFruitRegistry.FRUIT_QUAKE.equals(fruit)) return data.expRequiredForNextLevelQuake;
        if (DevilFruitRegistry.FRUIT_DARK.equals(fruit)) return data.expRequiredForNextLevelDark;
        if (DevilFruitRegistry.FRUIT_SOUL.equals(fruit)) return data.expRequiredForNextLevelSoul;
        if (DevilFruitRegistry.FRUIT_DRAGON.equals(fruit)) return data.expRequiredForNextLevelDragon;
        if (DevilFruitRegistry.FRUIT_OPERATION.equals(fruit)) return data.expRequiredForNextLevelOperation;
        return data.expRequiredForNextLevelNika;
    }

    // Command/Advancement helpers
    // Fruit-specific milestone list
    private static int[] getMilestonesForFruit(String fruit) {
        if (DevilFruitRegistry.FRUIT_DARKXQUAKE.equals(fruit)) return new int[]{1,3,5,10,15,20,25,50,60,75,90,100};
        if (DevilFruitRegistry.FRUIT_QUAKE.equals(fruit)) return new int[]{1,5,10,26,50,51,60,75,90,100};
        if (DevilFruitRegistry.FRUIT_DARK.equals(fruit)) return new int[]{1,5,26,50,51,60,75,90,100};
        if (DevilFruitRegistry.FRUIT_SOUL.equals(fruit)) return new int[]{1,25,50,75,100,125,150,175,200};
        if (DevilFruitRegistry.FRUIT_DRAGON.equals(fruit)) return new int[]{50,120,170,200,210,230,250};
        if (DevilFruitRegistry.FRUIT_OPERATION.equals(fruit)) return new int[]{25,75,120,160,200,250};
        return new int[]{1,3,5,10,15,20,25,50,60,75,90,100};
    }

    public static void setMasteryLevel(PlayerEntity player, int level) {
        if (!isDevilFruitUser(player)) return;
        PlayerData data = getPlayerData(player);
        String fruit = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.getPlayerFruit(player);
        if (fruit == null) fruit = DevilFruitRegistry.FRUIT_NIKA;
        int oldLevel;
        level = Math.max(1, Math.min(1000, level));
        if (DevilFruitRegistry.FRUIT_DARKXQUAKE.equals(fruit)) {
            oldLevel = data.masteryLevelDxq;
            data.masteryLevelDxq = level;
            data.expRequiredForNextLevelDxq = calcExpRequired(data.masteryLevelDxq);
        } else if (DevilFruitRegistry.FRUIT_QUAKE.equals(fruit)) {
            oldLevel = data.masteryLevelQuake;
            data.masteryLevelQuake = level;
            data.expRequiredForNextLevelQuake = calcExpRequired(data.masteryLevelQuake);
        } else if (DevilFruitRegistry.FRUIT_DARK.equals(fruit)) {
            oldLevel = data.masteryLevelDark;
            data.masteryLevelDark = level;
            data.expRequiredForNextLevelDark = calcExpRequired(data.masteryLevelDark);
        } else if (DevilFruitRegistry.FRUIT_SOUL.equals(fruit)) {
            oldLevel = data.masteryLevelSoul;
            data.masteryLevelSoul = level;
            data.expRequiredForNextLevelSoul = calcExpRequired(data.masteryLevelSoul);
        } else if (DevilFruitRegistry.FRUIT_DRAGON.equals(fruit)) {
            oldLevel = data.masteryLevelDragon;
            data.masteryLevelDragon = level;
            data.expRequiredForNextLevelDragon = calcExpRequired(data.masteryLevelDragon);
        } else if (DevilFruitRegistry.FRUIT_OPERATION.equals(fruit)) {
            oldLevel = data.masteryLevelOperation;
            data.masteryLevelOperation = level;
            data.expRequiredForNextLevelOperation = calcExpRequired(data.masteryLevelOperation);
        } else {
            oldLevel = data.masteryLevelNika;
            data.masteryLevelNika = level;
            data.expRequiredForNextLevelNika = calcExpRequired(data.masteryLevelNika);
        }
        // Fire unlock messages/advancements for milestones crossed upwards
        int[] milestones = getMilestonesForFruit(fruit);
        for (int l : milestones) {
            if (oldLevel < l && level >= l) {
                checkMoveUnlocks(player, l);
            }
        }
    }

    public static void setMasteryToNextUnlock(PlayerEntity player) {
        if (!isDevilFruitUser(player)) return;
        int current = getMasteryLevel(player);
        String fruit = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.getPlayerFruit(player);
        if (fruit == null) fruit = DevilFruitRegistry.FRUIT_NIKA;
        int[] milestones = getMilestonesForFruit(fruit);
        for (int m : milestones) {
            if (current < m) {
                setMasteryLevel(player, m);
                return;
            }
        }
    }

    public static void unlockGear5Early(PlayerEntity player) {
        if (!isDevilFruitUser(player)) return;
        if (getMasteryLevel(player) < 50) {
            setMasteryLevel(player, 50);
        }
        // Grant advancement only for Nika
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_NIKA)) {
            // Joyboy is the new unlock achievement
            grantAdvancement(player, "joyboy");
            player.sendMessage(Text.literal("§f§lFate intervenes! You awaken Gear 5 as Joyboy!"), false);
        }
        activateTransformation(player, player.getWorld());
    }

    public static void grantAdvancement(PlayerEntity player, String idPath) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        AdvancementEntry adv = serverPlayer.getServer().getAdvancementLoader().get(Identifier.of(NikaNikaFruit.MOD_ID, idPath));
        if (adv != null) {
            serverPlayer.getAdvancementTracker().grantCriterion(adv, "impossible");
        }
    }
    
    // Reset all Nika-related state for a player (used by Mysterious Brew)
    public static void resetAllForPlayer(PlayerEntity player) {
        playerDataMap.remove(player.getUuid());
    }

    // HUD accessors and utility
    public static long getGlobalCooldownRemaining(PlayerEntity player) {
        long now = player.getWorld().getTime();
        long cd = getPlayerData(player).abilityCooldown;
        return Math.max(0, cd - now);
    }

    public static long getSelectedMoveCooldownRemaining(PlayerEntity player) {
        PlayerData d = getPlayerData(player);
        long now = player.getWorld().getTime();
        int idx = d.currentMoveIndex;
        long remain = getCurrentFruitMoveCooldown(player, idx);
        // Overlay special long timers for Nika exclusives
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_NIKA)) {
            if (idx == 6) { // Bazooka Gun slot
                remain = Math.max(remain, Math.max(0, d.bazookaGunCooldown - now));
            }
            if (idx == 10 && d.transformationActive) { // Supreme Haki slot
                remain = Math.max(remain, Math.max(0, d.supremeHakiCooldown - now));
            }
        }
        return remain;
    }

    public static void resetCooldowns(PlayerEntity player) {
        PlayerData d = getPlayerData(player);
        long now = player.getWorld().getTime();
        d.abilityCooldown = now;
        d.bazookaGunCooldown = now;
        d.supremeHakiCooldown = now;
        d.ultimateCooldown = now; // also reset any ultimate timers (Quake, etc.)
        // Soul-specific cooldowns
        d.soulStealCooldown = now;
        d.soulGuardEndTime = now;
        d.soulBorrowEndTime = now;
        // Reset all per-move cooldown arrays for every fruit
        java.util.Arrays.fill(d.nikaMoveCooldowns, now);
        java.util.Arrays.fill(d.darkMoveCooldowns, now);
        java.util.Arrays.fill(d.quakeMoveCooldowns, now);
        java.util.Arrays.fill(d.dxqMoveCooldowns, now);
        java.util.Arrays.fill(d.soulMoveCooldowns, now);
        java.util.Arrays.fill(d.dragonMoveCooldowns, now);
        java.util.Arrays.fill(d.operationMoveCooldowns, now);
        player.sendMessage(Text.literal("§aAll ability cooldowns reset."), true);
    }

    public static void setQuickMoveToCurrent(PlayerEntity player) {
        PlayerData d = getPlayerData(player);
        d.quickMoveIndex = d.currentMoveIndex;
    }

    public static void quickUse(PlayerEntity player) {
        PlayerData d = getPlayerData(player);
        int saved = d.currentMoveIndex;
        d.currentMoveIndex = d.quickMoveIndex;
        useCurrentMove(player);
        d.currentMoveIndex = saved;
    }

    public static void showInfoAndSetQuickMove(PlayerEntity player) {
        // Show info in chat and set quick-move to current selection
        setQuickMoveToCurrent(player);
        boolean transformed = isTransformed(player);
        String[] moves = getMoveNames(player, transformed);
        int level = getMasteryLevel(player);
        player.sendMessage(Text.literal("§6§l=== Move Info ==="), false);
        for (int i = 0; i < moves.length; i++) {
            int req = getRequiredLevelFor(player, i);
            String status = level >= req ? "§a✔" : "§c✖";
            player.sendMessage(Text.literal(status + " " + moves[i] + " (Lv " + req + ")"), false);
        }
        long remain = getSelectedMoveCooldownRemaining(player) / 20;
        player.sendMessage(Text.literal("§7Quick Use bound to current move. Cooldown remaining: " + remain + "s"), false);
    }
}
