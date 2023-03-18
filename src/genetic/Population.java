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

import genetic.agent.Agent;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;


public class Population<T extends Agent<T>>
{
    private final List<T> agents;
    private final double[] costs;

    public Population(final int numAgents, final Supplier<T> init)
    {
        if (numAgents < 0 || numAgents % 4 != 0)
            throw new IllegalArgumentException("Population size must be positive and a multiple of four");
        requireNonNull(init);
        costs = new double[numAgents];
        agents = new ArrayList<>(numAgents);
        for (int i = 0; i < numAgents; i++)
            agents.add(requireNonNull(init.get()));
    }

    /**
     * Performs a fitness test, assessing the performance of all agents
     *
     * A cost function is applied to each agent, which should return their score.
     * The score determines their likelihood of survival and must be of the domain: [0.0, ∞).
     * A flawless agent will have a score of 0.0. Scores should increase if the agent is flawed.
     *
     * @param costFunc Callback cost function to be applied to each agent
     * @see Population#getFitnessCosts()
     */
    public void performFitnessTest(final ToDoubleFunction<Agent<T>> costFunc)
    {
        requireNonNull(costFunc);
        for (int i = 0; i < costs.length; i++)
        {
            final double cost = costFunc.applyAsDouble(agents.get(i));
            if (cost < 0) throw new IllegalArgumentException("Cost function output must be non-negative");
            costs[i] = cost;
        }
    }

    /**
     * Sorts the population by their fitness scores
     *
     * Fitness test should be performed before this method is called.
     * This method should be called before a population cull/gradient.
     */
    public void sortPopulation()
    {
        // Agents & costs must be tied together, create copies as to not overwrite data
        final List<T> agents_co = List.copyOf(agents);
        final double[] costs_co = Arrays.copyOf(costs, costs.length);

        // Sadly sorting via a comparator requires integers to be boxed
        final List<Integer> indexes = IntStream.range(0, costs.length).boxed()
                .sorted(Comparator.comparingDouble(i -> costs[i]))
                .toList();
        for (int i = 0; i < costs.length; i++)
        {
            final int j = indexes.get(i);
            agents.set(i, agents_co.get(j));
            costs[i] = costs_co[j];
        }
    }

    /**
     * Gathers statistics regarding the population's genes
     *
     * Each gene index of every agent will be tallied into a statistics object.
     * For each gene, the population's min/max/average will be calculated.
     * This function enables easier introspection regarding a population's progress.
     *
     * @return statistics for every gene of the population
     */
    public IntSummaryStatistics[] geneEvaluation()
    {
        if (agents.isEmpty()) throw new IllegalStateException("Cannot evaluate uninitialized population.");
        final int numWeights = agents.get(0).getWeights().length;
        // Create a statistics object for every single weight
        final IntSummaryStatistics[] stats = new IntSummaryStatistics[numWeights];
        for (int i = 0; i < numWeights; i++) stats[i] = new IntSummaryStatistics();
        agents.stream()
                .map(Agent::getWeights)
                .forEach(weights ->
                {
                    for (int i = 0; i < weights.length; i++)
                    {
                        final IntSummaryStatistics iss = stats[i];
                        iss.accept(weights[i]); // Record the weight of every gene of every agent
                    }
                });
        return stats;
    }

    /**
     * Retrieves fitness values for each agent
     *
     * Fitness values run parallel with indexes of agents in the population.
     * This method should not be called until a fitness test is performed that generation.
     *
     * @return Fitness values assigned to each agent
     * @see Population#performFitnessTest(ToDoubleFunction)
     */
    public double[] getFitnessCosts()
    {
        return costs;
    }

    /**
     * @return List of agents of the current generation
     */
    public List<T> getPopulation()
    {
        return agents;
    }
}
