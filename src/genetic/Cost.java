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

public class Cost<T extends Agent<T>> implements Consumer<T>
{
    private List<T> agents;
    private Map<T, Integer> costs;
    private ToIntFunction<T> costCallback;
    private Comparator<T> comparator;
    private IntSummaryStatistics iss = new IntSummaryStatistics();
    
    /**
     * Constructs a cost function with an initial capacity.
     *
     * @param capacity Number of agents to be processed.
     * @param costCallback Callback which determines the cost for a specified agent.
     */
    public Cost(final int capacity, final ToIntFunction<T> costCallback)
    {
        if (capacity <= 0) 
            throw new IllegalArgumentException("Cost function capacity must be positive");
        agents = new ArrayList<>(capacity);
        costs = new HashMap<>(capacity);
        this.costCallback = Objects.requireNonNull(costCallback);
        comparator = Comparator.comparing(costs::get);
    }

    /**
     * Constructs a cost function.
     *
     * @param costCallback Callback which determines the cost for a specified agent.
     */
    public Cost(final ToIntFunction<T> costCallback)
    {
        this(0, costCallback);
    }

    /**
     * Accepts an agent and performs the cost function on them.
     *
     * @param t Agent to be assessed and added.
     */
    @Override public void accept(final T t)
    {
        if (costs == null) throw new IllegalStateException("Cost function has already ended");
        if (costs.containsKey(Objects.requireNonNull(t)))
            throw new IllegalStateException("Specified agent has already been evaluated");
        final int cost = costCallback.applyAsInt(t);
        iss.accept(cost);
        agents.add(t);
        costs.put(t, cost);
    }

    /**
     * Ends the cost function, enabling garbage collection early.
     * This function must be called before retrieving the results.
     * 
     * @param numAgents Number of agents to rank.
     * @return This object for one-line convenience.
     */
    public List<T> doneAndRank(final int numAgents)
    {
        if (agents == null)
            throw new IllegalStateException("Cost function has already ended");
        if (numAgents < 0 || numAgents > agents.size())
            throw new IllegalArgumentException(String.format(
                    "Index of %d is not within bounds [0, size]", agents.size()));
        agents.subList(0, numAgents).sort(comparator);
        final List<T> temp = agents;
        /* Begin garbage collection as soon as possible. */
        comparator = null; agents = null; costs = null; costCallback = null;
        return temp;
    }

    /**
     * Retrieves the statistics of the generation's costs.
     * This function may only be called after the cost function has ended.
     *
     * @return Summary statistics of how the generation performed.
     */
    public IntSummaryStatistics costAssessment()
    {
        if (agents != null)
            throw new IllegalStateException("Cost function must be ended before requesting cost assessment");
        if (iss == null)
            throw new IllegalStateException("Cost assessment has already been returned");
        final IntSummaryStatistics temp = iss;
        iss = null;
        return temp;
    }
}
