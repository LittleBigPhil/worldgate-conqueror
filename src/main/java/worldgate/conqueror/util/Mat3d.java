package worldgate.conqueror.util;

import net.minecraft.util.math.Vec3d;

public class Mat3d {
    private final double[][] matrix;

    public Mat3d() {
        this.matrix = new double[3][3];
    }

    public Mat3d(double[][] matrix) {
        if (matrix.length != 3 || matrix[0].length != 3) {
            throw new IllegalArgumentException("Matrix must be 3x3");
        }
        this.matrix = matrix;
    }

    public static Mat3d createOrthogonalBasis(Vec3d zAxis) {
        Vec3d normalizedZ = zAxis.normalize();
        Vec3d xAxis = new Vec3d(1, 0, 0);
        if (Math.abs(normalizedZ.dotProduct(xAxis)) > 0.9) {
            xAxis = new Vec3d(0, 1, 0);
        }
        Vec3d yAxis = normalizedZ.crossProduct(xAxis).normalize();
        xAxis = yAxis.crossProduct(normalizedZ).normalize();

        return new Mat3d(new double[][]{
                {xAxis.x, yAxis.x, normalizedZ.x},
                {xAxis.y, yAxis.y, normalizedZ.y},
                {xAxis.z, yAxis.z, normalizedZ.z}
        });
    }

    public Vec3d transform(Vec3d vec) {
        return new Vec3d(
                vec.x * matrix[0][0] + vec.y * matrix[0][1] + vec.z * matrix[0][2],
                vec.x * matrix[1][0] + vec.y * matrix[1][1] + vec.z * matrix[1][2],
                vec.x * matrix[2][0] + vec.y * matrix[2][1] + vec.z * matrix[2][2]
        );
    }

    public Mat3d transpose() {
        double[][] transposed = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                transposed[i][j] = matrix[j][i];
            }
        }
        return new Mat3d(transposed);
    }

    public static Vec3d transformToLocalSpace(Vec3d vec, Vec3d zAxis) {
        Mat3d basis = createOrthogonalBasis(zAxis);
        return basis.transpose().transform(vec);
    }

    public static Vec3d transformToWorldSpace(Vec3d vec, Vec3d zAxis) {
        Mat3d basis = createOrthogonalBasis(zAxis);
        return basis.transform(vec);
    }
}
