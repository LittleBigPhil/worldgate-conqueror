package worldgate.conqueror.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import worldgate.conqueror.block.AgavePlant;
import worldgate.conqueror.block.ModBlocks;

public class AgavePatchFeature extends Feature<AgavePatchFeatureConfig> {
    // see RandomPatchFeature

    public AgavePatchFeature(Codec<AgavePatchFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<AgavePatchFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos pos = context.getOrigin();
        Random random = context.getRandom();
        AgavePatchFeatureConfig config = context.getConfig();
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        for (int i = 0; i < config.tries(); ++i) {
            mutablePos.set(
                    pos,
                    random.nextInt(config.xzSpread()) - random.nextInt(config.xzSpread()),
                    random.nextInt(config.ySpread()) - random.nextInt(config.ySpread()),
                    random.nextInt(config.xzSpread()) - random.nextInt(config.xzSpread())
            );

            if (world.isAir(mutablePos) && world.getBlockState(mutablePos.down()).isOf(ModBlocks.SAND)) {
                world.setBlockState(mutablePos, config.state().get(random, pos), 2);
                var newState = world.getBlockState(mutablePos);
                AgavePlant.tryGrow(world, mutablePos, newState, random.nextInt(3));
            }
        }

        return true;
    }
}
