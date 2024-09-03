package worldgate.conqueror.util;

import net.minecraft.util.math.random.Random;
import worldgate.conqueror.WorldgateConqueror;

public class RandomHelper {
    public static boolean chance(Random random, float probability) {
        return random.nextFloat() < probability;
    }

    /**
     *
     * @param random
     * @param effectStrength
     * @param targetResistance
     * @return true = Saving throw failed to save the target.
     */
    public static boolean savingThrow(Random random, float effectStrength, float targetResistance) {
        // https://www.wolframalpha.com/input?i=solve+1%2F%281%2Be%5E%28x*s%29%29+%3D+1%2F2%2B%28x%2F100%29+where+x+%3D+5+for+s
        // https://www.google.com/search?q=graph+1+%2F+%281+%2B+e%5E%28x+*+-.0401341%29%29
        final var scaling = -0.0401341f;
        final var delta = effectStrength - targetResistance;
        final var prob = 1f / (1f + Math.exp(delta * scaling));
        //WorldgateConqueror.LOGGER.info("prob={}",prob);
        return chance(random, (float) prob);
    }
}
