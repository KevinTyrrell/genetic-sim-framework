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

import static java.util.Objects.requireNonNull;


/**
 * Defines an agent
 *
 * An agent has weights which represent their disposition towards particular actions.
 * Agents can also reproduce and inherit their dispositions from their parent agents.
 *
 * @param <T> Concrete agent type
 */
public interface Agent<T>
{
    /**
     * Retrieves the agent's weights
     *
     * Weights represent the agent's disposition towards an action.
     * The index of the weight represents the combined inputs of an agent.
     * The value at the index represents their disposition, given the inputs.
     *
     * For multiple inputs, the array should be treated as a multi-dimensional
     *
     * @return Weights of the agent
     */
    int[] getWeights();

    /**
     * Randomizes the agent's weights
     *
     * Each weight is randomized from [0, Integer.MAX_VALUE].
     * This function randomizes the agent's disposition towards all actions.
     * 
     * @param generator Random sequence generator
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
     * @param generator Random sequence generator
     * @param cross Crossover method for inheritance
     */
    default void inherit(final Agent<T> father, final Agent<T> mother, final Random generator, final Crossover cross)
    {
        checkParentalLegitimacy(father, mother);
        requireNonNull(generator);
        requireNonNull(cross);
        final int[] weights = getWeights(), fWeights = father.getWeights(), mWeights = mother.getWeights();
        for (int i = 0; i < weights.length; i++)
            weights[i] = cross.perform(fWeights[i], mWeights[i], generator);
    }

    /* Ensure reproduction parameters are valid */
    private void checkParentalLegitimacy(final Object father, final Object mother)
    {
        if (requireNonNull(mother) == requireNonNull(father))
            throw new IllegalArgumentException("Father and mother must be unique");
        if (father == this || mother == this)
            throw new IllegalArgumentException("Child agent cannot also be its own parent");
    }
}
