package ru.itmo.nds.reference.treap2015;

import java.util.Arrays;

public class Double2DIndividual {
    private double x1, x2;
    private static int dominationComparsionCount;

    Double2DIndividual(double x1, double x2) {
        super();
        this.x1 = x1;
        this.x2 = x2;
    }

    public Double2DIndividual(double[] x) {
        if (x.length != 2)
            throw new RuntimeException("Can't cast to Double2DIndividual this array: " + Arrays.toString(x));
        this.x1 = x[0];
        this.x2 = x[1];
    }

    public static int getDominationComparsionCount() {
        return dominationComparsionCount;
    }

    double getX1() {
        return x1;
    }

    double getX2() {
        return x2;
    }

    int compareX1(Double2DIndividual o) {
        ++dominationComparsionCount;
        return Double.compare(x1, o.x1);
    }

    int compareX2(Double2DIndividual o) {
        ++dominationComparsionCount;
        return Double.compare(x2, o.x2);
    }

    /**
     * not normalized
     */
    int compareDom(Double2DIndividual o) {
        //synchronized(Double2DIndividual.class) {
        ++dominationComparsionCount;
        //}
        int xc = Double.compare(x1, o.x1);
        int yc = Double.compare(x2, o.x2);
        return xc + yc;
    }


    @Override
    public boolean equals(Object o) {
        ++dominationComparsionCount;

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Double2DIndividual that = (Double2DIndividual) o;

        return Double.compare(that.x1, x1) == 0 && Double.compare(that.x2, x2) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x1);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(x2);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return String.format("ind %f %f", x1, x2);
    }
}