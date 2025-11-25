package nika_nika_fruit_v7tnmscy.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import nika_nika_fruit_v7tnmscy.DevilFruitRegistry;
import nika_nika_fruit_v7tnmscy.NikaNikaFruit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class FruitPickupMixin {
    @Inject(method = "onPlayerCollision", at = @At("HEAD"))
    private void nikaNika$preventDuplicateFruitPickup(PlayerEntity player, CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack stack = self.getStack();
        if (stack.isOf(NikaNikaFruit.NikaNikaFruit_ITEM)) {
            String fruitId = DevilFruitRegistry.FRUIT_NIKA;
            if (DevilFruitRegistry.isFruitOwned(fruitId) && !DevilFruitRegistry.playerOwnsFruit(player, fruitId)) {
                if (!player.getWorld().isClient) {
                    player.sendMessage(Text.literal("§cWarning: Someone has already eaten this fruit. You cannot possess duplicates."), true);
                    stack.decrement(stack.getCount());
                    self.discard();
                }
            }
        } else if (stack.isOf(NikaNikaFruit.DARK_FRUIT_ITEM)) {
            String fruitId = DevilFruitRegistry.FRUIT_DARK;
            if (DevilFruitRegistry.isFruitOwned(fruitId) && !DevilFruitRegistry.playerOwnsFruit(player, fruitId)) {
                if (!player.getWorld().isClient) {
                    player.sendMessage(Text.literal("§cWarning: Someone has already eaten this fruit. You cannot possess duplicates."), true);
                    stack.decrement(stack.getCount());
                    self.discard();
                }
            }
        } else if (stack.isOf(NikaNikaFruit.QUAKE_FRUIT_ITEM)) {
            String fruitId = DevilFruitRegistry.FRUIT_QUAKE;
            if (DevilFruitRegistry.isFruitOwned(fruitId) && !DevilFruitRegistry.playerOwnsFruit(player, fruitId)) {
                if (!player.getWorld().isClient) {
                    player.sendMessage(Text.literal("§cWarning: Someone has already eaten this fruit. You cannot possess duplicates."), true);
                    stack.decrement(stack.getCount());
                    self.discard();
                }
            }
        } else if (stack.isOf(NikaNikaFruit.DARKXQUAKE_FRUIT_ITEM)) {
            String fruitId = DevilFruitRegistry.FRUIT_DARKXQUAKE;
            if (DevilFruitRegistry.isFruitOwned(fruitId) && !DevilFruitRegistry.playerOwnsFruit(player, fruitId)) {
                if (!player.getWorld().isClient) {
                    player.sendMessage(Text.literal("§cWarning: Someone has already eaten this fruit. You cannot possess duplicates."), true);
                    stack.decrement(stack.getCount());
                    self.discard();
                }
            }
        }
    }
}
