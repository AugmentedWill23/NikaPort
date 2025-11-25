package nika_nika_fruit_v7tnmscy.mixin;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem;
import nika_nika_fruit_v7tnmscy.DevilFruitRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class WaterWeaknessMixin {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void nikaNika$applyWaterWeakness(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        
        if (!NikaNikaFruitItem.isPlayerDevilFruitUser(player) && !DevilFruitRegistry.hasAnyFruit(player)) return;
        // Operation fruit can walk on water without debuffs
        if (DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_OPERATION)) return;
        
        // Check if player is in water
        if (player.isSubmergedInWater() || player.isTouchingWater()) {
            // Devil Fruit weakness: cannot swim, Slowness V, strong sinking
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 4, false, false)); // V (amplifier 4)
            player.setSprinting(false);
            player.setSwimming(false);
            if (player.isSubmergedInWater()) {
                player.addVelocity(0, -0.15, 0);
                player.velocityModified = true;
            }
        }
    }
}