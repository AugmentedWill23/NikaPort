package nika_nika_fruit_v7tnmscy.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class EntityKillTrackingMixin {
    
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void nikaNika$trackEntityKills(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity killedEntity = (LivingEntity)(Object)this;
        
        // Check if killer is a devil fruit or Haki user player
        if (damageSource.getAttacker() instanceof PlayerEntity killer && 
            (NikaNikaFruitItem.isPlayerDevilFruitUser(killer) || nika_nika_fruit_v7tnmscy.DevilFruitRegistry.hasAnyFruit(killer) || nika_nika_fruit_v7tnmscy.haki.HakiSystem.hasAnyUnlocked(killer)) &&
            killedEntity != killer) { // Don't give EXP for suicide
            
            // Award kill EXP
            NikaNikaFruitItem.addKillExp(killer, killedEntity);
            // DarkxQuake awakening requires player kills
            if (killedEntity instanceof PlayerEntity) {
                if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(killer, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE)
                    && killer.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.DARKNESS)) {
                    NikaNikaFruitItem.recordDxqPlayerKill(killer);
                }
                // Dark unique challenge: track player kills while owning Dark
                if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(killer, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARK)) {
                    NikaNikaFruitItem.recordDarkPlayerKill(killer);
                }
                // Haki: feed Conqueror unlock tracker
                nika_nika_fruit_v7tnmscy.haki.HakiSystem.onPlayerKill(killer, killedEntity);
            }
            // Quake unique challenge: record boss kills
            var type = killedEntity.getType();
            if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(killer, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE)) {
                if (type == net.minecraft.entity.EntityType.WARDEN || type == net.minecraft.entity.EntityType.WITHER || type == net.minecraft.entity.EntityType.ELDER_GUARDIAN) {
                    NikaNikaFruitItem.recordQuakeBossKill(killer);
                }
            }
        }
    }
}