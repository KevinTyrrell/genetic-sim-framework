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
import java.util.function.IntBinaryOperator;

/**
 * Defines an agent: a subject who reproduces and
 * has a series of weights geared towards learning.
 *
 * @param <T> Concrete agent type.
 * @since 1.0
 */
public interface Agent<T>
{
    /**
     * Retrieves the agent's weights.
     * Weights represent the agent's current alignment.
     * For agents with multiple weight dimensions, a single
     * dimension array should be used for all dimensions.
     * 
     * @return Array of weights.
     */
    int[] getWeights();

    /**
     * Inherits genes from two specified parents.
     * Genes are distributed through uniform crossover.
     * If a different crossover method is preferred, then
     * Agent.inherit(father, mother, callback) should be used.
     * 
     * @param mother Mother to inherit genes from.
     * @param father Father to inherit genes from.
     * @param generator Random number sequence to use.
     * @see Crossover#uniform(int, int, Random) 
     */
    default void inherit(final Agent<T> father, final Agent<T> mother, final Random generator)
    {
        checkPtrs(father, mother);
        Objects.requireNonNull(generator);
        final int[] fWeights = father.getWeights();
        final int[] mWeights = mother.getWeights();
        final int[] weights = getWeights();
        for (int i = 0; i < weights.length; i++) 
            weights[i] = Crossover.uniform(fWeights[i], mWeights[i], generator);
    }

    /**
     * Inherits genes from two specified parents.
     * For each gene, the callback will be used to 
     * determine the child's new gene weight.
     * Typically, a type of 'Crossover' is used.
     * 
     * @param mother Mother to inherit genes from.
     * @param father Father to inherit genes from.
     * @param crossCallback Callback for the specific crossover.
     * @see Crossover
     */
    default void inherit(final Agent<T> father, final Agent<T> mother, final IntBinaryOperator crossCallback)
    {
        checkPtrs(father, mother);
        Objects.requireNonNull(crossCallback);
        final int[] fWeights = father.getWeights();
        final int[] mWeights = mother.getWeights();
        final int[] weights = getWeights();
        for (int i = 0; i < weights.length; i++) 
            weights[i] = crossCallback.applyAsInt(fWeights[i], mWeights[i]);
    }

    /* Ensure reproduction parameters make sense. */
    private void checkPtrs(final Object father, final Object mother)
    {
        if (Objects.requireNonNull(mother) == Objects.requireNonNull(father))
            throw new IllegalArgumentException("Father and mother must be unique");
        if (father == this || mother == this)
            throw new IllegalArgumentException("Child agent cannot also be its own parent");
    }
}
