/*
 *     <one line to give the program's name and a brief idea of what it does.>
 *     Copyright (C) 2023  Kevin Tyrrell
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

package genetic.gene;

import util.Utilities;

import java.util.Random;

import static java.util.Objects.requireNonNull;
import static util.Utilities.validateDomain;


/**
 * Defines an assortment of Crossover functions
 *
 * Crossover handles inheritance of genes from a mother and father to a child
 *
 * TODO: Add more Crossover constants
 */
public interface Crossover
{
    /**
     * Uniform crossover
     *
     * Evenly distributes bits from the father and mother, randomly.
     * For each bit, either the father or the mother is selected.
     * The selected parent's bit is then copied to the child gene.
     */
    public static final Crossover UNIFORM = (father, mother, generator, bias) ->
    {
        if (father < 0 || mother < 0)
            throw new IllegalArgumentException("Parental genes must be non-negative.");
        validateDomain(bias, 0.0f, 1.0f);
        requireNonNull(generator);
        /* Crossover father and mother into the child */
        int a = father, b = mother, c = 0;
        for (int i = 0; a != 0 || b != 0; i++)
        {
            c += ((generator.nextFloat() < bias ? a : b) & 1) << i;
            a >>= 1;
            b >>= 1;
        }

        return c;
    };

    /**
     * Performs a gene crossover between two parents
     *
     * Crossover type depends upon the implementation of this method
     *
     * A bias of 0.0 would yield 100% of bits to be inherited from the father.
     * A bias of 1.0 would yield 100% of bits to be inherited from the mother.
     * A bias of 0.5 would yield an even likelihood of inheritance from either parent.
     *
     * @param father Father bits to crossover
     * @param mother Mother bits to crossover
     * @param generator Random sequence generator
     * @param bias Likelihood of bits being inherited from the father/mother, domain: [0.0, 1.0]
     * @return Child gene crossed over from parents
     */
    int perform(final int father, final int mother, final Random generator, final float bias);

    /**
     * Performs a gene crossover between two parents
     *
     * Crossover type depends upon the implementation of this method.
     * Ensures equal likelihood of bits being inherited from father vs. mother.
     *
     * @param father Father bits to crossover
     * @param mother Mother bits to crossover
     * @param generator Random sequence generator
     * @return Child gene crossed over from parents
     */
    default int perform(final int father, final int mother, final Random generator)
    {
        return perform(father, mother, generator, 0.5f);
    }
}
