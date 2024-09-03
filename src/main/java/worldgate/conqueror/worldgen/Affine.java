package worldgate.conqueror.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public record Affine(DensityFunction input, DensityFunction scale, DensityFunction offset) implements DensityFunction {
    public static final MapCodec<Affine> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(
                DensityFunction.FUNCTION_CODEC
                        .fieldOf("input")
                        .forGetter(Affine::input),
                DensityFunction.FUNCTION_CODEC
                        .optionalFieldOf("scale", DensityFunctionTypes.constant(1.0))
                        .forGetter(Affine::scale),
                DensityFunction.FUNCTION_CODEC
                        .optionalFieldOf("offset", DensityFunctionTypes.constant(0.0))
                        .forGetter(Affine::offset)

        ).apply(instance, Affine::new);
    });

    @Override
    public double sample(DensityFunction.NoisePos pos) {
        return input.sample(pos) * scale.sample(pos) + offset.sample(pos);
    }

    @Override
    public void fill(double[] densities, DensityFunction.EachApplier applier) {
        applier.fill(densities, this);
    }

    @Override
    public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
        return visitor.apply(new Affine(input.apply(visitor), scale.apply(visitor), offset.apply(visitor)));
    }

    @Override
    public double minValue() {
        double extremeFromInput = Double.max(Math.abs(input.minValue()), Math.abs(input.maxValue()));
        double extremeFromScale = Double.max(Math.abs(scale.minValue()), Math.abs(scale.maxValue()));
        return -extremeFromInput * extremeFromScale + offset.minValue();
    }

    @Override
    public double maxValue() {
        double extremeFromInput = Double.max(Math.abs(input.minValue()), Math.abs(input.maxValue()));
        double extremeFromScale = Double.max(Math.abs(scale.minValue()), Math.abs(scale.maxValue()));
        return extremeFromInput * extremeFromScale + offset.maxValue();
    }

    public CodecHolder<? extends DensityFunction> getCodecHolder() {
        return CodecHolder.of(CODEC);
    }
}
