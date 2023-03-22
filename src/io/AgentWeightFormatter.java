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

package io;

import blackjack.card.Card;
import blackjack.card.Face;
import blackjack.card.Suit;
import genetic.agent.Agent;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOfRange;
import static java.util.Objects.requireNonNull;


public final class AgentWeightFormatter
{
    private static final DecimalFormat df = new DecimalFormat("#.#");

    private static final int MATRIX_CELL_WIDTH = 12, CELL_COUNT = 10, ROW_COUNT = 8;
    private static final String ROW_FORMAT = ("%-" + MATRIX_CELL_WIDTH + "s")
            .repeat(CELL_COUNT - 1) + "%s";

    /**
     * Formats the agent's weights into a human-readable matrix of values
     *
     * The matrix consists of alternating rows of headers and weight values.
     * The header rows will appear as follows, with weight values directly underneath
     *
     *      ..      02      03      04      05      06      07      08      09      10
     *
     *      11      12      13      14      15      16      17      18      19      20
     *
     *      ..      02      03      04      05      06      07      08      09      10
     *
     *      11      12      13      14      15      16      17      18      19      20
     *
     * The first four rows consist of weights for hands that do not contain any Aces.
     * The second four rows consist of weights for hands that contain at least one Ace.
     * The percentages represent how likely an agent is to hit on that particular score.
     *
     * For example, if the Agent's hand was [Ace of Spades][7 of Diamonds], one would look
     * at row 6 (index 5) at column 8 (index 7) and observe the percentage listed. If the
     * percentage was 75%, this indicates the agent is 75% likely to hit on that particular hand.
     *
     * @param agent Agent to inspect
     * @return Matrix of headers and weight percentages
     */
    public static String[] formatWeightMatrix(final Agent<?> agent)
    {
        final int SECTION_COUNT = CELL_COUNT * 2;
        final String[] values = new String[SECTION_COUNT * 2];
        // Convert all integer weights such as '10528181' into percentage strings
        final String[] percentages = Arrays.stream(requireNonNull(agent.getWeights()))
                .mapToObj(w -> df.format(100 * (double)w / Integer.MAX_VALUE) + "%")
                .toArray(String[]::new);
        values[0] = "??-" + Card.CARD_BACK_SYMBOL; // Indicate this section is hand values without an ace
        values[SECTION_COUNT] = new Card(Face.ACE, Suit.SPADES).toString(); // section with an ace

        arraycopy(percentages, 0, values, 1, SECTION_COUNT - 1);
        arraycopy(percentages, SECTION_COUNT - 1, values, SECTION_COUNT + 1, SECTION_COUNT - 1);

        final String[] formatted = new String[ROW_COUNT];
        for (int i = 1; i < ROW_COUNT; i += 2)
        {
            final int j = i / 2;
            final Object[] row = copyOfRange(values, j * CELL_COUNT, (j + 1) * CELL_COUNT);
            formatted[i] = format(ROW_FORMAT, row);
        }

        final String[] header = buildHeader();
        for (int i = 0; i < ROW_COUNT / 4; i++)
        {
            final Object[] row = copyOfRange(header, i * CELL_COUNT, (i + 1) * CELL_COUNT);
            final String f = String.format(ROW_FORMAT, row);
            // Place two copies of the header, one for the hands without & with an ace in-hand
            formatted[i * 2] = f; formatted[ROW_COUNT / 2 + i * 2] = f;
        }

        return formatted;
    }

    private static String[] buildHeader()
    {
        final int cells = CELL_COUNT * 2;
        final String[] header = IntStream.rangeClosed(1, cells)
                .mapToObj(i -> (i < 10 ? "0" : "") + i)
                .toArray(String[]::new);
        header[0] = ".."; // A score of 1 is impossible in Blackjack, put placeholder
        return header;
    }
}
