package net.runelite.client.automation.util.calculations;

import net.runelite.api.Locatable;
import net.runelite.api.coords.WorldPoint;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BotMath {

    /**
     * Scale the value to have a value between 0 and 1.
     *
     * @param value The current value to normalise.
     * @param min The minimum possible value of provided values range.
     * @param max The maximum possible value of provided values range.
     * @return The value after it has been normalised to have a value between 0 and 1.
     */
    public static double normalise(double value, double min, double max) {
        return (value - min) / (max - min);
    }

    /**
     * Round the provided values to the number of specified significant places.
     * @param value The value to be rounded.
     * @param places The number of significant places the value should be rounded to.
     * @return The value after it has been rounded.
     */
    public static double round(double value, int places) {
        if (Double.isFinite(value)) {
            return new BigDecimal(Double.toString(value)).setScale(places, RoundingMode.HALF_UP).doubleValue();
        }

        return value;
    }

    /**
     * Calculates the angle between 2 points.
     *
     * @param origin The origin point.
     * @param target The target point.
     * @return The calculated angle that has been normalised to be between 0 and 360.
     */
    public static int getAngleBetween(Locatable origin, Locatable target) {
        WorldPoint originPosition = origin.getWorldLocation();
        WorldPoint targetPosition = target.getWorldLocation();

        // https://math.stackexchange.com/questions/470112/calculate-camera-pitch-yaw-to-face-point/470121
        double dx = targetPosition.getX() - originPosition.getX();
        double dy = targetPosition.getY() - originPosition.getY();
        int angleDifference = (int) Math.toDegrees(StrictMath.atan2(dy, dx)) - 90;

        if (angleDifference >= 0) {
            return angleDifference % 360;
        }

        return (angleDifference + 360) % 360;
    }
}
