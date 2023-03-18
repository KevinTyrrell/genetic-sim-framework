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
import genetic.gene.Crossover;

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
     * Constructs a blank agent
     *
     * The returned agent should not assign values for its weights,
     * as callers of this method will overwrite the agent's weights.
     * 
     * @return Newly created agent
     */
    T initAgent();

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
     * Performs a simulation on a specified population.
     * For each generation, the following will be performed:
     *  Each agent will be passed through the cost function.
     *  The top half best performing agents are chosen to live.
     *  The remaining agents reproduce with each other and pass on genes.
     *  The newly birthed children are added into the population.
     *  This new population is closer to convergence than the last.
     *
     *  TODO: Fix documentation
     */
    default void run(final Population<T> pop, final int generations, final Random generator, final boolean multiThreaded)
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
            
            /* Heap sort half of the population */
            for (int i = 0; i < population.length; i++) sorted.add(i);
            assert sorted.peek() != null;
            for (int weight : population[sorted.peek()].getWeights())
            final int halfPop = population.length / 2;
            final Object[] parents = new Object[halfPop];
            for (int i = 0; i < halfPop; i++)
                //noinspection ConstantConditions
                parents[i] = population[sorted.poll()];
            
            /* Repopulate the agent population. */
            for (int i = 0; i < halfPop; i++)
            {
                @SuppressWarnings("unchecked")
                final T father = (T)parents[i];
                population[i] = father;
                /* Randomly pick a mother among the remaining population. */
                final int offset = 1 + generator.nextInt(halfPop - 1);
                @SuppressWarnings("unchecked")
                final T mother = (T)parents[(i + offset) % halfPop];
                assert father != mother; // Should be impossible.
                final T child = initAgent();
                child.inherit(father, mother, generator, Crossover.UNIFORM);
                mutateAgent(child);
                population[i + halfPop] = child;
            }
            
            sorted.clear();
        }
    }
}
