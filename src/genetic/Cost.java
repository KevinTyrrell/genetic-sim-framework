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
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

public class Cost<T extends Agent> implements Consumer<T>
{
    private final List<T> agents;
    private final Map<T, Integer> costs;
    private final ToIntFunction<T> costCallback;
    private final Comparator<T> comparator;

    /**
     * Constructs a cost function with a specified capacity.
     *
     * @param capacity Number of agents to be processed.
     * @param costCallback Callback which determines the cost for a given agent.
     */
    public Cost(final int capacity, final ToIntFunction<T> costCallback)
    {
        if (capacity <= 0)
            throw new IllegalArgumentException(String.format("Capacity must be positive", capacity));
        agents = new ArrayList<>(capacity);
        costs = new HashMap<>(capacity);
        this.costCallback = Objects.requireNonNull(costCallback);
        comparator = Comparator.comparing(costs::get);
    }

    /**
     * Constructs a cost function.
     *
     * @param costCallback Callback which determines the cost for a given agent.
     */
    public Cost(final ToIntFunction<T> costCallback)
    {
        this.costCallback = Objects.requireNonNull(costCallback);
        agents = new ArrayList<>();
        costs = new HashMap<>();
        comparator = Comparator.comparing(costs::get);
    }

    /**
     * Accepts an agent and performs the cost function on them.
     *
     * @param t Agent to be assessed.
     */
    @Override public void accept(final T t)
    {
        if (costs.containsKey(t))
            throw new IllegalStateException(String.format("Agent \"%s\" was already evaluated", t.toString()));
        final int cost = costCallback.applyAsInt(t);
        agents.add(t);
        costs.put(t, cost);
    }

    /**
     * @return Number of agents assessed.
     */
    public int size()
    {
        return agents.size();
    }

    /**
     * Retrieves the top scorers of those assessed by the cost function.
     *
     * @param numAgents Number of agents to retrieve.
     * @return List of agents sorted by least cost to most.
     */
    public List<T> topScorers(final int numAgents)
    {
        final int size = costs.size();
        if (numAgents > size)
            throw new IllegalArgumentException(String.format(
                    "Request of %d agents exceeds current size of %d", numAgents, size));
        agents.sort(comparator);
        return Collections.unmodifiableList(agents.subList(0, numAgents + 1));
    }
}
