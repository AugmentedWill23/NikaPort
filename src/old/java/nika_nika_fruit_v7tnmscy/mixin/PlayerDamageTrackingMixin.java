package nika_nika_fruit_v7tnmscy.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class PlayerDamageTrackingMixin {
    private static final ThreadLocal<Boolean> HAKI_BONUS_APPLYING = ThreadLocal.withInitial(() -> false);
    
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void nikaNika$trackDamageAndImmunity(ServerWorld world, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        
        // Track damage dealt for attacker players (any devil fruit or Haki owner)
        Entity attackerEntity = damageSource.getAttacker();
        if (attackerEntity instanceof PlayerEntity attacker && attacker != self && (NikaNikaFruitItem.isPlayerDevilFruitUser(attacker) || nika_nika_fruit_v7tnmscy.DevilFruitRegistry.hasAnyFruit(attacker) || nika_nika_fruit_v7tnmscy.haki.HakiSystem.hasAnyUnlocked(attacker))) {
            NikaNikaFruitItem.addDamageDealt(attacker, amount);
            // Haki: track armament unlock/progression via melee damage
            nika_nika_fruit_v7tnmscy.haki.HakiSystem.onPlayerDealMeleeDamage(attacker, amount);
            // Track DarkxQuake consecutive hits (respecting i-frames via method)
            NikaNikaFruitItem.recordDxqHit(attacker);
        }
        
        // Track damage taken for devil fruit users OR Haki users
        if (self instanceof PlayerEntity player && (NikaNikaFruitItem.isPlayerDevilFruitUser(player) || nika_nika_fruit_v7tnmscy.DevilFruitRegistry.hasAnyFruit(player) || nika_nika_fruit_v7tnmscy.haki.HakiSystem.hasAnyUnlocked(player))) {
            // Early Gear 5 unlock on lethal blow when at half a heart, level >= 25, killer is player/hostile, 2% chance
            float currentHealth = player.getHealth();
            boolean lethal = currentHealth - amount <= 0.0f;
            // Nika 0.1% revival on lethal blow: totem-like revive + Gear Five activation + cooldown reset
            if (lethal && nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_NIKA)) {
                if (world.getRandom().nextFloat() < 0.001f) {
                    // Totem-like revival
                    player.setHealth(1.0f);
                    player.clearStatusEffects();
                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.REGENERATION, 900, 1));
                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.FIRE_RESISTANCE, 800, 0));
                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.ABSORPTION, 100, 1));
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sound.SoundEvents.ITEM_TOTEM_USE, net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
                    if (world instanceof ServerWorld sw) {
                        sw.spawnParticles(net.minecraft.particle.ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1, player.getZ(), 100, 0.5, 1.0, 0.5, 0.5);
                    }
                    // Grant the Sun God advancement and awaken Gear 5, then reset all cooldowns
                    NikaNikaFruitItem.grantAdvancement(player, "nika_the_sun_god");
                    NikaNikaFruitItem.unlockGear5Early(player);
                    NikaNikaFruitItem.resetCooldowns(player);
                    cir.setReturnValue(false);
                    return;
                }
            }
            if (lethal && currentHealth <= 1.0f) {
                Entity attacker = damageSource.getAttacker();
                boolean validKiller = attacker instanceof PlayerEntity || attacker instanceof HostileEntity;
                if (validKiller && NikaNikaFruitItem.getMasteryLevel(player) >= 25 && !NikaNikaFruitItem.isPlayerTransformed(player)) {
                    if (world.getRandom().nextFloat() < 0.02f) {
                        // Prevent death and unlock Gear 5 early
                        NikaNikaFruitItem.unlockGear5Early(player);
                        player.setHealth(1.0f);
                        cir.setReturnValue(false);
                        return;
                    }
                    // else: they die normally unless they already unlocked at 50+ by other means
                }
            }

            // Prevent self-damage from own abilities
            if (damageSource.getAttacker() instanceof PlayerEntity attacker && attacker.equals(player)) {
                // Check if it's from player attack (abilities) but not from explosion/environment
                String sourceName = damageSource.getName();
                if (sourceName.contains("player") || sourceName.contains("explosion")) {
                    // Only cancel if it's the same player (self-damage from abilities)
                    cir.setReturnValue(false);
                    return;
                }
            }
            
            // Observation interaction rules:
            // - If defender's Observation >= 50, only allow hits from players with Conqueror's unlocked OR Armament toggled ON.
            // - At Observation >= 100, Armament must be within 50 levels of the defender's Observation (>= obsLevel - 50). Conqueror's always bypasses.
            if (attackerEntity instanceof PlayerEntity ap) {
                int obsLevel = nika_nika_fruit_v7tnmscy.haki.HakiSystem.getObservationLevel(player);
                if (obsLevel >= 50) {
                    boolean hasConq = nika_nika_fruit_v7tnmscy.haki.HakiSystem.hasConquerorsUnlocked(ap);
                    boolean armOn = nika_nika_fruit_v7tnmscy.haki.HakiSystem.isArmamentActive(ap);
                    int armLv = nika_nika_fruit_v7tnmscy.haki.HakiSystem.getArmamentLevel(ap);
                    boolean allowed = false;
                    if (hasConq) {
                        allowed = true; // no mastery gate for Conqueror's
                    } else if (armOn) {
                        allowed = obsLevel < 100 || armLv >= (obsLevel - 50);
                    }
                    if (!allowed) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
                // If allowed, Observation dodge may still cancel the damage
                if (nika_nika_fruit_v7tnmscy.haki.HakiSystem.tryObservationDodge(player, world.getTime())) {
                    cir.setReturnValue(false);
                    return;
                }
                // Armament damage bonus when toggled ON (applies extra flat damage). Guard recursion.
                if (!HAKI_BONUS_APPLYING.get()) {
                    float bonus = nika_nika_fruit_v7tnmscy.haki.HakiSystem.armamentBonus(ap);
                    if (bonus > 0f) {
                        try {
                            HAKI_BONUS_APPLYING.set(true);
                            ((LivingEntity)(Object)this).damage(world, world.getDamageSources().playerAttack(ap), bonus);
                        } finally {
                            HAKI_BONUS_APPLYING.set(false);
                        }
                    }
                }
            } else {
                // Non-player attackers: Observation dodge may cancel the hit
                if (nika_nika_fruit_v7tnmscy.haki.HakiSystem.tryObservationDodge(player, world.getTime())) {
                    cir.setReturnValue(false);
                    return;
                }
            }

            // Soul fruit Spirit Guard projectile block
            if (nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.tryBlockProjectileWithSoulGuard(player, damageSource.getName())) {
                cir.setReturnValue(false);
                return;
            }
            // Dragon fire/heat immunity
            if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DRAGON)) {
                String s = damageSource.getName();
                if (s.contains("fire") || s.contains("lava") || s.contains("hot")) {
                    cir.setReturnValue(false);
                    return;
                }
                // Extra fall damage: apply 2 health penalty in addition to normal fall damage
                if (s.contains("fall")) {
                    float newHp = Math.max(0.0f, player.getHealth() - 2.0f);
                    player.setHealth(newHp);
                }
            }
            // Check for projectile/lightning immunity during transformation
            if (NikaNikaFruitItem.isPlayerTransformed(player)) {
                String sourceName = damageSource.getName();
                // Check for lightning and projectile damage
                if (sourceName.contains("lightning") ||
                    sourceName.contains("arrow") ||
                    sourceName.contains("projectile") ||
                    sourceName.contains("trident") ||
                    sourceName.contains("fireball") ||
                    sourceName.contains("thrown")) {
                    cir.setReturnValue(false); // Cancel damage
                    return;
                }
            }
            
            // Track damage taken (only if not self-inflicted)
            if (!(damageSource.getAttacker() instanceof PlayerEntity attacker && attacker.equals(player))) {
                NikaNikaFruitItem.addDamageTaken(player, amount);
                // Haki: observation unlock/progression via damage taken without dying
                nika_nika_fruit_v7tnmscy.haki.HakiSystem.onPlayerTakeDamage(player, amount);
            }
        }
    }
}