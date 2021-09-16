package peernet.transport.tools;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generate pseudo-random floating point values, with an
 * approximately Gaussian (normal) distribution.
 * <p>
 * Many physical measurements have an approximately Gaussian
 * distribution; this provides a way of simulating such values.
 */
public final class RandomGaussian implements Cloneable {

    private Random fRandom;
    private double aMean;
    private double aVariance;

    public RandomGaussian(double aMean, double aVariance) {
        this.fRandom = ThreadLocalRandom.current();
        this.aMean = aMean;
        this.aVariance = aVariance;
    }

    public RandomGaussian(double aMean, double aVariance, int seed) {
        this.fRandom = new Random(seed);
        this.aMean = aMean;
        this.aVariance = aVariance;
    }

    public double nextValue() {
        return this.getGaussian(this.aMean, this.aVariance);
    }

    private double getGaussian(double aMean, double aVariance) {
        return aMean + Math.abs(fRandom.nextGaussian()) * aVariance;
    }

    public RandomGaussian clone() {
        return new RandomGaussian(aMean, aVariance);
    }
} 
