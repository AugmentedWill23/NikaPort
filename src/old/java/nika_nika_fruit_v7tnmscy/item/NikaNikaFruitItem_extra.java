// Helper for per-fruit milestones if needed elsewhere
package nika_nika_fruit_v7tnmscy.item;

import nika_nika_fruit_v7tnmscy.DevilFruitRegistry;

public class NikaNikaFruitItem_extra {
    public static int[] getMilestonesForFruit(String fruit) {
        if (DevilFruitRegistry.FRUIT_DARKXQUAKE.equals(fruit)) return new int[]{1,3,5,10,15,20,25,50,60,75,90,100};
        if (DevilFruitRegistry.FRUIT_QUAKE.equals(fruit)) return new int[]{1,5,10,26,50,51,60,75,90,100};
        if (DevilFruitRegistry.FRUIT_DARK.equals(fruit)) return new int[]{1,5,26,50,51,60,75,90,100};
        return new int[]{1,3,5,10,15,20,25,50,60,75,90,100};
    }
}