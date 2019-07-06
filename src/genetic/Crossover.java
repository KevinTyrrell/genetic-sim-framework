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

import java.util.BitSet;
import java.util.Objects;

public interface Crossover
{
    /**
     * Performs a single-point crossover.
     * Bits from the father are transferred to the birthed child up
     * until the index, from which the mother's bits are transferred.
     *
     * @param father Father's bits to crossover.
     * @param mother Mother's bits to crossover.
     * @param index Bit index, starting from the right-most bit, where father/mother crossover changes.
     * @return newly birthed child.
     */
    static BitSet singlePoint(final BitSet father, final BitSet mother, final int index)
    {
        checkBitSets(father, mother);
        if (index < 0) throw new IndexOutOfBoundsException(index);
        final BitSet child = new BitSet();
        copyBits(father, father.length(), child, 0, index - 1);
        for (int bit = index;; bit++)
        {
            bit = mother.nextSetBit(bit);
            if (bit < 0) return child;
            child.set(bit);
        }
    }

    /**
     * Performs a two-point crossover.
     * Bits from the father and mother in partitions are inherited by the child.
     * |father bits|mother bits|father bits|
     *             ^ index i   ^ index j
     *
     * This is equivalent to calling BitSet.kPoint(father, mother, i, j).
     *
     * @param father Father's bits to crossover.
     * @param mother Mother's bits to crossover.
     * @param i Starting index of when to transfer the mother's bits.
     * @param j Starting index of when to continue transferring the father's bits.
     * @return newly birthed child.
     * @see Crossover#kPoint(BitSet, BitSet, int...)
     */
    static BitSet twoPoint(final BitSet father, final BitSet mother, final int i, final int j)
    {
        return kPoint(father, mother, i, j);
    }

    /**
     * Performs a K-point crossover.
     * Bits will alternate being inherited from the father and the mother on each index.
     * For example, the call of Crossover.kPoint(father, mother, 1, 3, 3, 10) incur the following:
     * Father's bit 0 is inherited by the child.
     * Mother's bits 1 & 2 are inherited by the child.
     * Father's turn is skipped due to having a segment of length 0.
     * Mother's bits 3-9 are inherited by the child.
     * All child  bits not inherited from parents are set to 0.
     *
     * @param father Father's bits to crossover.
     * @param mother Mother's bits to crossover.
     * @param indexes Indexes of where to alternate the transferring of father's/mother's bits.
     * @return newly birthed child.
     * @see Crossover#singlePoint(BitSet, BitSet, int)
     * @see Crossover#twoPoint(BitSet, BitSet, int, int)
     */
    static BitSet kPoint(final BitSet father, final BitSet mother, final int... indexes)
    {
        checkBitSets(father, mother);
        final BitSet[] parents = { father, mother };
        final int[] lengths = { father.length(), mother.length() };
        final BitSet child = new BitSet();
        int bit = 0;
        for (int i = 0; i < indexes.length; i++)
        {
            final int index = indexes[i];
            if (bit > index) throw new IllegalArgumentException(String.format("Index %d is out of order", index));
            if (bit != index)
            {
                final int j = i & 1;
                copyBits(parents[j], lengths[j], child, bit, index - 1);
                bit = index;
            }
        }

        return child;
    }

    /**
     * Performs a uniform crossover.
     * Allows control over the likelihood of bits being inherited from father.
     * A bias of 0.0 would yield 100% of bits to be inherited from the father.
     * A bias of 1.0 would yield 100% of bits to be inherited from the mother.
     * For an even likelihood (0.5), Crossover.uniform(BitSet, BitSet) should be used.
     *
     * @param father Father bits to crossover.
     * @param mother Mother bits to crossover.
     * @param inheritRatio likelihood of bits being inherited from father:mother.
     * @return newly birthed child.
     */
    static BitSet uniform(final BitSet father, final BitSet mother, float inheritRatio)
    {
        checkBitSets(father, mother);
        if (inheritRatio < 0 || inheritRatio > 1)
            throw new IllegalArgumentException("Paternal bias must be within bounds [0.0, 1.0]");
        final int len = Math.max(father.length(), mother.length());
        final BitSet child = new BitSet();
        for (int i = 0; i < len; i++)
            child.set(i, Math.random() < inheritRatio ? father.get(i) : mother.get(i));
        return child;
    }

    /**
     * Performs a uniform crossover.
     * For each bit of the child, there will be an equally-likely chance
     * that he will inherit a bit from the mother instead of the father.
     *
     * This is equivalent to calling Crossover.uniform(father, mother, 0.5f).
     *
     * @param father Father bits to crossover.
     * @param mother Mother bits to crossover.
     * @return newly birthed child.
     */
    static BitSet uniform(final BitSet father, final BitSet mother)
    {
        return uniform(father, mother, 0.5f);
    }

    private static void copyBits(final BitSet host, final int hostLen, final BitSet target, final int from, final int to)
    {
        assert(host != null);
        assert(target != null);
        assert(hostLen >= 0);
        assert(host != target);
        assert(from >= 0);
        assert(to >= 0);
        assert(from <= to);

        for (int bit = from; bit < hostLen && bit <= to; bit++)
            if (host.get(bit))
                target.set(bit);
    }

    private static void checkBitSets(final BitSet a, final BitSet b)
    {
        if (Objects.requireNonNull(a) == Objects.requireNonNull(b))
            throw new IllegalArgumentException("Paternal and maternal BitSets must be unique");
    }
}
