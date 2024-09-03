package worldgate.conqueror.util;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public abstract class VectorHelper {
    public static Vec3d hadamard(Vec3d u, Vec3d v) {
        return new Vec3d(u.getX() * v.getX(), u.getY() * v.getY(), u.getZ() * v.getZ());
    }
    public static Vec3d hadamardInverse(Vec3d u) {
        return new Vec3d(1.0 / u.getX(), 1.0 / u.getY(), 1.0 / u.getZ());
    }
    public static Vec3d hadamardDivide(Vec3d u, Vec3d v) {
        return new Vec3d(u.getX() / v.getX(), u.getY() / v.getY(), u.getZ() / v.getZ());
    }
    public static double dot(Vec3d u, Vec3d v) {
        return u.getX() * v.getX() + u.getY() * v.getY() + u.getZ() * v.getZ();
    }

    public static Vec3d nextVec3d(Random random) {
        return new Vec3d(random.nextDouble(), random.nextDouble(), random.nextDouble());
    }
}
