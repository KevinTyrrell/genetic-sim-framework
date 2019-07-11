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

import java.util.Objects;
import java.util.Random;
import java.util.function.IntBinaryOperator;

/**
 * <description>
 *
 * @param 
 * @see 
 * @since 1.0
 */
public interface Agent<T>
{
    int[] getWeights();

    default Agent<T> reproduce(final Agent<T> mother, final Agent<T> child, final Random generator)
    {
        checkPtrs(mother, child);
        final int[] childWeights = child.getWeights();
        final int[] weights = getWeights();
        for (int i = 0; i < weights.length; i++)
            childWeights[i] = 0;
        return null;
    }

    default Agent<T> reproduce(final Agent<T> mother, final Agent<T> child, final IntBinaryOperator crossCallback)
    {
        return null;
    }

    /* Ensure reproduction parameters make sense. */
    private void checkPtrs(final Object mother, final Object child)
    {
        if (Objects.requireNonNull(mother) == this)
            throw new IllegalArgumentException("Agent cannot reproduce with itself");
        if (Objects.requireNonNull(child) == mother || child == this)
            throw new IllegalArgumentException("Child agent cannot also be its own parent");
    }
}
