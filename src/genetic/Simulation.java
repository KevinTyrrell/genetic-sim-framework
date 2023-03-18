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
import genetic.gradient.Gradient;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.Math.min;
import static java.lang.Runtime.getRuntime;
import static java.util.Objects.requireNonNull;


/**
 * Defines a simulation in which agents, perform, are assessed,
 * and reproduce with the goal of converging on a specific strategy
 *
 * @param <T> Type of agent
 */
public interface Simulation<T extends Agent<T>>
{
    /**
     * Callback function for agent fitness data, called each generation
     *
     * Summary statistics holds the average, min, and max fitness cost of the generation.
     * The generation number is provided to the callback for reference.
     * By default, this method is a stub, but can be overridden.
     *
     * @param dss Statistics of the costs for the generation
     * @param generation Current generation number
     */
    default void genCostStatsCallback(final DoubleSummaryStatistics dss, final int generation) { }

    /**
     * Performs a simulation on the specified population
     *
     * Each generation, the following will occur:
     *      * Agents will be passed through a fitness function
     *      * Half of the worst performing agents will be destroyed
     *      * A few lucky/unlucky agents will have their fate swapped from the gradient
     *      * The remaining agents will repopulate the destroyed half of the population
     *      * Crossover and mutations will affect the child's genes
     * Ideally, each generation will be closer to convergence than the last.
     *
     * @param pop Population of agents
     * @param generations Generations to iterate before stopping
     * @param generator Random sequence generator
     * @param gradient Population gradient for genetic diversity
     * @param multiThreaded True if all CPU cores should be utilized
     */
    default void run(final Population<T> pop, final int generations, final Random generator,
                     final Gradient<T> gradient, final boolean multiThreaded)
    {
        requireNonNull(generator);
        if (generations < 0) throw new IllegalArgumentException("Generation parameter must be positive");

        /* Separate cost function work across multiple threads */
        final int numAgents = requireNonNull(pop).size();
        final int numThreads = multiThreaded ? min(getRuntime().availableProcessors(), numAgents) : 1;
        final int agentsPerThread = numAgents / numThreads;
        
        for (int gen = 1; gen <= generations; gen++)
        {
            final ExecutorService es = Executors.newFixedThreadPool(numThreads);
            final List<Future<?>> results = new ArrayList<>(numThreads);
            for (int i = 0; i < numThreads; i++)
            {
                final int j = i; // i must be final for anonymous inner class to use it
                results.add(es.submit(() ->
                {
                    final int startInc = j * agentsPerThread;
                    final int endExc = startInc + agentsPerThread
                            /* In case work load is not evenly divisible, last thread picks up the slack */
                            + (j + 1 >= numThreads ? numAgents - agentsPerThread * numThreads : 0);
                    for (int k = startInc; k < endExc; k++)
                        pop.evaluateFitness(k);
                }));
            }
            
            /* Ensure each thread completes normally */
            for (final Future<?> result : results)
                try { result.get(); }
                catch (final ExecutionException e) { throw new RuntimeException(e); }
                catch (final InterruptedException ignored) { }
            es.shutdown();
            
            /* Broadcast the performance of the current generation */
            genCostStatsCallback(pop.costEvaluation(), gen);

            pop.sortPopulation();
            gradient.apply(pop);
            pop.repopulate();
        }
    }
}
