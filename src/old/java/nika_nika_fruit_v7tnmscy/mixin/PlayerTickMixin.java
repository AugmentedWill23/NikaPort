package nika_nika_fruit_v7tnmscy.mixin;

import net.minecraft.entity.player.PlayerEntity;
import nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem;
import nika_nika_fruit_v7tnmscy.DevilFruitRegistry;
import nika_nika_fruit_v7tnmscy.NikaNikaFruit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerTickMixin {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void nikaNika$checkTransformationExpiry(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        
        // Only check on server side every 20 ticks (once per second)
        if (!player.getWorld().isClient && player.getWorld().getTime() % 20 == 0) {
            // Haki passive tick
            nika_nika_fruit_v7tnmscy.haki.HakiSystem.onServerTick(player);

            // Awakening Crystal offhand: double mastery, consume, grant Strength II
            var off = player.getOffHandStack();
            if (!off.isEmpty() && off.isOf(nika_nika_fruit_v7tnmscy.NikaNikaFruit.AWAKENING_CRYSTAL_ITEM)) {
                int oldLv = nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.getMasteryLevel(player);
                int newLv = Math.min(200, Math.max(1, oldLv * 2));
                nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.setMasteryLevel(player, newLv);
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.STRENGTH, 6000, 1)); // 5m Strength II
                off.decrement(1);
                player.sendMessage(net.minecraft.text.Text.literal("§dAwakening Crystal used: Mastery doubled to Lv " + newLv + " and Strength II granted!"), false);
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sound.SoundEvents.ITEM_TOTEM_USE, net.minecraft.sound.SoundCategory.PLAYERS, 0.8F, 1.4F);
            }
            if (NikaNikaFruitItem.isPlayerDevilFruitUser(player)) {
                NikaNikaFruitItem.checkTransformationExpiry(player);
            }
            
            // Enforce duplicate fruit possession death rule
            String owned = DevilFruitRegistry.getPlayerFruit(player);
            if (owned != null) {
                // If player holds any fruit item they do not own and that fruit is globally owned by someone else
                boolean shouldDie = false;
                var inv = player.getInventory();
                for (int i = 0; i < inv.size(); i++) {
                    var s = inv.getStack(i);
                    if (s.isEmpty()) continue;
                    if (s.isOf(NikaNikaFruit.NikaNikaFruit_ITEM) && !DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_NIKA) && DevilFruitRegistry.isFruitOwned(DevilFruitRegistry.FRUIT_NIKA)) {
                        shouldDie = true; break;
                    }
                    if (NikaNikaFruit.DARK_FRUIT_ITEM != null && s.isOf(NikaNikaFruit.DARK_FRUIT_ITEM) && !DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_DARK) && DevilFruitRegistry.isFruitOwned(DevilFruitRegistry.FRUIT_DARK)) {
                        shouldDie = true; break;
                    }
                    if (NikaNikaFruit.QUAKE_FRUIT_ITEM != null && s.isOf(NikaNikaFruit.QUAKE_FRUIT_ITEM) && !DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_QUAKE) && DevilFruitRegistry.isFruitOwned(DevilFruitRegistry.FRUIT_QUAKE)) {
                        shouldDie = true; break;
                    }
                    if (NikaNikaFruit.DARKXQUAKE_FRUIT_ITEM != null && s.isOf(NikaNikaFruit.DARKXQUAKE_FRUIT_ITEM) && !DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_DARKXQUAKE) && DevilFruitRegistry.isFruitOwned(DevilFruitRegistry.FRUIT_DARKXQUAKE)) {
                        shouldDie = true; break;
                    }
                }
                if (shouldDie) {
                    player.damage(((net.minecraft.server.world.ServerWorld)player.getWorld()), ((net.minecraft.server.world.ServerWorld)player.getWorld()).getDamageSources().outOfWorld(), Float.MAX_VALUE);
                }
            }

            // Passive Perk: Gravity Distortion (DarkxQuake only)
            if (DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_DARKXQUAKE)) {
                // Every 30 seconds (600 ticks), subtle rumble and micro pulls
                if (player.getWorld().getTime() % 600 == 0) {
                    var sw = (net.minecraft.server.world.ServerWorld) player.getWorld();
                    // Low bass rumble
                    sw.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sound.SoundEvents.ENTITY_WARDEN_HEARTBEAT, net.minecraft.sound.SoundCategory.PLAYERS, 0.6F, 0.5F);
                    // Gentle pull of nearby mobs toward the player to simulate gravity shifts
                    sw.getOtherEntities(player, player.getBoundingBox().expand(8.0), e -> e instanceof net.minecraft.entity.LivingEntity && e != player)
                      .forEach(e -> {
                          var dir = player.getPos().subtract(e.getPos()).normalize().multiply(0.05);
                          e.addVelocity(dir.x, 0.02, dir.z);
                          e.velocityModified = true;
                      });
                    // Ambient particles
                    sw.spawnParticles(net.minecraft.particle.ParticleTypes.POOF, player.getX(), player.getY() + 0.5, player.getZ(), 8, 0.6, 0.4, 0.6, 0.02);
                }
            }

            // Dragon passives and Dragon's Fury effects
            if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DRAGON)) {
                // Always-on Resistance I
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.RESISTANCE, 60, 0, true, false));
                // Rain gives Slowness I
                if (player.getWorld().isRaining() && player.getWorld().isSkyVisible(player.getBlockPos())) {
                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.SLOWNESS, 40, 0, true, false));
                }
                // Dragon's Fury contact burn and rotating fire sphere while transformed
                if (nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.isPlayerTransformed(player)) {
                    long t = player.getWorld().getTime();
                    double radius = 3.0 + (player.getRandom().nextDouble() * 0.2);
                    int count = 24;
                    for (int i = 0; i < count; i++) {
                        double base = (2 * Math.PI * i) / count;
                        double spin = (t % 20) * 0.15; // different each second
                        double ang = base + spin;
                        double x = player.getX() + radius * Math.cos(ang);
                        double z = player.getZ() + radius * Math.sin(ang);
                        ((net.minecraft.server.world.ServerWorld)player.getWorld()).spawnParticles(net.minecraft.particle.ParticleTypes.FLAME, x, player.getY() + 1.0, z, 1, 0.02,0.02,0.02, 0.0);
                    }
                    // Contact burn unless Observation Haki unlocked
                    var sw = (net.minecraft.server.world.ServerWorld) player.getWorld();
                    sw.getOtherEntities(player, player.getBoundingBox().expand(3.2), e -> e instanceof net.minecraft.entity.LivingEntity && e != player).forEach(e -> {
                        net.minecraft.entity.LivingEntity le = (net.minecraft.entity.LivingEntity)e;
                        boolean obs = (e instanceof net.minecraft.entity.player.PlayerEntity pe) && nika_nika_fruit_v7tnmscy.haki.HakiSystem.isObservationUnlocked(pe);
                        if (!obs) {
                            le.setOnFireFor(3);
                            le.damage(sw, sw.getDamageSources().playerAttack(player), 4.0f);
                        }
                    });
                }
            }

            // Operation passive Room upkeep: maintain a baseline 10-block room, expanded when active
            if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_OPERATION)) {
                // Visual subtle particles to indicate room edge every 2s when active room
                var dataPlayer = nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.class; // placeholder reference
                // Nothing heavy here; moves handle effects
            }
        }
    }
}