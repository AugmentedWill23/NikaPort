package nika_nika_fruit_v7tnmscy.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import nika_nika_fruit_v7tnmscy.NikaNikaFruit;

import java.util.ArrayList;
import java.util.List;

public class DevilFruitChestItem extends Item {
    public DevilFruitChestItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            boolean lockedScenario = false; // normal chest never locks now (renamed to Normal Chest)
            boolean opened = true;

            if (opened) {
                giveChestRewards(world, user, lockedScenario);
            }

            // Count openings for awakening crystal recipe unlock (only count successful opens)
            if (opened && user instanceof ServerPlayerEntity sp) {
                int count = (int) sp.getCommandTags().stream().filter(s -> s.startsWith("dfc_opened_")).count();
                String nextTag = "dfc_opened_" + (count + 1);
                sp.addCommandTag(nextTag);
                if (count + 1 >= 23) {
                    sp.getRecipeBook().unlock(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.RECIPE, net.minecraft.util.Identifier.of(NikaNikaFruit.MOD_ID, "awakening_crystal")));
                }
            }

            if (!user.isCreative()) {
                stack.decrement(1);
            }
        }

        return ActionResult.SUCCESS;
    }

    private void giveChestRewards(World world, PlayerEntity user, boolean locked) {
        java.util.Random rand = new java.util.Random();
        double fruitChance = 0.0; // No fruits in normal chest
        double brewChance = locked ? 0.45 : 0.30;
        double netheriteIngotChance = locked ? 0.10 : 0.03; // max 1 per open

        boolean gaveFruit = false;

        // Brews
        if (!gaveFruit && rand.nextDouble() < brewChance) {
            int count = 1 + rand.nextInt(3);
            insertOrDrop(user, new ItemStack(NikaNikaFruit.MYSTERIOUS_BREW_ITEM, count));
        }

        // Valuable gear pool
        java.util.List<Item> pool = new java.util.ArrayList<>();
        // Iron tools and weapons (diamonds are extremely rare and not in the common pool)
        pool.add(net.minecraft.item.Items.IRON_SWORD);
        pool.add(net.minecraft.item.Items.IRON_PICKAXE);
        pool.add(net.minecraft.item.Items.IRON_AXE);
        pool.add(net.minecraft.item.Items.IRON_SHOVEL);
        // Ranged
        pool.add(net.minecraft.item.Items.BOW);
        pool.add(net.minecraft.item.Items.CROSSBOW);
        pool.add(net.minecraft.item.Items.ARROW);
        pool.add(net.minecraft.item.Items.POTION);
        if (locked) {
            // Small chance for netherite gear in locked scenario
            if (rand.nextDouble() < 0.08) insertOrDrop(user, new ItemStack(net.minecraft.item.Items.NETHERITE_SWORD));
            if (rand.nextDouble() < 0.06) insertOrDrop(user, new ItemStack(net.minecraft.item.Items.NETHERITE_PICKAXE));
        }

        // Always give 1-3 items from pool
        int items = 1 + rand.nextInt(3);
        for (int i = 0; i < items; i++) {
            Item it = pool.get(rand.nextInt(pool.size()));
            insertOrDrop(user, new ItemStack(it));
        }

        // Extremely low chance for single diamond tool (3rd rarest overall)
        if (rand.nextDouble() < 0.02) {
            net.minecraft.item.Item[] diamonds = new net.minecraft.item.Item[] {
                net.minecraft.item.Items.DIAMOND_SWORD,
                net.minecraft.item.Items.DIAMOND_PICKAXE,
                net.minecraft.item.Items.DIAMOND_AXE,
                net.minecraft.item.Items.DIAMOND_SHOVEL
            };
            insertOrDrop(user, new ItemStack(diamonds[rand.nextInt(diamonds.length)]));
        }
        // Low chance for 1 netherite ingot
        if (rand.nextDouble() < netheriteIngotChance) {
            insertOrDrop(user, new ItemStack(net.minecraft.item.Items.NETHERITE_INGOT));
        }
        // Extremely low chance to drop a key from normal chests
        if (!locked && rand.nextDouble() < 0.005) { // 0.5%
            insertOrDrop(user, new ItemStack(NikaNikaFruit.KEY_ITEM));
            user.sendMessage(Text.literal("§7You found an Ancient Key!"), false);
        }

        // Tiny chance to also include a fruit in locked scenario if not given
        if (locked && !gaveFruit && rand.nextDouble() < 0.04) {
            Item fruit = NikaNikaFruit.DARKXQUAKE_FRUIT_ITEM; // slightly juicier
            insertOrDrop(user, new ItemStack(fruit));
            user.sendMessage(Text.literal("§8Locked trove revealed a rare fruit!"), false);
        }

        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.UI_LOOM_TAKE_RESULT, SoundCategory.PLAYERS, 1.0F, locked ? 0.8F : 1.2F);
    }

    private void insertOrDrop(PlayerEntity user, ItemStack reward) {
        if (!user.getInventory().insertStack(reward)) {
            user.dropItem(reward, false);
        }
    }
}