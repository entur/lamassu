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

package org.entur.lamassu.service.impl;

import java.util.LinkedList;
import java.util.Queue;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.RangeQueryParameters;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

public class GeoUtils {

  private GeoUtils() {}

  /**
   * Map the bounding box parameters to a geometric envelope
   * @param boundingBoxQueryParameters
   * @return
   */
  public static @NotNull ReferencedEnvelope mapToEnvelope(
    BoundingBoxQueryParameters boundingBoxQueryParameters
  ) {
    return new ReferencedEnvelope(
      boundingBoxQueryParameters.getMinimumLongitude(),
      boundingBoxQueryParameters.getMaximumLongitude(),
      boundingBoxQueryParameters.getMinimumLatitude(),
      boundingBoxQueryParameters.getMaximumLatitude(),
      DefaultGeographicCRS.WGS84
    );
  }

  /**
   * Uses the center of the envelope, and the radius of the circle the encloses the envelope to
   * define the coordinates and range of the range query parameters.
   * @param envelope
   * @return
   */
  public static @NotNull RangeQueryParameters mapToRangeQueryParameters(
    ReferencedEnvelope envelope
  ) {
    RangeQueryParameters rangeQueryParameters = new RangeQueryParameters();

    // The center of the circle that encloses the envelope is equal to the center
    // of the envelope itself
    rangeQueryParameters.setLon(envelope.getCenterX());
    rangeQueryParameters.setLat(envelope.getCenterY());

    // The diameter of the circle that encloses the envelope is equal to the length
    // of the diagonal of the envelope, hence half this length equals the circle's radius
    rangeQueryParameters.setRange(getDiagonalLength(envelope) / 2.0);
    return rangeQueryParameters;
  }

  /**
   * Find the great circle distance between the southwest and northeast corners of the envelope,
   * which we called the diagonal length of the envelope.
   * @param envelope
   * @return
   */
  private static double getDiagonalLength(ReferencedEnvelope envelope) {
    GeodeticCalculator gc = null;
    try {
      gc = GeodeticCalculatorPoolManager.get();
      Coordinate start = new CoordinateXY(envelope.getMinX(), envelope.getMinY());
      Coordinate end = new CoordinateXY(envelope.getMaxX(), envelope.getMaxY());
      gc.setStartingPosition(JTS.toDirectPosition(start, DefaultGeographicCRS.WGS84));
      gc.setDestinationPosition(JTS.toDirectPosition(end, DefaultGeographicCRS.WGS84));
      return gc.getOrthodromicDistance();
    } catch (TransformException e) {
      throw new IllegalArgumentException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(
        "Unable to calculate diagonal length of envelope",
        e
      );
    } finally {
      GeodeticCalculatorPoolManager.release(gc);
    }
  }

  private static class GeodeticCalculatorPoolManager {

    private static final Queue<GeodeticCalculator> QUEUE = new LinkedList<>();
    private static final int POOL_SIZE = 10;

    static {
      for (int i = 0; i < POOL_SIZE; i++) {
        QUEUE.add(new GeodeticCalculator(DefaultGeographicCRS.WGS84));
      }
    }

    private static GeodeticCalculator get() throws InterruptedException {
      synchronized (QUEUE) {
        while (QUEUE.isEmpty()) {
          QUEUE.wait();
        }
        return QUEUE.poll();
      }
    }

    private static void release(GeodeticCalculator gc) {
      if (gc != null) {
        synchronized (QUEUE) {
          QUEUE.offer(gc);
          QUEUE.notifyAll();
        }
      }
    }
  }
}
