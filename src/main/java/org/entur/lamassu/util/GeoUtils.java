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

package org.entur.lamassu.util;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.RangeQueryParameters;
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
    GeodeticCalculator geodeticCalculator = null;
    try {
      geodeticCalculator = GeodeticCalculatorPoolManager.getInstance().get();
      Coordinate start = new CoordinateXY(envelope.getMinX(), envelope.getMinY());
      Coordinate end = new CoordinateXY(envelope.getMaxX(), envelope.getMaxY());
      geodeticCalculator.setStartingPosition(
        JTS.toDirectPosition(start, DefaultGeographicCRS.WGS84)
      );
      geodeticCalculator.setDestinationPosition(
        JTS.toDirectPosition(end, DefaultGeographicCRS.WGS84)
      );
      return geodeticCalculator.getOrthodromicDistance();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(
        "Unable to calculate diagonal length of envelope",
        e
      );
    } catch (Exception e) {
      throw new IllegalStateException(
        "Unable to calculate diagonal length of envelope",
        e
      );
    } finally {
      GeodeticCalculatorPoolManager.getInstance().release(geodeticCalculator);
    }
  }

  private static class GeodeticCalculatorPoolManager {

    // pool size can be high, purpose is to not share instances between threads
    private static final int POOL_SIZE = 100;
    private static final GeodeticCalculatorPoolManager INSTANCE =
      new GeodeticCalculatorPoolManager();

    private final GenericObjectPool<GeodeticCalculator> pool;

    private GeodeticCalculatorPoolManager() {
      GenericObjectPool.Config config = new GenericObjectPool.Config();
      config.maxActive = POOL_SIZE;
      PoolableObjectFactory<GeodeticCalculator> geodeticCalculatorFactory =
        new BasePoolableObjectFactory<>() {
          @Override
          public GeodeticCalculator makeObject() {
            return new GeodeticCalculator(DefaultGeographicCRS.WGS84);
          }
        };

      pool = new GenericObjectPool<>(geodeticCalculatorFactory, config);
    }

    public static GeodeticCalculatorPoolManager getInstance() {
      return INSTANCE;
    }

    private GeodeticCalculator get() throws Exception {
      return pool.borrowObject();
    }

    private void release(GeodeticCalculator gc) {
      try {
        pool.returnObject(gc);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Calculate distance between two points in kilometers using the Haversine formula
   */
  public static double calculateDistance(
    double lat1,
    double lon1,
    double lat2,
    double lon2
  ) {
    final int EARTH_RADIUS = 6371; // Earth's radius in kilometers

    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);

    double a =
      Math.sin(latDistance / 2) *
      Math.sin(latDistance / 2) +
      Math.cos(Math.toRadians(lat1)) *
      Math.cos(Math.toRadians(lat2)) *
      Math.sin(lonDistance / 2) *
      Math.sin(lonDistance / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS * c;
  }
}
