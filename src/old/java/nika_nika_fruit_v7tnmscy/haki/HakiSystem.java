package nika_nika_fruit_v7tnmscy.haki;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;
// File I/O removed for safety: no external persistence

/**
 * Lightweight Haki framework integrated with the Devil Fruits control/UX.
 * This is intentionally simple and piggybacks the existing mastery/cooldown/UX patterns.
 */
public class HakiSystem {
    public enum Type { ARMAMENT, OBSERVATION, CONQUERORS }

    public static class PlayerHaki {
        // Unlock flags
        public boolean armamentUnlocked = false;
        public boolean observationUnlocked = false;
        public boolean conquerorsUnlocked = false;

        // Mastery/exp per type (1..200 levels)
        public int armLevel = 1, obsLevel = 1, conqLevel = 1;
        public float armExp = 0, obsExp = 0, conqExp = 0;
        public float armReq = 100, obsReq = 100, conqReq = 100;

        // Selection/state
        public Type selected = Type.ARMAMENT; // default
        public int armMove = 0, obsMove = 0, conqMove = 0; // per-type selected move index
        public boolean armamentActive = false; // toggle state

        // Cooldowns (per-ability timers; no global lock)
        public long globalCd = 0L; // deprecated; retained for compatibility but unused
        public long lastUseArm = 0L, lastUseObs = 0L, lastUseConq = 0L;
        // Per special moves and per-ability arrays (index by current move idx)
        public long cdRyuo = 0L;          // Armament special
        public long cdFutureBurst = 0L;   // Observation special
        public long cdConqMight = 0L;     // Conquerors special
        public long[] cdArm = new long[4];
        public long[] cdObs = new long[4];
        public long[] cdConq = new long[4];

        // Unlock progress trackers
        public float damageTakenTotal = 0f; // Observation unlock (total, across deaths)
        public float meleeDamageDealt = 0f;   // Armament unlock (only fists/wood/stone)
        public Set<UUID> uniquePlayersKilled = new HashSet<>(); // Conquerors unlock unique kills
        public long conqUnlockTimerStart = 0L; // start tick for 3 in-game hours window (3*60*20*60 = 216000 ticks)

        // Observation unlock objective: survive without taking damage
        public long noHitTicks = 0L; // increments on server tick; resets on damage
        public long bestNoHitTicks = 0L; // personal best for UI
        
        // Observation: temporary dodge invulnerability end tick
        public long obsDodgeInvulnerableUntil = 0L;
    }

    private static final Map<UUID, PlayerHaki> DATA = new HashMap<>();

    private static PlayerHaki get(PlayerEntity p) { return DATA.computeIfAbsent(p.getUuid(), k -> new PlayerHaki()); }

    private static float expRequired(int level) {
        if (level < 10) return 30.0f + level * 12.0f;
        if (level < 30) return 150.0f + (level - 10) * 15.0f;
        if (level < 60) return 450.0f + (level - 30) * 25.0f;
        if (level < 100) return 1200.0f + (level - 60) * 35.0f;
        if (level < 150) return 2600.0f + (level - 100) * 50.0f;
        if (level < 200) return 5100.0f + (level - 150) * 65.0f;
        return 8350.0f;
    }

    // ------------- PUBLIC API (server-side) -------------

