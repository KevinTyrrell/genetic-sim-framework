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

import unused.Conversions;
import unused.Mutations;
import blackjack.Player;

import java.util.BitSet;
import java.util.Objects;

import static blackjack.Blackjack.rand;

public class ConcreteAgent extends Player
{ /**
     * Possible scores that a non-busted non-21'ed player can have.
     * Minimum score is 2 with Ace, Ace and maximum score is 10, 10
     * which is possible in a variety of combinations.
     */
    private static final int POSSIBLE_SCORES = 19;

    /**
     * Two possible circumstances: having an ace or not.
     */
    private static final int ACE_POSSIBILITY = 2;

    /**
     * @return Random weight between [0, Integer.MAX_VALUE].
     */
    private static int randomWeight()
    {
        return rand.nextInt() & Integer.MAX_VALUE;
    }

    /**
     * Two dimensional weight array.
     * 1st dimension: what score the agent currently has ( [2, 20] ).
     * 2md dimension: if the player has an ace or not.
     */
    private int[][] weights = new int[POSSIBLE_SCORES][ACE_POSSIBILITY];

    /**
     * Constructs an agent with completely random weights.
     *
     * @see Player#hit()
     */
    public ConcreteAgent()
    {
        for (int i = 0; i < POSSIBLE_SCORES; i++)
            for (int j = 0; j < ACE_POSSIBILITY; j++)
                weights[i][j] = randomWeight();
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
        return getWeight(getScore(), hasAce()) < randomWeight();
    }

    /**
     * Retrieves an agent's weight, given a specified situation.
     * A weight is an integer of domain [0, Integer.MAX_VALUE].
     * The larger the weight, the more 'affirmative' the agent is.
     * As the weight approaches Integer.MAX_VALUE, the agent
     * becomes more assure of his decision. A weight of
     * INTEGER.MAX_VALUE / 2 would indicate a coin flip.
     *
     * @param score The agent's current score.
     * @param hasAce Whether or not the agent has an ace.
     * @return weight for that given circumstance.
     */
    public int getWeight(final int score, final boolean hasAce)
    {
        assert score >= 2;
        assert score <= 20;
        return weights[score - 2][hasAce ? 1 : 0];
    }

    /**
     * Reproduces two agents into a new agent.
     * The child agent retains influence from his mother and father's genes.
     *
     * @param mother Mother to reproduce with,
     * @return newly birthed child.
     */
    public ConcreteAgent reproduce(final ConcreteAgent mother)
    {
        if (Objects.requireNonNull(mother) == this)
            throw new IllegalArgumentException("Father and Mother must be unique");
        final ConcreteAgent child = new ConcreteAgent();
        /* For every gene, recalculate new values. */
        for (int i = 0; i < POSSIBLE_SCORES; i++)
            for (int j = 0; j < ACE_POSSIBILITY; j++)
            {
                final int fatherWeight = weights[i][j];
                final int motherWeight = mother.weights[i][j];
                /* Crossover the father and mother's weights into a new weight. */
                final BitSet childCross = Crossover.uniform(
                        Conversions.convert(fatherWeight), Conversions.convert(motherWeight), rand);
                Mutations.flip(childCross, 0.25f);
                final long childWeight = Conversions.convert(childCross);
                assert childWeight <= Integer.MAX_VALUE; // Should not be possible.
                child.weights[i][j] = (int)childWeight;
            }

        return child;
    }

    public void printWeights()
    {
        for (int score = 2; score <= 20; score++)
        {
            double weight = getWeight(score, false);
            double weightAce = getWeight(score, true);
            weight *= 100.0 / Integer.MAX_VALUE;
            weightAce *= 100.0 / Integer.MAX_VALUE;
            System.out.printf("Current Score: %d\t\t\tHit Chance: %.2f%%\t\t\tHit Chance (Ace): %.2f%%\n",
                    score, weight, weightAce);
        }
        System.out.println();
    }
}
