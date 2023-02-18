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

import java.util.Random;

import static java.util.Objects.requireNonNull;


/**
 * Defines an assortment of Mutation functions
 *
 * Mutations modify a particular gene, inducing new behavior
 *
 * TODO: Add more Mutation constants
 */
public interface Mutation
{
    /**
     * Uniform mutation
     *
     * Randomly flips bits of a gene based on a mutation chance.
     * Leading zero bits are not iterated over or flipped.
     */
    public static final Mutation UNIFORM = (gene, generator, mutationRate) ->
    {
        if (gene < 0)
            throw new IllegalArgumentException("Gene must be non-negative");
        if (mutationRate < 0 || mutationRate > 1)
            throw new IllegalArgumentException("Mutation rate must be within the domain: [0.0, 1.0].");
        requireNonNull(generator);
        int a = gene, b = gene;
        for (int k = 0; b != 0; k++)
        {
            if (generator.nextFloat() < mutationRate)
                a ^= (1 << k);
            /* Continue until there are no more set bits */
            b >>= 1;
        }
        return a;
    };

    /**
     * Performs a gene mutation
     *
     * Mutation type depends on the implementation of this method
     *
     * @param gene Gene to be mutated
     * @param generator Random sequence generator
     * @param mutationRate Likelihood of bits being mutated, domain: [0.0, 1.0]
     * @return Mutated gene
     */
    int perform(final int gene, final Random generator, final float mutationRate);

    /**
     * Performs a gene mutation
     *
     * Mutation type depends on the implementation of this method.
     * Mutates half of the bits of the gene, randomly.
     *
     * @param gene Gene to be mutated
     * @param generator Random sequence generator
     * @return Mutated gene
     */
    default int perform(final int gene, final Random generator)
    {
        return perform(gene, generator, 0.5f);
    }
}
