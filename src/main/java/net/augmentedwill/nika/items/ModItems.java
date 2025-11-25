package net.augmentedwill.nika.items;

import net.augmentedwill.nika.NikaFruit;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

interface ModItems {
    DeferredRegister.Items ITEMS = DeferredRegister.createItems(NikaFruit.MOD_ID);


    //TODO: Actually make the classes
    DeferredItem<Item> NIKA_NIKA = item("nika_nika_fruit", new Item(new Item.Properties().stacksTo(1)));
    DeferredItem<Item> DARK = item("dark_fruit", new Item(new Item.Properties().stacksTo(1)));
    DeferredItem<Item> QUAKE = item("quake_fruit", new Item(new Item.Properties().stacksTo(1)));
    DeferredItem<Item> DARKXQUAKE = item("dark_fruit", new Item(new Item.Properties().stacksTo(1)));
    DeferredItem<Item> DARK_FRUIT_ITEM = item("dark_fruit", new Item(new Item.Properties().stacksTo(1)));
    DeferredItem<Item> DARK_FRUIT_ITEM = item("dark_fruit", new Item(new Item.Properties().stacksTo(1)));





    private static DeferredItem<Item> item(String name, Item item) {
        return ITEMS.register(name, () -> item);
    }

    static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
