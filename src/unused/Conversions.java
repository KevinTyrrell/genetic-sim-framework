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

package bitset;

import java.util.BitSet;
import java.util.Objects;

public interface Conversions
{
    /**
     * Parses a specified value into a bit set.
     *
     * @param value Value to be parsed.
     * @return bit set corresponding to the specified value.
     */
    static BitSet convert(final long value)
    {
        final BitSet bits = new BitSet();
        long v = value;
        for (int index = 0; v != 0L;)
        {
            if ((v & 1L) != 0L)
                bits.set(index);
            index++;
            v >>>= 1;
        }

        return bits;
    }

    /**
     * Converts a bit set back to decimal.
     *
     * @param bits Bit set to parse.
     * @return Decimal value corresponding to the bit set.
     */
    static long convert(final BitSet bits)
    {
        Objects.requireNonNull(bits);
        long value = 0L;
        for (int index = 0;; index++)
        {
            index = bits.nextSetBit(index);
            if (index < 0) return value;
            value += 1L << index;
        }
    }
}
