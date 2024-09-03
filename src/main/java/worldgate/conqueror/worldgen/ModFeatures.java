package worldgate.conqueror.worldgen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import worldgate.conqueror.WorldgateConqueror;

public class ModFeatures {

    public static final Feature<AgavePatchFeatureConfig> AGAVE_FEATURE = register("agave_patch", new AgavePatchFeature(AgavePatchFeatureConfig.CODEC));
    public static final Feature<PondFeature.Config> POND_FEATURE = register("pond", new PondFeature(PondFeature.Config.CODEC));

    public static <T extends FeatureConfig> Feature<T> register(String name, Feature<T> feature) {
        return Registry.register(Registries.FEATURE, Identifier.of(WorldgateConqueror.MOD_ID, name), feature);
    }

    public static void registerFeatures() {
        WorldgateConqueror.LOGGER.info("Registering worldgen features for {}", WorldgateConqueror.MOD_ID);
    }
}
