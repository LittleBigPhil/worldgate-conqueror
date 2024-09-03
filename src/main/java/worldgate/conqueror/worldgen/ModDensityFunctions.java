package worldgate.conqueror.worldgen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

public class ModDensityFunctions {

    public static void registerDensityFunctions() {
        Registry.register(Registries.DENSITY_FUNCTION_TYPE, Identifier.of("worldgate-conqueror","y_stepped_gradient"), YSteppedGradient.CODEC);
        Registry.register(Registries.DENSITY_FUNCTION_TYPE, Identifier.of("worldgate-conqueror","y_mixed_gradient"), YMixedGradient.CODEC);
        Registry.register(Registries.DENSITY_FUNCTION_TYPE, Identifier.of("worldgate-conqueror","linear_blend"), LinearBlend.CODEC);
        Registry.register(Registries.DENSITY_FUNCTION_TYPE, Identifier.of("worldgate-conqueror","affine"), Affine.CODEC);
    }
}
