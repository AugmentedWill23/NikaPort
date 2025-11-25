package nika_nika_fruit_v7tnmscy.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerDeathMixin {
    
    @Inject(method = "onDeath", at = @At("TAIL"))
    private void nikaNika$deactivateTransformationOnDeath(DamageSource damageSource, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        
        if (NikaNikaFruitItem.isPlayerDevilFruitUser(player) && 
            NikaNikaFruitItem.isPlayerTransformed(player)) {
            // Deactivate transformation on death
            NikaNikaFruitItem.deactivateTransformation(player);
        }
        // Reset DarkxQuake combo on death
        NikaNikaFruitItem.resetDxqCombo(player);
        // Haki observation chain breaks on death
        nika_nika_fruit_v7tnmscy.haki.HakiSystem.onPlayerDeath(player);

        // 0.1% chance to unlock Joyboy on death for Nika users
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_NIKA)) {
            if (((ServerWorld)player.getWorld()).random.nextFloat() < 0.001f) {
                nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.grantAdvancement(player, "joyboy");
            }
        }


        // Conqueror's Haki chance unlocks
        if (!player.getWorld().isClient) {
            net.minecraft.entity.Entity attacker = damageSource.getAttacker();
            if (attacker instanceof net.minecraft.entity.boss.WitherEntity || attacker instanceof net.minecraft.entity.mob.WardenEntity) {
                if (((ServerWorld)player.getWorld()).random.nextFloat() < 0.00001f) { // 0.001%
                    nika_nika_fruit_v7tnmscy.haki.HakiSystem.unlockConquerorsTemp(player);
                }
            }
            if (attacker instanceof net.minecraft.entity.boss.dragon.EnderDragonEntity) {
                if (((ServerWorld)player.getWorld()).random.nextFloat() < 0.000001f) { // 0.0001%
                    nika_nika_fruit_v7tnmscy.haki.HakiSystem.grantPermanentConq(player.getUuid());
                    nika_nika_fruit_v7tnmscy.haki.HakiSystem.applyPermanent((net.minecraft.server.network.ServerPlayerEntity)player);
                    player.sendMessage(net.minecraft.text.Text.literal("§5Your will transcends worlds. Conqueror's Haki is yours forever."), false);
                }
            }
        }

        // Fusion unlock rule: If player owns Dark-Dark Fruit and dies while holding Quake-Quake in offhand, unlock fusion
        if (!player.getWorld().isClient) {
            String owned = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.getPlayerFruit(player);
            if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARK.equals(owned)) {
                // Rule: Keep Inventory must be ON, player must have Quake in inventory (any slot), and have at least 25 mastery
                boolean keepInv = ((ServerWorld)player.getWorld()).getGameRules().getBoolean(net.minecraft.world.GameRules.KEEP_INVENTORY);
                boolean hasQuakeInInventory = false;
                for (int i = 0; i < player.getInventory().size(); i++) {
                    var s = player.getInventory().getStack(i);
                    if (!s.isEmpty() && s.isOf(nika_nika_fruit_v7tnmscy.NikaNikaFruit.QUAKE_FRUIT_ITEM)) { hasQuakeInInventory = true; break; }
                }
                boolean hasRequiredMastery = nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.getMasteryLevel(player) >= 25;
                if (keepInv && hasQuakeInInventory && hasRequiredMastery) {
                    // Mark unlock and grant advancement only for the first player
                    nika_nika_fruit_v7tnmscy.DevilFruitRegistry.unlockDarkxQuake(player);
                }
            }
            // Awakening alt unlock: dying to a Warden while under Darkness/Blindness
            if (damageSource.getAttacker() instanceof net.minecraft.entity.mob.WardenEntity) {
                if (player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.DARKNESS) || player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.BLINDNESS)) {
                    nika_nika_fruit_v7tnmscy.DevilFruitRegistry.unlockDarkxQuakeAwakening(player);
                }
            }

            // Dark ult removed: no Darkness-death tracking

            // Quake ult tracking: death to Drowned with Trident and death with Dolphin's Grace
            if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE)) {
                // Dolphin's Grace death condition
                if (player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.DOLPHINS_GRACE)) {
                    nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.recordDeathWithDolphinsGrace(player);
                }
                // Drowned with Trident death condition
                net.minecraft.entity.Entity attacker = damageSource.getAttacker();
                boolean drownedTrident = false;
                if (attacker instanceof net.minecraft.entity.mob.DrownedEntity drowned) {
                    net.minecraft.item.ItemStack main = drowned.getMainHandStack();
                    net.minecraft.item.ItemStack off = drowned.getOffHandStack();
                    if ((!main.isEmpty() && main.isOf(net.minecraft.item.Items.TRIDENT)) ||
                        (!off.isEmpty() && off.isOf(net.minecraft.item.Items.TRIDENT))) {
                        drownedTrident = true;
                    }
                } else if (attacker instanceof net.minecraft.entity.projectile.TridentEntity trident) {
                    net.minecraft.entity.Entity owner = trident.getOwner();
                    if (owner instanceof net.minecraft.entity.mob.DrownedEntity) {
                        drownedTrident = true;
                    }
                }
                if (drownedTrident) {
                    nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.recordDeathToDrownedTrident(player);
                }
            }
        }
    }
}