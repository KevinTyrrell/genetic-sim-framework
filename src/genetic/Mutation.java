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
 * Defines various mutations which can be applied to agents.
 * The type and severity of a mutation varies based on its implementation.
 *
 */
public abstract class Mutation
{
    /* Prevent class from being instantiated. */
    private Mutation() { }
    
    private static final Random generator = new Random();

    /**
     * Randomly flips bits of an agent based on a specified mutation chance.
     * Leading zero bits are not iterated over or flipped.
     * 
     * @param agent Agent to be mutated.
     * @param mutationChance Chance of an individual bit being flipped, from [0.0, 1.0].
     * @param generator Random sequence generator
     */
    public static void flip(final Agent agent, final float mutationChance, final Random generator)
    {
        if (mutationChance < 0 || mutationChance > 1)
            throw new IllegalArgumentException("Mutation chance parameter must be in bounds [0.0, 1.0]");
        Objects.requireNonNull(generator);
        final int[] weights = Objects.requireNonNull(agent).getWeights();
        for (int i = 0; i < weights.length; i++)
        {
            int a = weights[i], b = a;
            for (int k = 0; b != 0; k++)
            {
                if (generator.nextFloat() < mutationChance)
                    a ^= (1 << k);
                /* Continue until there are no more set bits. */
                b >>= 1;
            }
            weights[i] = a;
        }
    }

    /**
     * Randomly flips bits of an agent based on a specified mutation chance.
     * Leading zero bits are not iterated over or flipped.
     * The local mutation Random sequence generator is used for random values.
     *
     * @param agent Agent to be mutated.
     * @param mutationChance Chance of an individual bit being flipped, from [0.0, 1.0].
     */
    public static void flip(final Agent agent, final float mutationChance)
    {
        flip(agent, mutationChance, generator);
    }
}
