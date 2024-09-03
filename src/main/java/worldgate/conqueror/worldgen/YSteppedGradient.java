package worldgate.conqueror.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public record YSteppedGradient(int fromY, int toY, double fromValue, double toValue, double stepSize) implements DensityFunction.Base {

    public static final MapCodec<YSteppedGradient> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(
                Codec.intRange(DimensionType.MIN_HEIGHT * 2, DimensionType.MAX_COLUMN_HEIGHT * 2)
                        .fieldOf("from_y")
                        .forGetter(YSteppedGradient::fromY),
                Codec.intRange(DimensionType.MIN_HEIGHT * 2, DimensionType.MAX_COLUMN_HEIGHT * 2)
                        .fieldOf("to_y")
                        .forGetter(YSteppedGradient::toY),
                Codec.DOUBLE
                        .fieldOf("from_value")
                        .forGetter(YSteppedGradient::fromValue),
                Codec.DOUBLE
                        .fieldOf("to_value")
                        .forGetter(YSteppedGradient::toValue),
                Codec.DOUBLE
                        .fieldOf("step_size")
                        .forGetter(YSteppedGradient::stepSize)

        ).apply(instance, YSteppedGradient::new);
    });
    public CodecHolder<? extends DensityFunction> getCodecHolder() {
        return CodecHolder.of(CODEC);
    }

    public double sample(DensityFunction.NoisePos pos) {
        double stepSizeValue = stepSize;

        if (stepSizeValue <= 0) {
            stepSizeValue = 0.01;
        }

        double raw = MathHelper.clampedMap((double)pos.blockY(), (double)this.fromY, (double)this.toY, this.fromValue, this.toValue);
        return Math.floor(raw / stepSizeValue) * stepSizeValue;
    }

    public double minValue() {
        return Math.min(this.fromValue, this.toValue);
    }
    public double maxValue() {
        return Math.max(this.fromValue, this.toValue);
    }


}