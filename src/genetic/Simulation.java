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
import java.util.function.ToIntFunction;

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
     * Performs a specified mutation on an agent.
     * This function will only be called after an
     * agent has inherited genes from his parents.
     * 
     * This function should mutate the agent's genes
     * to ensure a healthy balance of genetic diversity.
     * 
     * @param agent Agent to be mutated.
     */
    void mutateAgent(final Agent agent);

    /**
     * Creates a new cost function.
     * Multiple cost functions are created if the simulation is multithreaded.
     * The cost function(s) created should never used shared resources.
     * 
     * @return Cost function which assesses agents.
     */
    ToIntFunction<T> initCostFunc();

    /**
     * Callback function called each generation of the simulation.
     * Summary statistics holds the average, min, and max cost of the generation.
     * The generation number is also passed to the callback.
     * By default, this method is empty but can be overridden.
     *
     * @param iss Statistics of the costs for the generation.
     * @param generation Current generation number.
     */
    default void genCostStatsCallback(final IntSummaryStatistics iss, final int generation) { }

    /**
     * Performs a simulation on a specified population.
     * For each generation, the following will be performed:
     *  Each agent will be passed through the cost function.
     *  The top half best performing agents are chosen to live.
     *  The remaining agents reproduce with each other and pass on genes.
     *  The newly birthed children are added into the population.
     *  This new population is closer to convergence than the last.
     * 
     * @param population Population of random agents to run the simulation with.
     * @param generations Number of generations to continue the simulation.
     */
    default void run(final T[] population, final int generations, final Random generator, final boolean multiThreaded)
    {
        Objects.requireNonNull(generator);
        if (generations < 0)
            throw new IllegalArgumentException("Generation parameter must be positive");
        if (Objects.requireNonNull(population).length % 4 != 0)
            throw new IllegalArgumentException("Population parameter size must be a factor of four");
        /* Separate cost function work across multiple threads. */
        final int numThreads = multiThreaded ? 
                Math.min(Runtime.getRuntime().availableProcessors(), population.length) : 1;
        final int agentsPerThread = population.length / numThreads;
        
        final int[] costs = new int[population.length];
        /* We only care about the top half performing agents -- not the entire population. */
        final PriorityQueue<Integer> sorted = new PriorityQueue<>(population.length, 
                Comparator.comparingInt(o -> costs[o]));
        
        for (int gen = 1; gen <= generations; gen++)
        {
            final ExecutorService es = Executors.newFixedThreadPool(numThreads);
            final List<Future<?>> results = new ArrayList<>(numThreads);
            for (int i = 0; i < numThreads; i++)
            {
                /* Each thread gets their own cost function so none step on each other's toes. */
                final ToIntFunction<T> costFunc = initCostFunc();
                final int j = i; // i must be final for inner class to use it.
                results.add(es.submit(() ->
                {
                    final int startInc = j * agentsPerThread;
                    final int endExc = startInc + agentsPerThread
                            /* In case work load is not evenly divisible, last thread picks up the slack. */
                            + (j + 1 >= numThreads ? population.length - agentsPerThread * numThreads : 0);
                    for (int k = startInc; k < endExc; k++)
                        costs[k] = costFunc.applyAsInt(Objects.requireNonNull(population[k]));
                }));
            }
            
            /* Ensure each thread completes normally. */
            for (final Future<?> result : results)
                try { result.get(); }
                catch (final ExecutionException e)
                { throw new RuntimeException(e); }
                catch (final InterruptedException ignored) { }
            es.shutdown();
            
            /* Determine how well the population performed. */
            final IntSummaryStatistics iss = new IntSummaryStatistics();
            for (int i = 0; i < population.length; i++) iss.accept(costs[i]);
            genCostStatsCallback(iss, gen);
            
            /* Heap sort half of the population. */
            for (int i = 0; i < population.length; i++) sorted.add(i);
            assert sorted.peek() != null;
            for (int weight : population[sorted.peek()].getWeights())
                System.out.printf("%.0f%%     ", ((double)weight / Integer.MAX_VALUE) * 100);
            System.out.println();
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
