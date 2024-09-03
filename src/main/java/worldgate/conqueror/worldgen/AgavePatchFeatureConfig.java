package worldgate.conqueror.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public record AgavePatchFeatureConfig(BlockStateProvider state, int tries, int xzSpread, int ySpread) implements FeatureConfig {
    public static final Codec<AgavePatchFeatureConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            BlockStateProvider.TYPE_CODEC
                                    .fieldOf("state")
                                    .forGetter(AgavePatchFeatureConfig::state),
                            Codecs.POSITIVE_INT
                                    .fieldOf("tries")
                                    .orElse(128)
                                    .forGetter(AgavePatchFeatureConfig::tries),
                            Codecs.NONNEGATIVE_INT
                                    .fieldOf("xz_spread")
                                    .orElse(7)
                                    .forGetter(AgavePatchFeatureConfig::xzSpread),
                            Codecs.NONNEGATIVE_INT
                                    .fieldOf("y_spread")
                                    .orElse(3)
                                    .forGetter(AgavePatchFeatureConfig::ySpread)
            ).apply(instance, AgavePatchFeatureConfig::new));

}
