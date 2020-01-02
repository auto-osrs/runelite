package net.runelite.client.automation.util;

public class Execution {

    /**
     * Suspends the current thread for the specified length.
     *
     * @param length The length to suspend the thread, this should be a value
     *               greater than 0.
     */
    public static void delay(long length) {
        if (length <= 0) {
            return;
        }

        try {
            Thread.sleep(length);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Suspends the current thread using our randomly generated gaussian value.
     *
     * @param mean The mean that will be used with our gaussian distribution, the majority
     *             of values will be generated closer to this value.
     * @param min The minimum value to be generated.
     * @param max The maximum value to be generated.
     */
    public static void delay(int mean, int min, int max) {
        delay(Random.getLimitedGaussian(mean, min, max));
    }
}
