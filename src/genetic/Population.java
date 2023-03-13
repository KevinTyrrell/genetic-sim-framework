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

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.function.ToDoubleFunction;


public interface Population<T>
{
    /**
     * Performs a fitness test, assessing the performance of all agents
     *
     * A cost function is applied to each agent, which should return their score.
     * The score determines their likelihood of survival and must be of the domain: [0.0, ∞).
     * A flawless agent will have a score of 0.0. Scores should increase if the agent is flawed.
     *
     * TODO: Change cost function parameter to a CostFunction object
     *
     * @param costFunc Callback cost function to be applied to each agent
     * @see Population#getFitnessCosts()
     */
    default void performFitnessTest(final ToDoubleFunction<Agent<T>> costFunc)
    {
        final double[] fitnessCosts = getFitnessCosts();
        final List<Agent<T>> agents = getPopulation();
        for (int i = 0; i < fitnessCosts.length; i++)
            fitnessCosts[i] = costFunc.applyAsDouble(agents.get(i));
    }

    /**
     * Retrieves fitness values for each agent
     *
     * Each agent's index in the population corresponds with their index of the fitness array.
     * A fitness test must be performed once each generation in order for this function to be valid.
     *
     * @return Fitness values assigned to each agent
     * @see Population#performFitnessTest(ToDoubleFunction)
     */
    double[] getFitnessCosts();

    /**
     * @return List of agents of the current generation
     */
    List<Agent<T>> getPopulation();

    /**
     * Initializes the population with randomized agents
     *
     * This function should only be called once per population.
     * The implementing function should ensure the fitness array is initialized as well.
     *
     * TODO: Determine if Population should be a class with this as the constructor
     *
     * @param agentCount Size of the population
     */
    void populate(final int agentCount);

    /**
     * Gathers statistics regarding the population's genes
     *
     * Each gene index of every agent will be tallied into a statistics object.
     * For each gene, the population's min/max/average will be calculated.
     * This function enables easier introspection regarding a population's progress.
     *
     * @return statistics for every gene of the population
     */
    default IntSummaryStatistics[] geneEvaluation()
    {
        final List<Agent<T>> agents = getPopulation();
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

    default void advanceGeneration()
    {
        /*
        TODO:
        Step 1) Perform a fitness test
        Step 2) Cull the population
        Step 3) Apply a gradient to the culling process
        e.g. FLAT completely severs the population in half
         */
    }

    void sortPopulation(); // TODO: Unsure if this is the route to go
    void cullPopulation(); // TODO: Parameter could be a 'CullingMethod'
}
