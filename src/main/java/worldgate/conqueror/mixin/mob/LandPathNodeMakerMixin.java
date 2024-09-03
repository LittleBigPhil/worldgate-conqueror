package worldgate.conqueror.mixin.mob;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldgate.conqueror.block.ModBlocks;

@Mixin(LandPathNodeMaker.class)
public class LandPathNodeMakerMixin {

    @Inject(method="getCommonNodeType", at=@At("RETURN"), cancellable = true)
    private static void getCommonNodeType(BlockView world, BlockPos pos, CallbackInfoReturnable<PathNodeType> cir) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isOf(ModBlocks.AGAVE_PLANT) || blockState.isOf(ModBlocks.AGAVE_LEAF)) {
            cir.setReturnValue(PathNodeType.DAMAGE_OTHER);
        }
    }
}
