package net.runelite.client.automation.util;

import java.security.InvalidParameterException;
import java.util.concurrent.ThreadLocalRandom;

public final class Random {

    public static java.util.Random getRandom() {
        /**
         * Random is thread safe for use by multiple threads. But if multiple
         * threads use the same instance of Random, the same seed is shared by
         * multiple threads. It leads to contention between multiple threads
         * and so to performance degradation.
         *
         * ThreadLocalRandom is solution to above problem. ThreadLocalRandom has
         * a Random instance per thread and safeguards against contention.
         */
        return ThreadLocalRandom.current();
    }

    /**
     * Generates a random integer within the specified values.
     * @param min The minimum value that will be generated.
     * @param max The maximum value that will be generated.
     * @return A random integer within the min and max range.
     */
    public static int getInt(int min, int max) {

        if (min == max) {
            return max;
        }

        return min + getRandom().nextInt(max - min);
    }

    /**
     * Generates a random gaussian value with the specified mean. The max deviation is
     * used to calculate the standard deviation used with the gaussian distribution.
     *
     * @param mean The mean that will be used with our gaussian distribution, the majority
     *             of values will be generated closer to this value.
     * @param maxDeviation The maximum deviation from the mean that will be returned.
     * @return A random gaussian value that is within our maxDeviation from the mean.
     */
    public static int getLimitedGaussian(int mean, int maxDeviation) {

        // Divide by 2 so we have ~95% chance that our value is within the maxDeviation.
        double standardDeviation = maxDeviation / (double) 2;

        // Calculate our gaussian value with our standard deviation.
        double gaussianValue;

        // Loop until we have a value within our max deviation range.
        do {
            gaussianValue = getGaussian(mean, standardDeviation);
        } while ((gaussianValue > mean + maxDeviation) || (gaussianValue < mean - maxDeviation));

        return (int)(gaussianValue);
    }

    /**
     * Generates a random gaussian value with the specified mean, this value will be
     * between the min and max values provided.
     *
     * @param mean The mean that will be used with our gaussian distribution, the majority
     *             of values will be generated closer to this value.
     * @param min The minimum value to be generated.
     * @param max The maximum value to be generated.
     * @return A random gaussian value between our min and max values.
     */
    public static int getLimitedGaussian(int mean, int min, int max) {

        if (min >= max) {
            throw new InvalidParameterException("The minimum value must be less than the maximum value.");
        }
        if (mean < min || mean > max) {
            throw new InvalidParameterException("The mean value must be between the minimum and maximum values.");
        }

        double standardDeviation = Math.max(mean - min, max - mean) / (double) 2;

        // Calculate our gaussian value with our standard deviation.
        double gaussianValue;

        // Loop until we have a value within our max deviation range.
        do {
            gaussianValue = getGaussian(mean, standardDeviation);
        } while ((gaussianValue > max) || (gaussianValue < min));

        return (int)(gaussianValue);
    }

    /**
     * Returns the next pseudorandom, Gaussian ("normally") distributed double
     * value with mean and standardDeviation from this random number
     * generator's sequence.
     *
     * @param mean The mean of the normal distribution.
     * @param standardDeviation The standard deviation of the normal distribution.
     * @return The pseudorandom gaussian value.
     */
    public static double getGaussian(int mean, double standardDeviation) {
        return (getRandom().nextGaussian() * standardDeviation) + mean;
    }
}