    public static void cycle(PlayerEntity player) {
        PlayerHaki d = get(player);
        // Cycle within current selected type
        int count = getMoves(player, d.selected).length;
        int idx = getSelectedIndex(d.selected, d);
        idx = (idx + 1) % Math.max(1, count);
        setSelectedIndex(d.selected, d, idx);
        String name = getMoves(player, d.selected)[idx];
        player.sendMessage(Text.literal("§bHaki Selected: " + name + " (§7" + d.selected.name().toLowerCase() + "§b)"), true);
        if (player.getWorld() instanceof ServerWorld sw) sw.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 0.6F, 1.1F);
    }

    public static void cycleReverse(PlayerEntity player) {
        PlayerHaki d = get(player);
        int count = getMoves(player, d.selected).length;
        int idx = getSelectedIndex(d.selected, d);
        idx = (idx - 1 + Math.max(1, count)) % Math.max(1, count);
        setSelectedIndex(d.selected, d, idx);
        String name = getMoves(player, d.selected)[idx];
        player.sendMessage(Text.literal("§bHaki Selected: " + name + " (§7" + d.selected.name().toLowerCase() + "§b)"), true);
        if (player.getWorld() instanceof ServerWorld sw) sw.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 0.6F, 0.9F);
    }

    public static void selectTypeNext(PlayerEntity player) {
        PlayerHaki d = get(player);
        Type[] vals = Type.values();
        int idx = (d.selected.ordinal() + 1) % vals.length;
        d.selected = vals[idx];
        player.sendMessage(Text.literal("§bSelected Haki Type: §f" + d.selected.name().substring(0,1) + d.selected.name().substring(1).toLowerCase()), true);
        if (player.getWorld() instanceof ServerWorld sw) sw.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 0.6F, 1.0F);
    }

    public static void selectTypePrev(PlayerEntity player) {
        PlayerHaki d = get(player);
        Type[] vals = Type.values();
        int idx = (d.selected.ordinal() - 1 + vals.length) % vals.length;
        d.selected = vals[idx];
        player.sendMessage(Text.literal("§bSelected Haki Type: §f" + d.selected.name().substring(0,1) + d.selected.name().substring(1).toLowerCase()), true);
        if (player.getWorld() instanceof ServerWorld sw) sw.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 0.6F, 0.9F);
    }

    public static void use(PlayerEntity player) {
        if (player.getWorld().isClient) return;
        PlayerHaki d = get(player);
        long now = player.getWorld().getTime();
        // Per-ability cooldowns are enforced inside each branch now
        switch (d.selected) {
            case ARMAMENT -> useArmament(player, d, now);
            case OBSERVATION -> useObservation(player, d, now);
            case CONQUERORS -> useConquerors(player, d, now);
        }
    }

    public static void showMastery(PlayerEntity player) {
        PlayerHaki d = get(player);
        player.sendMessage(Text.literal("§6§l=== Haki Mastery ==="), false);
        player.sendMessage(Text.literal("§7Armament: §eLv " + d.armLevel + " §7(§f" + (int)d.armExp + "/" + (int)d.armReq + "§7)"), false);
        player.sendMessage(Text.literal("§7Observation: §eLv " + d.obsLevel + " §7(§f" + (int)d.obsExp + "/" + (int)d.obsReq + "§7)"), false);
        player.sendMessage(Text.literal("§7Conqueror's: §eLv " + d.conqLevel + " §7(§f" + (int)d.conqExp + "/" + (int)d.conqReq + "§7)"), false);
        player.sendMessage(Text.literal("§8Press I for move info."), false);
    }

    public static void showInfo(PlayerEntity player) {
        PlayerHaki d = get(player);
        player.sendMessage(Text.literal("§6§l=== " + d.selected.name() + " Haki Moves ==="), false);
        String[] moves = getMoves(player, d.selected);
        for (int i = 0; i < moves.length; i++) {
            int req = requiredLevel(d.selected, i);
            boolean ok = levelFor(d.selected, d) >= req;
            String status = ok ? "§a✔ " : "§c✖ ";
            player.sendMessage(Text.literal(status + moves[i] + " §7(Lv " + req + ")"), false);
        }
        player.sendMessage(Text.literal("§7Selected: §f" + moves[getSelectedIndex(d.selected, d)]), false);
    }

    public static void resetCooldowns(PlayerEntity p) {
        PlayerHaki d = get(p);
        long now = p.getWorld().getTime();
        Arrays.fill(d.cdArm, now); Arrays.fill(d.cdObs, now); Arrays.fill(d.cdConq, now);
        d.cdRyuo = now; d.cdFutureBurst = now; d.cdConqMight = now;
        p.sendMessage(Text.literal("§aAll Haki cooldowns reset."), true);
    }

    public static boolean hasAnyUnlocked(PlayerEntity p) {
        PlayerHaki d = get(p);
        return d.armamentUnlocked || d.observationUnlocked || d.conquerorsUnlocked;
    }

    // Called from mixins to feed unlock/XP
    public static void onPlayerTakeDamage(PlayerEntity p, float amount) {
        PlayerHaki d = get(p);
        // Observation objective tracking: total damage and reset no-hit timer
        d.damageTakenTotal += amount;
        d.noHitTicks = 0L;
        // Observation passive XP: gain based on damage taken
        if (d.observationUnlocked) {
            addExp(p, Type.OBSERVATION, Math.max(1f, amount * 0.8f));
        }
    }

    public static void onPlayerDealMeleeDamage(PlayerEntity attacker, float amount) {
        PlayerHaki d = get(attacker);
        // Count only fists damage for Armament requirement
        net.minecraft.item.ItemStack held = attacker.getMainHandStack();
        boolean allowed = held.isEmpty();
        if (!allowed) return;
        d.meleeDamageDealt += amount;
        // Unlock is granted via the book after requirements are met
        // Armament passive XP when toggled ON: gain based on damage dealt
        if (d.armamentUnlocked && d.armamentActive) {
            addExp(attacker, Type.ARMAMENT, Math.max(1f, amount * 0.7f));
        }
    }

    public static void onPlayerKill(PlayerEntity killer, LivingEntity victim) {
        if (!(killer.getWorld() instanceof ServerWorld sw)) return;
        PlayerHaki d = get(killer);
        if (victim instanceof PlayerEntity pe) {
            // Start/track 3h window from first player kill
            if (d.conqUnlockTimerStart == 0L) d.conqUnlockTimerStart = sw.getTime();
            d.uniquePlayersKilled.add(pe.getUuid());
            long elapsed = sw.getTime() - d.conqUnlockTimerStart; // ticks
            long limit = 216000L; // 3h
            if (!d.conquerorsUnlocked && d.uniquePlayersKilled.size() >= 25 && elapsed <= limit) {
                d.conquerorsUnlocked = true;
                grant(killer, "§5Conqueror's Haki unlocked! (Will of a King)");
            }
        }
        // Conqueror XP for kills only when not on cooldown
        // EXP is only granted when using a Haki ability (G), not from kills
    }

    public static void onPlayerDeath(PlayerEntity p) {
        // No reset for observation total damage; persists across deaths
    }

    // Observation dodge chance hook. Return true to cancel damage.
    public static boolean tryObservationDodge(PlayerEntity p, long now) {
        PlayerHaki d = get(p);
        if (!d.observationUnlocked) return false;
        if (now < d.obsDodgeInvulnerableUntil) return true; // already in dodge window
        // Chance scales with level: 5% base + 0.15% per level (Lv200 -> 35%)
        float chance = 0.05f + (levelFor(Type.OBSERVATION, d) * 0.0015f);
        if (p.getRandom().nextFloat() < chance) {
            // Grant brief invulnerability and visual
            d.obsDodgeInvulnerableUntil = now + 20; // 1 second i-frames
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 1));
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 20, 0));
            p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.6F, 1.8F);
            if (p.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(net.minecraft.particle.ParticleTypes.CLOUD, p.getX(), p.getBodyY(0.5), p.getZ(), 8, 0.4,0.3,0.4, 0.1);
            }
            return true;
        }
        return false;
    }

    // Armament melee bonus. Returns additional damage to add.
    public static float armamentBonus(PlayerEntity attacker) {
        PlayerHaki d = get(attacker);
        if (!d.armamentUnlocked || !d.armamentActive) return 0f;
        int lvl = d.armLevel;
        // +25% to +50% scaling
        float mult = 1.0f + 0.25f + Math.min(0.25f, lvl * 0.00125f);
        return (float) ((mult - 1.0f) * 4.0f); // translate to flat damage approximation
    }

    // Damage reduction when armament active
    public static float armamentDamageReduction(PlayerEntity defender) {
        PlayerHaki d = get(defender);
        if (!d.armamentUnlocked || !d.armamentActive) return 0f;
        // 10% + up to 30% by level
        return 0.10f + Math.min(0.30f, d.armLevel * 0.0015f);
    }

    // ------------- HUD helpers -------------
    public static boolean shouldRender(PlayerEntity p) {
        return hasAnyUnlocked(p);
    }

    public static int getDisplayedLevel(PlayerEntity p) {
        PlayerHaki d = get(p);
        // Show the selected type level
        return switch (d.selected) {
            case ARMAMENT -> d.armLevel; case OBSERVATION -> d.obsLevel; case CONQUERORS -> d.conqLevel; };
    }

    public static float getDisplayedExp(PlayerEntity p) {
        PlayerHaki d = get(p);
        return switch (d.selected) {
            case ARMAMENT -> d.armExp; case OBSERVATION -> d.obsExp; case CONQUERORS -> d.conqExp; };
    }

    public static float getDisplayedReq(PlayerEntity p) {
        PlayerHaki d = get(p);
        return switch (d.selected) {
            case ARMAMENT -> d.armReq; case OBSERVATION -> d.obsReq; case CONQUERORS -> d.conqReq; };
    }

    public static Type getSelectedType(PlayerEntity p) { return get(p).selected; }
    public static boolean isArmamentActive(PlayerEntity p) { return get(p).armamentActive; }

    // Individual getters for HUD's three-bar display
    public static int getArmamentLevel(PlayerEntity p) { return get(p).armLevel; }
    public static float getArmamentExp(PlayerEntity p) { return get(p).armExp; }
    public static float getArmamentReq(PlayerEntity p) { return Math.max(1f, get(p).armReq); }

    public static int getObservationLevel(PlayerEntity p) { return get(p).obsLevel; }
    public static float getObservationExp(PlayerEntity p) { return get(p).obsExp; }
    public static float getObservationReq(PlayerEntity p) { return Math.max(1f, get(p).obsReq); }

    public static int getConquerorsLevel(PlayerEntity p) { return get(p).conqLevel; }
    public static float getConquerorsExp(PlayerEntity p) { return get(p).conqExp; }
    public static float getConquerorsReq(PlayerEntity p) { return Math.max(1f, get(p).conqReq); }

    public static long getObservationNoHitTicks(PlayerEntity p) { return get(p).noHitTicks; }
    public static long getObservationBestNoHitTicks(PlayerEntity p) { return get(p).bestNoHitTicks; }
    public static boolean hasConquerorsUnlocked(PlayerEntity p) { return get(p).conquerorsUnlocked; }

    public static String getSelectedMoveName(PlayerEntity p) {
        PlayerHaki d = get(p);
        String[] moves = getMoves(p, d.selected);
        int i = getSelectedIndex(d.selected, d);
        if (i >= moves.length) i = 0;
        return moves[i];
    }

    private static long conqCooldownTicks(PlayerHaki d) {
        int steps = Math.max(0, d.conqLevel / 50);
        int seconds = Math.max(15, 30 - (steps * 3));
        return seconds * 20L;
    }

    public static long getSelectedMoveCooldown(PlayerEntity p) {
        PlayerHaki d = get(p);
        long now = p.getWorld().getTime();
        long remain = 0L;
        int idx = getSelectedIndex(d.selected, d);
        if (d.selected == Type.ARMAMENT) remain = Math.max(Math.max(0, d.cdArm[idx] - now), Math.max(0, d.cdRyuo - now));
        if (d.selected == Type.OBSERVATION) remain = Math.max(Math.max(0, d.cdObs[idx] - now), Math.max(0, d.cdFutureBurst - now));
        if (d.selected == Type.CONQUERORS) remain = Math.max(Math.max(0, d.cdConq[idx] - now), Math.max(0, d.cdConqMight - now));
        return remain;
    }

    // ------------- Private impl -------------

    private static int getSelectedIndex(Type t, PlayerHaki d) {
        return switch (t) { case ARMAMENT -> d.armMove; case OBSERVATION -> d.obsMove; case CONQUERORS -> d.conqMove; };
    }
    private static void setSelectedIndex(Type t, PlayerHaki d, int v) {
        switch (t) { case ARMAMENT -> d.armMove = v; case OBSERVATION -> d.obsMove = v; case CONQUERORS -> d.conqMove = v; }
    }

    private static int levelFor(Type t, PlayerHaki d) {
        return switch (t) { case ARMAMENT -> d.armLevel; case OBSERVATION -> d.obsLevel; case CONQUERORS -> d.conqLevel; };
    }

    private static void addExp(PlayerEntity p, Type type, float amt) {
        PlayerHaki d = get(p);
        if (!isUnlocked(type, d)) return;
        switch (type) {
            case ARMAMENT -> { if (d.armLevel >= 1000) return; d.armExp += amt; d.armReq = expRequired(d.armLevel); while (d.armExp >= d.armReq && d.armLevel < 1000) { d.armExp -= d.armReq; d.armLevel++; d.armReq = expRequired(d.armLevel); ding(p, d.armLevel); } if (d.armLevel >= 1000) { d.armLevel = 1000; d.armExp = 0; d.armReq = expRequired(1000); } }
            case OBSERVATION -> { if (d.obsLevel >= 1000) return; d.obsExp += amt; d.obsReq = expRequired(d.obsLevel); while (d.obsExp >= d.obsReq && d.obsLevel < 1000) { d.obsExp -= d.obsReq; d.obsLevel++; d.obsReq = expRequired(d.obsLevel); ding(p, d.obsLevel); } if (d.obsLevel >= 1000) { d.obsLevel = 1000; d.obsExp = 0; d.obsReq = expRequired(1000); } }
            case CONQUERORS -> { if (d.conqLevel >= 1000) return; d.conqExp += amt; d.conqReq = expRequired(d.conqLevel); while (d.conqExp >= d.conqReq && d.conqLevel < 1000) { d.conqExp -= d.conqReq; d.conqLevel++; d.conqReq = expRequired(d.conqLevel); ding(p, d.conqLevel); } if (d.conqLevel >= 1000) { d.conqLevel = 1000; d.conqExp = 0; d.conqReq = expRequired(1000); } }
        }
    }

    public static boolean cmdAddExp(ServerPlayerEntity target, String type, int amount) {
        Type t;
        switch (type.toLowerCase()) {
            case "armament" -> t = Type.ARMAMENT;
            case "observation" -> t = Type.OBSERVATION;
            case "conquerors", "conqueror", "conqueror's" -> t = Type.CONQUERORS;
            default -> { return false; }
        }
        addExp(target, t, amount);
        target.sendMessage(Text.literal("§aAdded " + amount + " EXP to " + t.name().toLowerCase() + " haki"), false);
        return true;
    }

    private static void ding(PlayerEntity p, int lvl) {
        p.sendMessage(Text.literal("§6§lHaki Level Up! Lv " + lvl), false);
        p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
        if (p.getWorld() instanceof ServerWorld sw) sw.spawnParticles(net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY()+1, p.getZ(), 12, 0.6,0.6,0.6, 0.1);
    }

    private static boolean isUnlocked(Type t, PlayerHaki d) {
        return switch (t) {
            case ARMAMENT -> d.armamentUnlocked; case OBSERVATION -> d.observationUnlocked; case CONQUERORS -> d.conquerorsUnlocked; };
    }

    private static void grant(PlayerEntity p, String msg) {
        p.sendMessage(Text.literal(msg), false);
        if (p instanceof ServerPlayerEntity sp) {
            sp.sendMessage(Text.literal(msg), false);
        }
        p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.8F, 1.0F);
    }

    private static String[] getMoves(PlayerEntity p, Type t) {
        int lvl = levelFor(t, get(p));
        return switch (t) {
            case ARMAMENT -> new String[]{
                "Toggle Armament",           // on/off, fists black-like buff
                "Emission Shockwave",        // 5-block shockwave
                "Internal Destruction"       // bypass armor
            };
            case OBSERVATION -> new String[]{
                "Presence Sense",            // detect entities, outlines
                "Dodge Assist",              // brief slow-mo: implemented as Speed + time dilation feel
                "Future Sight"               // predict, Speed + Glowing on entities
            };
            case CONQUERORS -> new String[]{
                "Beginning Blast",           // weak knockback at 0
                "Will Crush",                // stun (slowness, weakness)
                "Conqueror's Might"          // special: explosion+lightning+effects
            };
        };
    }

    private static int requiredLevel(Type t, int idx) {
        return switch (t) {
            case ARMAMENT -> switch (idx) { case 0 -> 1; case 1 -> 20; case 2 -> 40; default -> 1; };
            case OBSERVATION -> switch (idx) { case 0 -> 1; case 1 -> 20; case 2 -> 40; default -> 1; };
            case CONQUERORS -> switch (idx) { case 0 -> 1; case 1 -> 40; case 2 -> 50; default -> 1; };
        };
    }

    private static boolean ensureUnlockedAndLevel(PlayerEntity p, Type t, int idx) {
        PlayerHaki d = get(p);
        if (!isUnlocked(t, d)) { p.sendMessage(Text.literal("§cYou have not unlocked " + t.name().toLowerCase() + " haki."), true); return false; }
        int req = requiredLevel(t, idx);
        if (levelFor(t, d) < req) { p.sendMessage(Text.literal("§cRequires Haki Lv " + req), true); return false; }
        return true;
    }

    private static void useArmament(PlayerEntity p, PlayerHaki d, long now) {
        int idx = d.armMove;
        if (!ensureUnlockedAndLevel(p, Type.ARMAMENT, idx)) return;
        // Per-ability cooldown check
        if (now < d.cdArm[idx]) {
            long remain = (d.cdArm[idx] - now) / 20L;
            p.sendMessage(Text.literal("§cArmament on cooldown: " + remain + "s"), true);
            return;
        }
        switch (idx) {
            case 0 -> { // Toggle
                // 10s toggle cooldown
                if (now - d.lastUseArm < 200) {
                    p.sendMessage(Text.literal("§cArmament toggle is on cooldown."), true);
                    return;
                }
                d.lastUseArm = now;
                d.armamentActive = !d.armamentActive;
                p.sendMessage(Text.literal(d.armamentActive ? "§8Armament: ON" : "§8Armament: OFF"), true);
                p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 0.6F, d.armamentActive ? 0.6F : 1.2F);
                if (d.armamentActive) {
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 0));
                }
            }
            case 1 -> { // Emission Shockwave
                d.cdArm[1] = now + 60; // 3s
                if (p.getWorld() instanceof ServerWorld sw) {
                    double r = 5.0 + d.armLevel * 0.02;
                    aoeDamage(sw, p, r, 8.0f, 1.2, true);
                    sw.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.8F, 1.3F);
                }
            }
            case 2 -> { // Internal Destruction
                d.cdArm[2] = now + 80; // 4s
                if (p.getWorld() instanceof ServerWorld sw) {
                    double r = 4.0 + d.armLevel * 0.02;
                    for (var e : sw.getOtherEntities(p, p.getBoundingBox().expand(r), ent -> ent instanceof LivingEntity && ent != p)) {
                        LivingEntity le = (LivingEntity)e;
                        le.damage(sw, sw.getDamageSources().magic(), 10.0f + (d.armLevel * 0.05f));
                    }
                    sw.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.PLAYERS, 1.0F, 0.5F);
                }
            }
            case 3 -> { // Ryuo Burst
                if (now < d.cdRyuo) { p.sendMessage(Text.literal("§cRyuo Burst on cooldown"), true); return; }
                d.cdRyuo = now + 20 * 25; // 25s
                d.cdArm[3] = now + 100;   // 5s
                if (p.getWorld() instanceof ServerWorld sw) {
                    double r = 7.0 + d.armLevel * 0.03;
                    aoeDamage(sw, p, r, 14.0f, 2.0, true);
                    sw.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.8F, 1.0F);
                }
            }
        }

    }

    private static void useObservation(PlayerEntity p, PlayerHaki d, long now) {
        int idx = d.obsMove;
        if (!ensureUnlockedAndLevel(p, Type.OBSERVATION, idx)) return;
        // Per-ability cooldown check
        if (now < d.cdArm[idx]) {
            long remain = (d.cdArm[idx] - now) / 20L;
            p.sendMessage(Text.literal("§cArmament on cooldown: " + remain + "s"), true);
            return;
        }
        switch (idx) {
            case 0 -> { // Presence Sense
                d.cdObs[0] = now + 40; // 2s
                if (p.getWorld() instanceof ServerWorld sw) {
                    double r = 20.0 + d.obsLevel * 0.05;
                    for (var e : sw.getOtherEntities(p, p.getBoundingBox().expand(r), ent -> ent instanceof LivingEntity && ent != p)) {
                        ((LivingEntity)e).addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0));
                    }
                    sw.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.PLAYERS, 0.8F, 1.5F);
                }
            }
            case 1 -> { // Dodge Assist
                d.cdObs[1] = now + 60; // 3s
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 60, 0));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 1));
            }
            case 2 -> { // Future Sight
                d.cdObs[2] = now + 80; // 4s
                if (p.getWorld() instanceof ServerWorld sw) {
                    double r = 20.0 + d.obsLevel * 0.05;
                    for (var e : sw.getOtherEntities(p, p.getBoundingBox().expand(r), ent -> ent instanceof LivingEntity && ent != p)) {
                        ((LivingEntity)e).addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 120, 0));
                        ((LivingEntity)e).addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 0));
                    }
                }
            }
            case 3 -> { // Future Vision Burst
                if (now < d.cdFutureBurst) { p.sendMessage(Text.literal("§cFuture Vision on cooldown"), true); return; }
                d.cdFutureBurst = now + 20 * 30; // 30s
                d.cdObs[0] = now + 40; // 2s
                // 5 seconds projectile invincibility simulated by resistance and a flag (checked by mixin not needed since generic)
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 2));
                p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.PLAYERS, 1.0F, 0.3F);
            }
        }

    }

    private static void useConquerors(PlayerEntity p, PlayerHaki d, long now) {
        int idx = d.conqMove;
        if (!ensureUnlockedAndLevel(p, Type.CONQUERORS, idx)) return;
        int tier = Math.max(0, d.conqLevel / 10); // extra power every 10 levels
        long cd = conqCooldownTicks(d);
        // Per-ability cooldown
        if (now < d.cdConq[idx]) {
            long remain = (d.cdConq[idx] - now) / 20L;
            p.sendMessage(Text.literal("§cConqueror's Haki on cooldown: " + remain + "s"), true);
            return;
        }
        d.lastUseConq = now;
        // Per-ability cooldown check
        if (now < d.cdArm[idx]) {
            long remain = (d.cdArm[idx] - now) / 20L;
            p.sendMessage(Text.literal("§cArmament on cooldown: " + remain + "s"), true);
            return;
        }
        switch (idx) {
            case 0 -> { // Beginning Blast
                d.cdConq[0] = now + conqCooldownTicks(d);
                if (p.getWorld() instanceof ServerWorld sw) {
                    double r = (6.0 + d.conqLevel * 0.03) * (1.0 + tier * 0.03);
                    aoeKnock(sw, p, r, 0.8 + tier * 0.05, (float)(6.0f + tier * 1.0f), false);
                    sw.spawnParticles(net.minecraft.particle.ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY(), p.getZ(), 10 + tier * 4, 1.0, 0.6, 1.0, 0.2);
                    addExp(p, Type.CONQUERORS, 3f);
                }
            }
            case 1 -> { // Will Crush
                d.cdConq[1] = now + conqCooldownTicks(d);
                if (p.getWorld() instanceof ServerWorld sw) {
                    double r = (10.0 + d.conqLevel * 0.03) * (1.0 + tier * 0.03);
                    for (var e : sw.getOtherEntities(p, p.getBoundingBox().expand(r), ent -> ent instanceof LivingEntity && ent != p)) {
                        LivingEntity le = (LivingEntity)e;
                        le.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100 + tier * 8, 4));
                        le.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200 + tier * 10, 2));
                        le.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60 + tier * 5, 0));
                        addExp(p, Type.CONQUERORS, 2f);
                    }
                    aoeKnock(sw, p, r, 1.5 + tier * 0.05, (float)(6.0f + tier * 1.0f), false);
                    sw.spawnParticles(net.minecraft.particle.ParticleTypes.SMOKE, p.getX(), p.getBodyY(0.5), p.getZ(), 15 + tier * 5, 1.2, 0.4, 1.2, 0.02);
                }
            }
            case 2 -> { // Conqueror's Might
                d.cdConq[2] = now + conqCooldownTicks(d);
                if (p.getWorld() instanceof ServerWorld sw) {
                    double r = (12.0 + d.conqLevel * 0.04) * (1.0 + tier * 0.04);
                    // Explosion burst
                    sw.createExplosion(p, p.getX(), p.getY(), p.getZ(), 4.0f + tier * 0.3f, World.ExplosionSourceType.NONE);
                    // Lightning: strongest at 250 has 2 strikes, earlier tiers unlock 1
                    if (d.conqLevel >= 200) {
                        int bolts = d.conqLevel >= 250 ? 2 : 1;
                        for (int i = 0; i < bolts; i++) {
                            net.minecraft.entity.LightningEntity bolt = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(sw, net.minecraft.entity.SpawnReason.TRIGGERED);
                            if (bolt != null) {
                                bolt.refreshPositionAfterTeleport(p.getX() + (sw.random.nextDouble()-0.5)*r, p.getY(), p.getZ() + (sw.random.nextDouble()-0.5)*r);
                                sw.spawnEntity(bolt);
                            }
                        }
                    }
                    for (var e : sw.getOtherEntities(p, p.getBoundingBox().expand(r), ent -> ent instanceof LivingEntity && ent != p)) {
                        LivingEntity le = (LivingEntity)e;
                        // Damage + launch
                        le.damage(sw, sw.getDamageSources().playerAttack(p), (12.0f + (d.conqLevel * 0.08f)) + tier * 2.0f);
                        Vec3d kb = le.getPos().subtract(p.getPos()).normalize().multiply(2.4 + tier * 0.08);
                        le.addVelocity(kb.x, 1.2 + tier * 0.02, kb.z);
                        le.velocityModified = true;
                        // Effects
                        le.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 120 + tier * 6, 0));
                        le.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200 + tier * 10, 1));
                        le.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200 + tier * 10, 4));
                        addExp(p, Type.CONQUERORS, 2f);
                    }
                    sw.spawnParticles(net.minecraft.particle.ParticleTypes.EXPLOSION_EMITTER, p.getX(), p.getY(), p.getZ(), 1, 0,0,0, 0.0);
                    sw.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.2F, 0.8F);
                }
            }
        }
        addExp(p, Type.CONQUERORS, 12f);
    }

    private static void aoeDamage(ServerWorld sw, PlayerEntity p, double radius, float damage, double kbMul, boolean weaken) {
        for (var e : sw.getOtherEntities(p, p.getBoundingBox().expand(radius), ent -> ent instanceof LivingEntity && ent != p)) {
            LivingEntity le = (LivingEntity)e;
            le.damage(sw, sw.getDamageSources().playerAttack(p), damage);
            Vec3d dir = le.getPos().subtract(p.getPos()).normalize();
            le.addVelocity(dir.x * kbMul, 0.8, dir.z * kbMul);
            le.velocityModified = true;
            if (weaken) le.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, 1));
        }
    }

    private static void aoeKnock(ServerWorld sw, PlayerEntity p, double radius, double kbMul, float damage, boolean armorBypass) {
        for (var e : sw.getOtherEntities(p, p.getBoundingBox().expand(radius), ent -> ent instanceof LivingEntity && ent != p)) {
            LivingEntity le = (LivingEntity)e;
            if (armorBypass) le.damage(sw, sw.getDamageSources().magic(), damage); else le.damage(sw, sw.getDamageSources().playerAttack(p), damage);
            Vec3d dir = le.getPos().subtract(p.getPos()).normalize();
            le.addVelocity(dir.x * kbMul, 1.0, dir.z * kbMul);
            le.velocityModified = true;
            // Conquerors hit grants a bit of mastery
            addExp(p, Type.CONQUERORS, 1.5f);
        }
    }

    // ------------- Server-side passive tick + helpers -------------
    public static void onServerTick(PlayerEntity p) {
        if (p.getWorld().isClient) return;
        PlayerHaki d = get(p);
        long now = p.getWorld().getTime();
        
        // Observation no-hit streak timer
        d.noHitTicks += 20; // count in ticks per second
        if (d.noHitTicks > d.bestNoHitTicks) d.bestNoHitTicks = d.noHitTicks;
        
        // Conqueror's passive aura: hurt entities and make mobs flee unless protected by non-leather armor
        if (d.conquerorsUnlocked && now % 40L == 0L) { // every 2 seconds
            if (p.getWorld() instanceof ServerWorld sw) {
                double r = 8.0 + d.conqLevel * 0.02;
                for (var e : sw.getOtherEntities(p, p.getBoundingBox().expand(r), ent -> ent instanceof LivingEntity && ent != p)) {
                    LivingEntity le = (LivingEntity)e;
                    boolean protectedByArmor = hasNonLeatherArmor(le);
                    if (!protectedByArmor) {
                        // Minor will damage
                        le.damage(sw, sw.getDamageSources().magic(), 1.0f);
                        // Flee impulse for mobs
                        if (!(le instanceof PlayerEntity)) {
                            var dir = le.getPos().subtract(p.getPos()).normalize().multiply(0.35);
                            le.addVelocity(dir.x, 0.2, dir.z);
                            le.velocityModified = true;
                            le.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0));
                        }
                    }
                }
            }
        }
    }

    private static boolean hasNonLeatherArmor(LivingEntity e) {
        net.minecraft.entity.EquipmentSlot[] armorSlots = new net.minecraft.entity.EquipmentSlot[] {
            net.minecraft.entity.EquipmentSlot.HEAD,
            net.minecraft.entity.EquipmentSlot.CHEST,
            net.minecraft.entity.EquipmentSlot.LEGS,
            net.minecraft.entity.EquipmentSlot.FEET
        };
        for (net.minecraft.entity.EquipmentSlot slot : armorSlots) {
            net.minecraft.item.ItemStack st = e.getEquippedStack(slot);
            if (st != null && !st.isEmpty()) {
                net.minecraft.util.Identifier id = net.minecraft.registry.Registries.ITEM.getId(st.getItem());
                String path = id != null ? id.getPath() : "";
                if (path.contains("leather")) {
                    continue; // leather doesn't protect
                }
                return true; // any non-leather armor piece protects
            }
        }
        return false;
    }

    // ------------- Persistence for permanent Conqueror's -------------
    private static final Set<UUID> PERM_CONQ = new HashSet<>();
    private static boolean PERM_LOADED = false;
    // External persistence disabled. Keep data in-memory only during runtime.
    private static void loadPerm() { /* no-op */ }
    private static void savePerm() { /* no-op */ }
    public static boolean hasPermanentConq(UUID u) { return PERM_CONQ.contains(u); }
    public static void grantPermanentConq(UUID u) { PERM_CONQ.add(u); }
    public static void revokePermanentConq(UUID u) { PERM_CONQ.remove(u); }
    public static void applyPermanent(ServerPlayerEntity p) { if (hasPermanentConq(p.getUuid())) { get(p).conquerorsUnlocked = true; } }
    public static void removeConquerors(ServerPlayerEntity p) { get(p).conquerorsUnlocked = false; }

    public static float getArmamentProgress(PlayerEntity p) { return get(p).meleeDamageDealt; }
    public static boolean isArmamentUnlocked(PlayerEntity p) { return get(p).armamentUnlocked; }
    public static float getObservationProgress(PlayerEntity p) { return get(p).damageTakenTotal; }
    public static boolean isObservationUnlocked(PlayerEntity p) { return get(p).observationUnlocked; }
    public static void unlockConquerorsTemp(PlayerEntity p) { PlayerHaki d = get(p); d.conquerorsUnlocked = true; p.sendMessage(Text.literal("§5Conqueror's Haki awakened!"), false); }

    // ------------- Commands -------------
    public static boolean cmdUnlock(ServerPlayerEntity target, String type) {
        PlayerHaki d = get(target);
        switch (type.toLowerCase()) {
            case "armament" -> d.armamentUnlocked = true;
            case "observation" -> d.observationUnlocked = true;
            case "conquerors", "conqueror", "conqueror's" -> { 
                // Disabled: Conqueror's can only be granted via King's Eye
                target.sendMessage(Text.literal("§cConqueror's Haki can only be unlocked via King's Eye."), false);
                return false;
            }
            default -> { return false; }
        }
        target.sendMessage(Text.literal("§aUnlocked Haki: " + type), false);
        return true;
    }

    public static void cmdUnlockAll(ServerPlayerEntity target) {
        PlayerHaki d = get(target);
        d.armamentUnlocked = true;
        d.observationUnlocked = true;
        // d.conquerorsUnlocked remains unchanged here
        target.sendMessage(Text.literal("§aUnlocked Armament and Observation."), false);
    }

    public static boolean cmdReset(ServerPlayerEntity target, String type) {
        PlayerHaki d = get(target);
        switch (type.toLowerCase()) {
            case "armament" -> { d.armLevel = 1; d.armExp = 0; d.armReq = 100; }
            case "observation" -> { d.obsLevel = 1; d.obsExp = 0; d.obsReq = 100; }
            case "conquerors", "conqueror", "conqueror's" -> { d.conqLevel = 1; d.conqExp = 0; d.conqReq = 100; }
            default -> { return false; }
        }
        target.sendMessage(Text.literal("§cReset Haki progress: " + type), false);
        return true;
    }
}