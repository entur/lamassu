package org.entur.lamassu.cache;

import java.util.Date;

/**
 * Interface for tracking GBFS update continuity.
 * Used to store timestamps of last successful updates to detect missed updates.
 */
public interface UpdateContinuityCache {
  /**
   * Get the timestamp of the last successful update.
   *
   * @param systemId ID of the system to check
   * @return Timestamp of last update or null if no previous update exists
   */
  Date getLastUpdateTime(String systemId);

  /**
   * Store the timestamp of a successful update.
   *
   * @param systemId ID of the system being updated
   * @param timestamp Timestamp of the successful update
   */
  void setLastUpdateTime(String systemId, Date timestamp);
}
