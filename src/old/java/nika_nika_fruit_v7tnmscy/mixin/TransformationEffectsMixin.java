package nika_nika_fruit_v7tnmscy.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class TransformationEffectsMixin {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void nikaNika$showTransformationEffects(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        
        // Only process on server side
        if (player.getWorld().isClient) return;
        
        if (NikaNikaFruitItem.isPlayerDevilFruitUser(player) && 
            NikaNikaFruitItem.isPlayerTransformed(player)) {
            
            // Show white sparks and particles every 10 ticks (0.5 seconds)
            if (player.age % 10 == 0) {
                ServerWorld serverWorld = (ServerWorld) player.getWorld();
                
                // White sparks around player
                serverWorld.spawnParticles(ParticleTypes.END_ROD,
                    player.getX() + (player.getRandom().nextDouble() - 0.5) * 2.0,
                    player.getY() + player.getRandom().nextDouble() * 2.0,
                    player.getZ() + (player.getRandom().nextDouble() - 0.5) * 2.0,
                    1, 0.0, 0.0, 0.0, 0.05);
                
                // Electric sparks
                if (player.age % 20 == 0) { // Less frequent
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        player.getX(),
                        player.getY() + 1.0,
                        player.getZ(),
                        3, 0.5, 0.5, 0.5, 0.1);
                }
            }
        }
    }
}