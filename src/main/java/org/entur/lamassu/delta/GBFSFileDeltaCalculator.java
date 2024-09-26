/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.delta;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a calculator that can compute the delta (difference) between to instances
 * of a GBFS file.
 *
 * @param <S> The type of the GBFS file instances to compare
 * @param <T> The type of the enumerable entity inside the GBFS file being compared
 */
public interface GBFSFileDeltaCalculator<S, T> {
  /**
   * Calculate the delta (difference) between to instances of a GBFS file of type S
   *
   * @param base The base of the comparison
   *             Note: This parameter can be null, in it is then interpreted as a full update
   *
   * @param compare The instance to compare with the base
   *                Note: This parameter can't be null
   * @return An instance of GBFSFileDelta containing deltas of the enumerable entity of type T
   */
  GBFSFileDelta<T> calculateDelta(S base, @NotNull S compare);
}
