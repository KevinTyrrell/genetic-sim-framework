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

import blackjack.Player;

import java.io.Serializable;
import java.util.Random;

/**
 * Defines an implementation of a Blackjack player & agent.
 * The concrete agent can play Blackjack as well as reproduce.
 *
 */
public class ConcreteAgent extends Player implements Agent<ConcreteAgent>, Serializable
{
    /* Dimension #1: Possible scores a player could have at any given time. */
    private final int SCORE_POSSIBILITY = 19;
    /* Dimension #2: Whether or not the player has an ace. */
    private final int ACE_POSSIBILITY = 2;
    
    /* Two dimensional array represented by one dimension. */
    private final int[] weights = new int[SCORE_POSSIBILITY * ACE_POSSIBILITY];
    private final Random generator;

    /**
     * Constructs a new concrete agent.
     * A seed for its personal random number generator must be provided.
     * 
     * @param seed
     */
    public ConcreteAgent(final long seed)
    {
        generator = new Random(seed);
    }

    /**
     * Retrieves the agent's weights.
     * Weights represent the agent's current alignment.
     * For agents with multiple weight dimensions, a single
     * dimension array should be used for all dimensions.
     *
     * @return Array of weights.
     */
    @Override public int[] getWeights()
    {
        return weights;
    }
    
    /**
     * Determines whether or not the player should hit.
     * A player may hit if their score is less than 21.
     * A dealer must hit if his maximum score is less than 17.
     *
     * @return true if the player should hit.
     */
    @Override public boolean hit()
    {
        return getWeight(getHardScore(), hasAce()) >
                generator.nextInt(Integer.MAX_VALUE);
    }
    
    /* Convenience function - translates 1D array into 2D. */
    private int getWeight(final int score, final boolean hasAce)
    {
        // Score of 2 is the lowest possible, thus index 0.
        return weights[(score - 2) * (hasAce ? 1 : 2)];
    }
}
