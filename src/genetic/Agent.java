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

import bitset.Conversions;
import blackjack.Player;
import blackjack.card.Card;
import blackjack.card.Face;
import javafx.beans.property.ObjectProperty;

import java.util.BitSet;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Agent extends Player
{
    /* Random number generator for initializing agents. */
    private static final Random rand = ThreadLocalRandom.current();

    /**
     * Number of possible outcomes when a dealer reveals his hidden card.
     */
    private static final int POSSIBLE_DEALER_CARDS = Face.set().size();
    /**
     * Possible scores that a non-busted non-21'ed player can have.
     * Minimum score is 2 with Ace, Ace and maximum score is 10, 10
     * which is possible in a variety of combinations.
     */
    private static final int POSSIBLE_SCORES = 18;

    /**
     * @return Random weight between [0, Integer.MAX_VALUE].
     */
    private static int randomWeight()
    {
        return rand.nextInt() & Integer.MAX_VALUE;
    }

    /**
     * Reference to the dealer's hidden card.
     * Used to determine whether to hit or not.
     */
    private final ObjectProperty<Card> dealersCard;

    /**
     * Three dimensional weight array.
     * 1st dimension: what card a dealer has revealed (0 -> Ace, 12 -> King).
     * 2nd dimension: what score the agent currently has ( [2, 20] ).
     * 3rd dimension: what maximum score the agent could have ( [2, 20] ).
     */
    private int[][][] weights = new int[POSSIBLE_DEALER_CARDS][POSSIBLE_SCORES][POSSIBLE_SCORES];

    /**
     * Constructs an agent with completely random weights.
     *
     * @param dealersCard Reference to the dealer's card which is revealed to the agent.
     * @see Player#hit()
     */
    public Agent(final ObjectProperty<Card> dealersCard)
    {
        for (int i = 0; i < POSSIBLE_DEALER_CARDS; i++)
            for (int j = 0; j < POSSIBLE_SCORES; j++)
                for (int k = 0; k < POSSIBLE_SCORES; k++)
                    weights[i][j][k] = randomWeight();
        this.dealersCard = Objects.requireNonNull(dealersCard);
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
        return getWeight(Objects.requireNonNull(dealersCard.get()), getScore(), getMaximumScore()) < randomWeight();
    }

    /**
     * Retrieves an agent's weight, given a specified situation.
     * A weight is an integer of domain [0, Integer.MAX_VALUE].
     * The larger the weight, the more 'affirmative' the agent is.
     * As the weight approaches Integer.MAX_VALUE, the agent
     * becomes more assure of his decision. A weight of
     * INTEGER.MAX_VALUE / 2 would indicate a coin flip.
     *
     * @param dealerCard The card the dealer has revealed.
     * @param score The agent's current score.
     * @param maxScore The agent's maximum score.
     * @return weight for that given circumstance.
     */
    public int getWeight(final Card dealerCard, final int score, final int maxScore)
    {
        assert dealerCard != null;
        assert score >= 2;
        assert score <= 20;
        assert maxScore >= 2;
        assert maxScore <= 20;
        return weights[dealerCard.getFace().ordinal()][score - 2][maxScore - 2];
    }

    /**
     * Reproduces two agents into a new agent.
     * The child agent retains influence from his mother and father's genes.
     *
     * @param mother Mother to reproduce with,
     * @return newly birthed child.
     */
    public Agent reproduce(final Agent mother)
    {
        if (Objects.requireNonNull(mother) == this)
            throw new IllegalArgumentException("Father and Mother must be unique");
        final Agent child = new Agent(dealersCard);
        /* For every gene, recalculate new values. */
        for (int i = 0; i < POSSIBLE_DEALER_CARDS; i++)
            for (int j = 0; j < POSSIBLE_SCORES; j++)
                for (int k = 0; k < POSSIBLE_SCORES; k++)
                {
                    final int fatherWeight = weights[i][j][k];
                    final int motherWeight = mother.weights[i][j][k];
                    /* Crossover the father and mother's weights into a new weight. */
                    final BitSet childCross = Crossover.uniform(
                            Conversions.convert(fatherWeight), Conversions.convert(motherWeight));
                    final long childWeight = Conversions.convert(childCross);
                    assert childWeight <= Integer.MAX_VALUE; // Should not be possible.
                    child.weights[i][j][k] = (int)childWeight;
                }

        return child;
    }
}
