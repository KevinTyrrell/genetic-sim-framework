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

package genetic.gradient;

import genetic.population.Population;
import genetic.agent.Agent;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static util.Utilities.*;


public final class BirdshotGradient<T extends Agent<T>> implements Gradient<T>
{
    private final float scalar;
    private final Random generator;

    /**
     * Constructs a bird shot-style gradient instance
     *
     * The choke controls the minimum/maximum percentage in which the top/bottom
     * performing agents have to be randomly selected to be destroyed/survive.
     * A higher choke will scale chances across the set linearly.
     *
     * The scalar for the sigmoid function controls how dramatically numbers are pushed
     * towards the edge of the domain. Very small/large numbers wil be decreased/increased
     * more than numbers near the mean. Scalar domain is [1, inf), recommended: 10
     *
     * @param generator Random sequence generator
     * @param scalar Scalar for the sigmoid function
     */
    public BirdshotGradient(final Random generator, final float scalar)
    {
        this.generator = requireNonNull(generator);
        if (scalar < 1) throw new IllegalArgumentException("Scalar must be positive and greater than one");
        this.scalar = scalar;
    }

    /**
     * Applies the gradient to the population
     *
     * Half of the population will be destroyed in order
     * to make room for the next generation of agents.
     * The route in which agents are selected to be
     * destroyed is up to the discretion of the gradient.
     *
     * @param pop Population to apply the gradient to
     */
    @Override public void apply(final Population<T> pop)
    {
        final List<T> agents = pop.getPopulation();
        final double[] costs = pop.getFitnessCosts();
        final int numAgents = agents.size();
        final double min = min(costs), max = max(costs);
        for (int i = 0; i < numAgents; i++)
            /* 1) Normalize numbers so they fall in ranges [0.0, 1.0]. 2) Subtract 0.5 so the mean of the
             * numbers is adjusted. 3) Scale the numbers by a scalar to create a more dramatic swing in the
             * sigmoid function. 4) Apply the sigmoid function to favor outsiders more than insiders. */
            costs[i] = sigmoid((normalize(costs[i], min, max) - 0.5) * scalar);

        /* Elite & Non Elite: Top & bottom performing half of the population.
        Unlucky & Lucky: Agents who are randomly selected to die & live despite performance. */
        final LinkedList<T> elite = new LinkedList<>(), nonElite = new LinkedList<>(),
                unlucky = new LinkedList<>(), lucky = new LinkedList<>();
        unluckyDeaths(agents, costs, elite, unlucky);
        luckyRebirths(agents, costs, nonElite, lucky);

        final int ulCount = unlucky.size(), luCount = lucky.size();
        // If elite & non-elite would be unbalanced, change fates of average performing agents until at equilibrium
        if (ulCount > luCount)
            for (int i = ulCount - luCount; i > 0; i--)
                elite.add(nonElite.removeFirst());
        else if (luCount > ulCount)
            for (int i = luCount - ulCount; i > 0; i--)
                nonElite.add(elite.removeFirst());
        agents.clear(); Stream.of(elite, lucky, nonElite, unlucky).forEach(agents::addAll);
    }

    // Categorized the front-half agents as 'alive' or, when very unlucky, 'dead'
    private void unluckyDeaths(final List<T> l, final double[] c, final List<T> a, final List<T> b)
    {
        for (int i = c.length / 2 - 1; i >= 0; i--)
            if (c[i] < generator.nextDouble()) a.add(l.get(i)); else b.add(l.get(i));
    }

    // Categorize the back-half agents as 'dead' or, when very lucky, 'alive'
    private void luckyRebirths(final List<T> l, final double[] c, final List<T> a, final List<T> b)
    {
        for (int i = c.length / 2; i < c.length; i++)
            if (c[i] > generator.nextDouble()) a.add(l.get(i)); else b.add(l.get(i));
    }
}
