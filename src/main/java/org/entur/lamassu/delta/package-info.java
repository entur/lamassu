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

/**
 * Warning: This feature is experimental and API is subject to change.
 *
 * <p>
 *     This package offers functionality to compare GBFS files of the same type
 *     and create a "delta" representation of the comparison.
 * </p>
 *
 * </p>
 * <p>
 *     Use cases:
 *     * Simplify the entity cache updater logic for stations and vehicles which
 *       encodes much of the same logic but in a much more complicated way.
 *     * Offer the possibility to subscribe to deltas via a public API, which has
 *       the potential to reduce bandwidth and compute resource usage between
 *       servers and clients
 * </p>
 */
package org.entur.lamassu.delta;
