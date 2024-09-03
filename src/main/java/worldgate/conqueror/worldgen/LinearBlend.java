package worldgate.conqueror.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public record LinearBlend(DensityFunction amount, DensityFunction argument1, DensityFunction argument2) implements DensityFunction {
    public static final MapCodec<LinearBlend> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(
                DensityFunction.FUNCTION_CODEC
                        .optionalFieldOf("amount", DensityFunctionTypes.constant(.5f))
                        .forGetter(LinearBlend::amount),
                DensityFunction.FUNCTION_CODEC
                        .fieldOf("argument1")
                        .forGetter(LinearBlend::argument1),
                DensityFunction.FUNCTION_CODEC
                        .fieldOf("argument2")
                        .forGetter(LinearBlend::argument2)

        ).apply(instance, LinearBlend::new);
    });

    @Override
    public double sample(NoisePos pos) {
        double t = (MathHelper.clamp(amount.sample(pos), -1.0, 1.0) + 1.0) / 2.0;
        return t * argument1.sample(pos) + (1 - t) * argument2.sample(pos);
    }

    @Override
    public void fill(double[] densities, EachApplier applier) {
        applier.fill(densities, this);
    }

    @Override
    public DensityFunction apply(DensityFunctionVisitor visitor) {
        return visitor.apply(new LinearBlend(amount.apply(visitor), argument1.apply(visitor), argument2.apply(visitor)));
    }

    @Override
    public double minValue() {
        return Double.min(argument1.minValue(), argument2.minValue());
    }

    @Override
    public double maxValue() {
        return Double.max(argument1.maxValue(), argument2.maxValue());
    }

    public CodecHolder<? extends DensityFunction> getCodecHolder() {
        return CodecHolder.of(CODEC);
    }
}
