package worldgate.conqueror.mixin.mob;

import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PathAwareEntity.class)
public class PathAwareEntityMixin {
    @Overwrite
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        // Normally requires pathfinding favor to be greater than 0 to spawn, but this prevents hostile mobs from spawning in daylight
        return true;
    }
}
