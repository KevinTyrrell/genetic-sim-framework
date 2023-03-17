/*
 *     Genetic algorithm which teaches agents how to play Blackjack.
 *     Copyright (C) 2019-2023  Kevin Tyrrell
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package util;

import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;


public final class Utilities
{
    /**
     * @param x Value to be validated towards the domain
     * @param a Starting value of the range, inclusive
     * @param b Starting value of the range, inclusive
     * @return x
     */
    public static float validateDomain(final float x, final float a, final float b)
    {
        assert a <= b;
        if (x < a || x > b) throw new IllegalArgumentException(
                format("Specified parameter %f is out of bounds of domain: [%f, %f]", x, a, b));
        return x;
    }

    /**
     * @param x Value to be validated towards the domain
     * @param a Starting value of the range, inclusive
     * @param b Starting value of the range, inclusive
     * @return x
     */
    public static double validateDomain(final double x, final double a, final double b)
    {
        assert a <= b;
        if (x < a || x > b) throw new IllegalArgumentException(
                format("Specified parameter %f is out of bounds of domain: [%f, %f]", x, a, b));
        return x;
    }
    
    /**
     * @param x Value to be validated towards the domain
     * @param a Starting value of the range, inclusive
     * @param b Starting value of the range, inclusive
     * @return x
     */
    public static int validateDomain(final int x, final int a, final int b)
    {
        assert a <= b;
        if (x < a || x > b) throw new IllegalArgumentException(
                format("Specified parameter %d is out of bounds of domain: [%d, %d]", x, a, b));
        return x;
    }

    /**
     * Normalizes a value, given the min & max values of a set
     *
     * The normalized value will be of the domain [0.0, 1.0].
     * If the value is the maximum of the set, it's normalized value will be 1.0.
     * If the value is the minimum of the set, it's normalized value will be 0.0.
     * The normalized value will scale linearly from the minimum to the maximum.
     *
     * @param x Value to be normalized
     * @param min Minimum value of the set
     * @param max Maximum value of the set
     * @return Normalized value
     */
    public static float normalize(final float x, final float min, final float max)
    {
        return (validateDomain(x, min, max) - min) / (max - min);
    }

    /**
     * Normalizes a value, given the min & max values of a set
     *
     * The normalized value will be of the domain [0.0, 1.0].
     * If the value is the maximum of the set, it's normalized value will be 1.0.
     * If the value is the minimum of the set, it's normalized value will be 0.0.
     * The normalized value will scale linearly from the minimum to the maximum.
     *
     * @param x Value to be normalized
     * @param min Minimum value of the set
     * @param max Maximum value of the set
     * @return Normalized value
     */
    public static double normalize(final double x, final double min, final double max)
    {
        return (validateDomain(x, min, max) - min) / (max - min);
    }

    /**
     * @param arr Array to search
     * @return minimum element in the array
     */
    public static double min(final double[] arr)
    {
        if (requireNonNull(arr).length <= 0) throw new IllegalArgumentException("Minimum element not present");
        double min = arr[0];
        for (int i = 1; i < arr.length; i++)
        {
            final double e = arr[i];
            if (e < min) min = e;
        }
        return min;
    }

    /**
     * @param arr Array to search
     * @return maximum element in the array
     */
    public static double max(final double[] arr)
    {
        if (requireNonNull(arr).length <= 0) throw new IllegalArgumentException("Maximum element not present");
        double max = arr[0];
        for (int i = 1; i < arr.length; i++)
        {
            final double e = arr[i];
            if (e > max) max = e;
        }
        return max;
    }
}
