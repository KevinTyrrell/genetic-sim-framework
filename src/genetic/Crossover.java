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

package genetic;

import java.util.Objects;
import java.util.Random;

/**
 * Assortment of gene crossover functions
 *
 * TODO: Add alternative Crossover methods
 */
public abstract class Crossover
{
    /* Prevent class from being instantiated */
    private Crossover() { }

    /**
     * Performs a uniform crossover
     *
     * Allows control over the likelihood of bits being inherited from father vs. mother
     *
     * A bias of 0.0 would yield 100% of bits to be inherited from the father.
     * A bias of 1.0 would yield 100% of bits to be inherited from the mother.
     * A bias of 0.5 would yield an even likelihood of inheritance from either parent.
     *
     * @param father Father bits to crossover
     * @param mother Mother bits to crossover
     * @param inheritRatio Likelihood of bits being inherited from the father/mother, domain [0.0, 1.0]
     * @param generator Random sequence generator
     * @return crossed-over child gene
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
     * Performs a uniform crossover
     *
     * Ensures equal likelihood of bits being inherited from father vs. mother
     *
     * @param father Father bits to crossover
     * @param mother Mother bits to crossover
     * @param generator Random sequence generator
     * @return crossed-over child gene
     */
    public static int uniform(final int father, final int mother, final Random generator)
    {
        return uniform(father, mother, 0.5f, generator);
    }
}
