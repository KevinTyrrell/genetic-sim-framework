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

package genetic.gradient;

import genetic.Population;
import genetic.agent.Agent;


public interface Gradient<T extends Agent<T>>
{
    /**
     * Applies the gradient to the population
     *
     * Half of the population will be destroyed in order
     * to make room for the next generation of agents.
     * The route in which agents are selected to be
     * destroyed is up to the discretion of the gradient.
     *
     * @param pop Population to apply the gradient to
     */
    void apply(final Population<T> pop);
}
