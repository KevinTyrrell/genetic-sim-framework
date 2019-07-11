/*
 *     <one line to give the program's name and a brief idea of what it does.>
 *     Copyright (C) 2019  Kevin Tyrrell
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

package genetic;

import java.util.Objects;
import java.util.Random;

/**
 * Library of popular bitwise crossover methods.
 *
 * @since 1.0
 */
public abstract class Crossover
{
    /* Prevent class from being instantiated. */
    private Crossover() { }
    
    /* Used when user does not provide a generator of his own. */
    private static final Random generator = new Random();

    /**
     * Performs a uniform crossover.
     * Allows control over the likelihood of bits being inherited from father.
     * A bias of 0.0 would yield 100% of bits to be inherited from the father.
     * A bias of 1.0 would yield 100% of bits to be inherited from the mother.
     * A bias of 0.5 would yield an even likelihood of inheritance from either parent.
     * TODO: Allow for negative integers.
     *
     * @param father Father bits to crossover.
     * @param mother Mother bits to crossover.
     * @param inheritRatio Likelihood of bits being inherited from the father, from [0.0, 1.0] (not likely to likely).
     * @param generator Random number generator to use.
     * @return crossed-over child gene.
     */
    public static int uniform(final int father, final int mother, final float inheritRatio, final Random generator)
    {
        if (father < 0 || mother < 0)
            throw new IllegalArgumentException("Father and mother must be positive");
        if (inheritRatio < 0 || inheritRatio > 1)
            throw new IllegalArgumentException(String.format(
                    "Inheritance ratio of %f is not in bounds [0.0, 1.0]", inheritRatio));
        /* Crossover father and mother into the child. */
        int a = father, b = mother, c = 0;
        Objects.requireNonNull(generator);
        
        for (int i = 0; a != 0 || b != 0; i++)
        {
            c += ((generator.nextFloat() < inheritRatio ? a : b) & 1) << i;
            a >>= 1;
            b >>= 1;
        }

        return c;
    }

    /**
     * Performs a uniform crossover.
     * Allows control over the likelihood of bits being inherited from father.
     * A bias of 0.0 would yield 100% of bits to be inherited from the father.
     * A bias of 1.0 would yield 100% of bits to be inherited from the mother.
     * A bias of 0.5 would yield an even likelihood of inheritance from either parent.
     * Random sequence will differ to the Crossover's Random instance.
     *
     * @param father Father bits to crossover.
     * @param mother Mother bits to crossover.
     * @param inheritRatio Likelihood of bits being inherited from the father, from [0.0, 1.0] (not likely to likely).
     * @return crossed-over child gene.
     */
    public static int uniform(final int father, final int mother, final float inheritRatio)
    {
        return uniform(father, mother, inheritRatio, generator);
    }

    /**
     * Performs a uniform crossover.
     * Allows control over the likelihood of bits being inherited from father.
     * A bias of 0.0 would yield 100% of bits to be inherited from the father.
     * A bias of 1.0 would yield 100% of bits to be inherited from the mother.
     * A bias of 0.5 would yield an even likelihood of inheritance from either parent.
     *
     * @param father Father bits to crossover.
     * @param mother Mother bits to crossover.
     * @param generator Random number generator to use.
     * @return crossed-over child gene.
     */
    public static int uniform(final int father, final int mother, final Random generator)
    {
        return uniform(father, mother, 0.5f, generator);
    }

    /**
     * Performs a uniform crossover.
     * Yields an even likelihood of father or mother's bits being inherited.
     * Random sequence will differ to the Crossover's Random instance.
     * This is equivalent to Crossover.uniform(father, mother, 0.5f).
     *
     * @param father Father bits to crossover.
     * @param mother Mother bits to crossover.
     * @return crossed-over child gene.
     */
    static int uniform(final int father, final int mother)
    {
        return uniform(father, mother, 0.5f, generator);
    }
}
