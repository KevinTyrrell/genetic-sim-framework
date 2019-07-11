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

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import static blackjack.Blackjack.rand;

public class Simulation<T extends ConcreteAgent>
{
    private final List<T> agents;
    private final int population;
    private final int generations;
    private final ToIntFunction<T> costFunc;

    public Simulation(final int population, final int generations, final Supplier<T> init,
                      final ToIntFunction<T> costFunc)
    {
        if (population <= 0)
            throw new IllegalArgumentException("Population must be positive and non-zero");
        if ((population & 1) == 1)
            throw new IllegalArgumentException("Population must be even");
        if (generations < 0)
            throw new IllegalArgumentException("Generations must be positive");
        this.population = population;
        this.generations = generations;
        Objects.requireNonNull(init);
        /* Initialize the population randomly. */
        agents = new ArrayList<>(population);
        for (int i = 0; i < population; i++)
            agents.add(init.get());
        this.costFunc = Objects.requireNonNull(costFunc);
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

    /**
     * @return List of agents who are currently alive.
     */
    public List<T> getAgents()
    {
        return Collections.unmodifiableList(agents);
    }
}
