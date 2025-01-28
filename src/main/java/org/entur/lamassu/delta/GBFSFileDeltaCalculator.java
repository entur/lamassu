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
 * Interface for calculating deltas between GBFS files.
 * @param <S> The type of the GBFS file
 * @param <T> The type of the entities in the GBFS file
 */
public interface GBFSFileDeltaCalculator<S, T> {
  /**
   * Calculate the delta between two GBFS files.
   * @param base The base GBFS file to compare against, may be null for initial state
   * @param compare The GBFS file to compare with the base file
   * @return A delta containing the changes between the two files
   * @throws GBFSDeltaException if there is an error calculating the delta
   */
  GBFSFileDelta<T> calculateDelta(S base, @NotNull S compare);
}
