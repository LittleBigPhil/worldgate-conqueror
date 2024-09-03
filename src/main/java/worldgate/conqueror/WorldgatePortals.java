package worldgate.conqueror;

import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import worldgate.conqueror.block.ModBlocks;
import worldgate.conqueror.item.ModItems;

public class WorldgatePortals {

    public static void registerPortals() {
        CustomPortalBuilder.beginPortal()
                .frameBlock(Blocks.COBBLESTONE)
                .lightWithItem(ModItems.DOWSING_ROD)
                .destDimID(Identifier.of(WorldgateConqueror.MOD_ID, "cave_world"))
                .onlyLightInOverworld()
                .tintColor(128,128,128)
                .registerPortal();

        CustomPortalBuilder.beginPortal()
                .frameBlock(ModBlocks.SANDSTONE)
                .lightWithItem(ModItems.DOWSING_ROD)
                .destDimID(Identifier.of(WorldgateConqueror.MOD_ID, "desert_world"))
                .onlyLightInOverworld()
                .tintColor(205,195,180)
                .registerPortal();

        CustomPortalBuilder.beginPortal()
                .frameBlock(Blocks.GLASS)
                .lightWithItem(ModItems.DOWSING_ROD)
                .destDimID(Identifier.of(WorldgateConqueror.MOD_ID, "crystal_world"))
                .onlyLightInOverworld()
                .tintColor(250,250,250)
                .registerPortal();

        CustomPortalBuilder.beginPortal()
                .frameBlock(ModBlocks.LOG)
                .lightWithItem(ModItems.DOWSING_ROD)
                .destDimID(Identifier.of(WorldgateConqueror.MOD_ID, "forest_world"))
                .onlyLightInOverworld()
                .tintColor(105,95,80)
                .registerPortal();
    }
}
