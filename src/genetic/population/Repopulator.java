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

package genetic.population;

import genetic.agent.Agent;

import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import static java.util.Objects.requireNonNull;
import static util.Utilities.swap;


public interface Repopulator
{
    /**
     * Repopulates the population, destroying the lesser half
     * and replacing them with newly birthed agents from parents
     * of the top performing half, dictated by the fitness function.
     *
     * The total population must be an even number, as to be evenly
     * divided into two sections (elite and non-elite agents).
     *
     * @param pop Population to repopulate
     * @param generator Random sequence generator
     */
    <T extends Agent<T>> void repopulate(final Population<T> pop, final Random generator,
                                         final BiFunction<T, T, T> spawner);


    /**
     * [*] Each parent is paired up with a parent who has yet to birth a child.
     * [*] Said parents are moved to the front of the array, as to not be re-selected
     * [*] This process continues until 1/4th of the population has been repopulated
     * [*] Another pass occurs, repeating the first step and repopulating the remainder
     * Each agent is guaranteed to be the parent of exactly two children.
     */
    public static final Repopulator TWO_PASS_FISCHER_YATES = new Repopulator()
    {
        @Override public <T extends Agent<T>> void repopulate(final Population<T> pop, final Random generator,
                                                              final BiFunction<T, T, T> spawner)
        {
            requireNonNull(generator);
            final List<T> agents = requireNonNull(pop).getPopulation();
            final int numAgents = agents.size();
            final int halfPop = numAgents / 2;
            final List<T> parents = agents.subList(0, halfPop), children = agents.subList(halfPop, numAgents);
            for (int i = 0; i < 2; i++) // Two-pass algorithm
            {
                for (int j = 0; j < halfPop; j += 2)
                {
                    final T father = parents.get(j);
                    final int mIndex = 1 + generator.nextInt(halfPop - j - 1);
                    swap(parents, j + 1, mIndex);
                    children.set(j + i, requireNonNull(spawner.apply(father, parents.get(mIndex))));
                }
            }
        }
    };
}
