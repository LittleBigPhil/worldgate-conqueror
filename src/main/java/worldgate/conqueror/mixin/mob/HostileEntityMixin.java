package worldgate.conqueror.mixin.mob;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HostileEntity.class)
public class HostileEntityMixin {
    @Overwrite
    public static boolean isSpawnDark(ServerWorldAccess world, BlockPos pos, Random random) {
        // See also PathAwareEntityMixin.canSpawn
        return ignoreSun(world, pos, random);
    }

    @Unique private static boolean alwaysDark(ServerWorldAccess world, BlockPos pos, Random random) {
        // Always return true, required for allowing spawns in any light level
        return true;
    }
    @Unique private static boolean ignoreSun(ServerWorldAccess world, BlockPos pos, Random random) {
        // Only runs the normal calculations for block light level, so mobs can spawn in daylight, but not in torched areas.
        DimensionType dimensionType = world.getDimension();
        int maxLightLevelOfDarkness = dimensionType.monsterSpawnBlockLightLimit();
        // Removes the condition that prevents a block light limit of 15 from working differently than a limit of 14
        return world.getLightLevel(LightType.BLOCK, pos) <= maxLightLevelOfDarkness;
    }
}
