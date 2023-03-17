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

import genetic.Population;
import genetic.agent.Agent;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static util.Utilities.*;


public final class BirdshotGradient<T extends Agent<T>> implements Gradient<T>
{
    private final float choke;
    private final Random generator;

    /**
     * Constructs a bird shot-style gradient instance
     *
     * The choke controls the minimum/maximum percentage in which the top/bottom
     * performing agents have to be randomly selected to be destroyed/survive.
     * A higher choke will scale chances across the set linearly.
     *
     * For example, a choke of 0.05f (5%) would dictate the top performing agent
     * having a 5% chance of being selected to be destroyed. The bottom performing
     * agent would have a (100 - x)% chance (95%) of being selected to survive.
     *
     * A choke of zero will yield no chance for the top performing agent to be
     * destroyed and for the lowest performing agent to survive.
     *
     * @param choke Choke percentage to control the gradient
     */
    public BirdshotGradient(final Random generator, final float choke)
    {
        this.generator = requireNonNull(generator);
        this.choke = validateDomain(choke, 0.0f, 1.0f);
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
        final List<Agent<T>> agents = pop.getPopulation();
        final double[] costs = pop.getFitnessCosts();
        final int numAgents = agents.size();
        final double min = min(costs), max = max(costs);
        final float scalar = 1.0f - 2 * choke;
        for (int i = 0; i < numAgents; i++)
            // Ensure costs domain becomes [choke, 1.0 - choke]
            costs[i] = choke + normalize(costs[i], min, max) * scalar;

        /* Elite & Non Elite: Top & bottom performing half of the population.
        Unlucky & Lucky: Agents who are randomly selected to die & live despite performance. */
        final LinkedList<Agent<T>> elite = new LinkedList<>(), nonElite = new LinkedList<>(),
                unlucky = new LinkedList<>(), lucky = new LinkedList<>();
        decideFates(agents, costs, elite, unlucky, 0, numAgents / 2 - 1);
        decideFates(agents, costs, nonElite, lucky, numAgents / 2, numAgents - 1);
        final int ulCount = unlucky.size(), luCount = lucky.size();
        // If elite & non-elite *would* be unbalanced, change fates of average performing agents until at equilibrium
        if (ulCount > luCount)
            for (int i = ulCount - luCount; i > 0; i--)
                elite.add(nonElite.removeFirst());
        else if (luCount > ulCount)
            for (int i = luCount - ulCount; i > 0; i--)
                nonElite.add(elite.removeFirst());
        agents.clear(); Stream.of(elite, lucky, nonElite, unlucky).forEach(agents::addAll);
    }

    // Randomly determines if the fate of each agent should change
    private void decideFates(final List<Agent<T>> l, final double[] c,
                             final List<Agent<T>> a, final List<Agent<T>> b, final int s, final int e)
    {
        for (int i = s; i <= e; i++)
        {
            final Agent<T> agent = l.get(i);
            // If the normalized cost is less than our random value, the agent's fate is changed
            if (c[i] < generator.nextDouble()) b.add(agent); else a.add(agent);
        }
    }
}
