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
import java.util.function.IntBinaryOperator;

import static java.util.Objects.*;


/**
 * Defines an agent: a subject who reproduces and
 * has a series of weights geared towards learning
 *
 * TODO: Define abstract agent, who has an abstract weights
 *
 * @param <T> Concrete agent type
 */
public interface Agent<T>
{
    /**
     * Retrieves the agent's weights
     *
     * Weights represent the agent's disposition towards an action.
     * For agents with multiple weight dimensions, a single
     * dimension array should be used for all dimensions.
     * 
     * @return Array of weights
     */
    int[] getWeights();

    /**
     * Randomizes an agent's weights
     *
     * Each weight is randomized from [0, Integer.MAX_VALUE]
     * 
     * @param generator Random sequence generator to use
     */
    default void randomizeWeights(final Random generator)
    {
        requireNonNull(generator);
        final int[] weights = requireNonNull(getWeights());
        for (int i = 0; i < weights.length; i++)
            /* Mask out any negative numbers of nextInt() */
            weights[i] = generator.nextInt() & Integer.MAX_VALUE;
    }

    /**
     * Inherits genes from two specified parents
     *
     * Genes are distributed through uniform crossover
     * 
     * @param mother Mother to inherit genes from
     * @param father Father to inherit genes from
     * @param generator Random number sequence to use
     * @see Crossover#uniform(int, int, Random) 
     */
    default void inherit(final Agent<T> father, final Agent<T> mother, final Random generator)
    {
        checkParentalLegitimacy(father, mother);
        requireNonNull(generator);
        final int[] weights = getWeights(), fWeights = father.getWeights(), mWeights = mother.getWeights();
        for (int i = 0; i < weights.length; i++) 
            weights[i] = Crossover.uniform(fWeights[i], mWeights[i], generator);
    }

    /**
     * Inherits genes from two specified parents
     *
     * Gene crossover is differed to the specified callback function
     * 
     * @param mother Mother to inherit genes from
     * @param father Father to inherit genes from
     * @param crossCallback Callback which performs a crossover of two genes
     * @see Crossover
     */
    default void inherit(final Agent<T> father, final Agent<T> mother, final IntBinaryOperator crossCallback)
    {
        checkParentalLegitimacy(father, mother);
        requireNonNull(crossCallback);
        final int[] weights = getWeights(), fWeights = father.getWeights(), mWeights = mother.getWeights();
        for (int i = 0; i < weights.length; i++) 
            weights[i] = crossCallback.applyAsInt(fWeights[i], mWeights[i]);
    }

    /* Ensure reproduction parameters make sense. */
    private void checkParentalLegitimacy(final Object father, final Object mother)
    {
        if (requireNonNull(mother) == requireNonNull(father))
            throw new IllegalArgumentException("Father and mother must be unique");
        if (father == this || mother == this)
            throw new IllegalArgumentException("Child agent cannot also be its own parent");
    }
}
