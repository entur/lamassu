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

import java.util.List;

/**
 * Record representing the difference between two instances of a GBFS file
 *
 * @param base Numerical identifier of the base of the comparison
 * @param compare Numerical identifier of the compare side
 * @param fileName The file name of the GBFS files being compared
 * @param entityDelta A list of entity deltas for the enumerable entity of type E in the GBFS file
 * @param <E> The type of the enumerable entity in the GBFS file
 */
public record GBFSFileDelta<E>(
  Long base,
  Long compare,
  Long ttl,
  String fileName,
  List<GBFSEntityDelta<E>> entityDelta
) {}
