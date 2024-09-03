package worldgate.conqueror.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public record YMixedGradient(int fromY, int toY, double fromValue, double toValue, double stepSize, DensityFunction smoothness, DensityFunction offset) implements DensityFunction {

    public static final MapCodec<YMixedGradient> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(
                Codec.intRange(DimensionType.MIN_HEIGHT * 2, DimensionType.MAX_COLUMN_HEIGHT * 2)
                        .fieldOf("from_y")
                        .forGetter(YMixedGradient::fromY),
                Codec.intRange(DimensionType.MIN_HEIGHT * 2, DimensionType.MAX_COLUMN_HEIGHT * 2)
                        .fieldOf("to_y")
                        .forGetter(YMixedGradient::toY),
                Codec.DOUBLE
                        .fieldOf("from_value")
                        .forGetter(YMixedGradient::fromValue),
                Codec.DOUBLE
                        .fieldOf("to_value")
                        .forGetter(YMixedGradient::toValue),
                Codec.DOUBLE
                        .fieldOf("step_size")
                        .forGetter(YMixedGradient::stepSize),
                DensityFunction.FUNCTION_CODEC // FUNCTION_CODEC works for datapack functions (just strings of name), while CODEC only works on types referenced with {} syntax
                        .optionalFieldOf("smoothness", DensityFunctionTypes.constant(.5f))
                        .forGetter(YMixedGradient::smoothness),
                DensityFunction.FUNCTION_CODEC
                        .optionalFieldOf("offset", DensityFunctionTypes.constant(0f))
                        .forGetter(YMixedGradient::offset)

        ).apply(instance, YMixedGradient::new);
    });
    public CodecHolder<? extends DensityFunction> getCodecHolder() {
        return CodecHolder.of(CODEC);
    }

    public double sample(NoisePos pos) {
        double raw = MathHelper.clampedMap((double)pos.blockY() + offset.sample(pos), (double)this.fromY, (double)this.toY, this.fromValue, this.toValue);
        double bigStepSize = stepSize * 2;
        double bigStepNumber = Math.floor(raw / bigStepSize);
        double bigStepProgress = (raw - (bigStepNumber * bigStepSize)) / bigStepSize;
        double steepStepPercent = smoothness.sample(pos);
        double flatStepPercent = (1 - steepStepPercent);
        if (bigStepProgress <= flatStepPercent) { // On an odd (flat) step
            return bigStepNumber * bigStepSize;
        } else { // On a steep step
            double bigStepProgressTowardsSteep = bigStepProgress - flatStepPercent;
            double steepStepProgress = bigStepProgressTowardsSteep / steepStepPercent;
            return (bigStepNumber + steepStepProgress) * bigStepSize;
        }
    }

    @Override
    public void fill(double[] densities, EachApplier applier) {
        applier.fill(densities, this);
    }

    @Override
    public DensityFunction apply(DensityFunctionVisitor visitor) {
        return visitor.apply(new YMixedGradient(fromY, toY, fromValue, toValue, stepSize, smoothness.apply(visitor), offset.apply(visitor)));
    }

    public double minValue() {
        return Math.min(this.fromValue, this.toValue);
    }
    public double maxValue() {
        return Math.max(this.fromValue, this.toValue);
    }

}