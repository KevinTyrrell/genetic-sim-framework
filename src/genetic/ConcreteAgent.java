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

import blackjack.player.Player;

import java.io.Serializable;
import java.util.Random;


/**
 * Defines a fully-fledged Blackjack player.
 */
public class ConcreteAgent extends Player implements Agent<ConcreteAgent>, Serializable
{
    private final Random generator;
    private final int[] weights = new int[2 * 19]; // Two dimensions represented as a single dimensional array

    /**
     * @param generator Random sequence generator
     */
    public ConcreteAgent(final Random generator)
    {
        this.generator = generator;
    }

    /**
     * Determines whether or not the player should hit
     *
     * A player may hit if their score is less than 21.
     * A dealer must hit if his maximum score is less than 17.
     *
     * @return true if the player should hit
     */
    @Override public boolean hit()
    {
        final int x = hasAce() ? 1 : 2;
        /* A score of 0 or 1 in Blackjack is impossible.
        Therefore we remove two weights that would otherwise be wasted. */
        final int y = getHardScore() - 2;
        /*
         * Dimension 1: Ace [0,1], a player having an ace in their hand can drastically change their disposition.
         * Dimension 2: Score [0,18] Valid scores to hit/stand are between 2 and 20, for a total of 19 possible scores.
         *
         * Flatten the two dimensions into one dimension
         */
        return weights[(x * y)] > generator.nextInt(Integer.MAX_VALUE);
    }

    /**
     * Retrieves the agent's weights
     *
     * Weights represent the agent's disposition towards an action.
     * The index of the weight represents the combined inputs of an agent.
     * The value at the index represents their disposition, given the inputs.
     *
     * For multiple inputs, the array should be treated as a multi-dimensional
     *
     * @return Weights of the agent
     */
    @Override public int[] getWeights()
    {
        return weights;
    }
}
