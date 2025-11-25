package nika_nika_fruit_v7tnmscy.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;

public class DevilFruitKnowledgeBookItem extends Item {
    public DevilFruitKnowledgeBookItem(Settings settings) { super(settings); }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            // Title
            user.sendMessage(Text.literal("§6§lThe Devil Fruits Guide"), false);

            // Controls
            user.sendMessage(Text.literal("§eControls:"), false);
            user.sendMessage(Text.literal("  §7R: §fCycle techniques"), false);
            user.sendMessage(Text.literal("  §7Z: §fUse selected move"), false);
            user.sendMessage(Text.literal("  §7X: §fQuick‑use current move (bind with info overlay)"), false);
            user.sendMessage(Text.literal("  §7O: §fView concise ultimate requirements"), false);
            user.sendMessage(Text.literal("  §7Right Shift: §fTrigger Awakening/Ultimate (if your fruit uses it)"), false);
            user.sendMessage(Text.literal("  §7H (Haki): §fSelect move  •  §7G: §fUse  •  §7Up/Down: §fSelect type"), false);

            // Fruits overview
            user.sendMessage(Text.literal("§eFruits & Playstyle:"), false);
            user.sendMessage(Text.literal("  §fNika (Rubber): versatile brawler. Gear 5 Awakening with god‑tier moves."), false);
            user.sendMessage(Text.literal("  §fDark‑Dark: utility control (pull, slow, blind). §7No transformation ultimate."), false);
            user.sendMessage(Text.literal("  §fQuake‑Quake: raw tremor power. §7Non‑transform ultimate burst."), false);
            user.sendMessage(Text.literal("  §fDarkxQuake: fusion of gravity + tremors. Has Awakening (The True Menace)."), false);
            user.sendMessage(Text.literal("  §fSoul‑Soul: steal enemy souls or reach mastery to unlock ultimate moves."), false);

            // How to get fruits & items
            user.sendMessage(Text.literal("§eGetting Fruits:"), false);
            user.sendMessage(Text.literal("  §fCraft a §6Devil Fruit Chest§f to roll a random fruit."), false);
            user.sendMessage(Text.literal("  §7Recipe: Surround a §dMagic Core§7 with any planks."), false);
            user.sendMessage(Text.literal("  §dMagic Core §7Recipe: §bEnder Pearls§7 and §bDiamonds§7 around a §6Nether Star§7."), false);
            user.sendMessage(Text.literal("  §8Locked Chests §7can be opened with an §eAncient Key§7."), false);
            user.sendMessage(Text.literal("  §4The Devil's Chest §7requires a §cDevil's Key§7."), false);
            user.sendMessage(Text.literal("     §7Crafting: surround an §eAncient Key §7with §cRedstone Blocks§7 (3x3)."), false);

            // Mastery
            user.sendMessage(Text.literal("§eMastery System:"), false);
            user.sendMessage(Text.literal("  §fUse moves and defeat mobs/players to gain Mastery EXP."), false);
            user.sendMessage(Text.literal("  §fEach fruit has its own mastery level and unlocks moves at milestones."), false);
            user.sendMessage(Text.literal("  §fImportant milestones include 25/50/60/75/90/100 (and up to 200 for some fruits)."), false);

            // Ult/awakening requirements
            user.sendMessage(Text.literal("§eAwakening & Ults:"), false);
            user.sendMessage(Text.literal("  §fPress §7O §ffor a condensed checklist in‑game."), false);
            user.sendMessage(Text.literal("  §6Nika: §fUnlock Joyboy (advancement) or reach Mastery 200, then Right Shift."), false);
            user.sendMessage(Text.literal("  §fQuake: §fNon‑transform ult. Either Mastery 60 or complete SMP challenges."), false);
            user.sendMessage(Text.literal("     §7Challenges: Die to Drowned (Trident) ×3 and die with Dolphin's Grace ×5."), false);
            user.sendMessage(Text.literal("  §8Dark: §7No transformation ultimate. Focus on control and utility moves."), false);
            user.sendMessage(Text.literal("  §8DarkxQuake: §fAwakens via PvP: get 3 player kills while you are under Darkness."), false);
            user.sendMessage(Text.literal("  §bSoul: §fNot a transform ult. Steal 3 player souls or reach Mastery 200 to unlock ultimate moves."), false);

            // Haki
            user.sendMessage(Text.literal("§eHaki:"), false);
            user.sendMessage(Text.literal("  §fArmament/Observation unlock via progression & books. Use H/G/Up/Down to control."), false);

            // Safety & tips
            user.sendMessage(Text.literal("§eTips:"), false);
            user.sendMessage(Text.literal("  §cYou can only have one Devil Fruit. Eating a second will kill you."), false);
            user.sendMessage(Text.literal("  §fUse §7/fruit give§f, §7/nika addexp§f etc. if you are an admin for testing."), false);
            user.sendMessage(Text.literal("  §fUse Mysterious Brew to reset fruit state if needed."), false);

            user.sendMessage(Text.literal("§7(Use this book anytime to re‑read the guide.)"), false);

            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 0.8F, 1.0F);
        }
        return ActionResult.SUCCESS;
    }
}
