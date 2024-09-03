package worldgate.conqueror.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowyBlock;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import worldgate.conqueror.block.ModBlocks;

@Mixin(SpreadableBlock.class)
public abstract class SpreadableBlockMixin extends SnowyBlock {
    public SpreadableBlockMixin(Settings settings) {
        super(settings);
    }

    @Shadow private static boolean canSurvive(BlockState state, WorldView world, BlockPos pos) { throw new AssertionError(); }
    @Shadow private static boolean canSpread(BlockState state, WorldView world, BlockPos pos) { throw new AssertionError(); }

    @Overwrite
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!canSurvive(state, world, pos)) {
            world.setBlockState(pos, ModBlocks.SOIL.getDefaultState());
        } else {
            if (world.getLightLevel(pos.up()) >= 9) {
                BlockState blockState = this.getDefaultState();

                for(int i = 0; i < 4; ++i) {
                    BlockPos blockPos = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                    if (world.getBlockState(blockPos).isOf(ModBlocks.SOIL) && canSpread(blockState, world, blockPos)) {
                        world.setBlockState(blockPos, (BlockState)blockState.with(SNOWY, world.getBlockState(blockPos.up()).isOf(Blocks.SNOW)));
                    }
                }
            }

        }
    }


}
