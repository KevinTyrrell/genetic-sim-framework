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

import java.util.Collections;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

/**
 * Defines a simulation in which agents, perform, are assessed,
 * and reproduce with the goal of converging on a specific strategy.
 *
 * @param <T> Type of agent.
 * @since 1.0
 */
public interface Simulation<T extends Agent<T>>
{
    /**
     * Creates an empty agent.
     * The returned agent should not assign values for its weights,
     * as callers of this method will overwrite the agent's weights.
     * 
     * @return Newly created agent.
     */
    T initAgent();

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
     *  Each agent will be passed through the cost function and is assessed.
     *  The 
     * 
     * 
     * 
     * @param population
     * @param generations
     */
    default void run(final T[] population, final int generations, final boolean multiThreaded)
    {
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
        final PriorityQueue<Integer> queue = new PriorityQueue<>(population.length, 
                Comparator.comparingInt(o -> costs[o]));
        
        for (int gen = 0; gen < generations; gen++)
        {
            final ExecutorService es = Executors.newFixedThreadPool(numThreads);
            for (int i = 0; i < numThreads; i++)
            {
                /* Each thread gets their own cost function so none step on each other's toes. */
                final ToIntFunction<T> costFunc = initCostFunc();
                final int j = i; // i must be final for inner class to use it.
                es.submit(() ->
                {
                    final int startInc = j * agentsPerThread;
                    final int endExc = startInc + agentsPerThread
                            /* In case work load is not evenly divisible, last thread picks up the slack. */
                            + (j + 1 >= numThreads ? population.length - agentsPerThread * numThreads : 0);
                    for (int k = startInc; k < endExc; k++)
                        costs[k] = costFunc.applyAsInt(population[k]);
                });
            }
            es.shutdown();
            /* Have all of the threads assess the generation of agents. */ 
            try { es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); }
            catch (final InterruptedException e) { e.printStackTrace(); }

            /* Determine how well the population performed. */
            final IntSummaryStatistics iss = new IntSummaryStatistics();
            for (int i = 0; i < population.length; i++) iss.accept(costs[i]);
            genCostStatsCallback(iss, gen);
            
            
            for (int i = 0; i < population.length; i++) queue.add(i);
            
            
            
            
            
            
            queue.clear();
        }
    }

    public void startSimulation(final BiConsumer<IntSummaryStatistics, Integer> summaryCallback)
    {
        final int numThreads = Runtime.getRuntime().availableProcessors();
        
        Objects.requireNonNull(summaryCallback);
        for (int gen = 0; gen < generations; gen++)
        {
            final ExecutorService es = Executors.newFixedThreadPool(numThreads);
            
            
            final Cost<T> cost = new Cost<>(population, costFunc);
            for (int i = 0; i < population; i++)
                cost.accept(agents.get(i));
            summaryCallback.accept(cost.costAssessment(), gen);

            final int half = population / 2;
            final List<T> top = cost.topScorers(half);
            for (int i = 0; i < half; i++) agents.set(i, top.get(i));
            for (int fatherI = 0; fatherI < half - 1; fatherI++)
            {
                /* Pick a mother randomly to the right. */
                final int motherI = 1 + fatherI + rand.nextInt(half - fatherI - 1);
                final T mother = agents.get(motherI);
                /* Swap the mother with whoever was neighboring us. */
                agents.set(motherI, agents.get(fatherI + 1));
                agents.set(fatherI + 1, mother);
                agents.set(half + fatherI, (T)agents.get(fatherI).reproduce(mother));
            }
            // The last person has no one left to repopulate with. Pick one for him.
            agents.set(population - 1, (T)agents.get(0).reproduce(agents.get(half - 1)));
        }
    }
}
