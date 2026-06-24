export interface PublicFeedProviderStatus {
  systemId: string;
  operatorId: string;
  operatorName: string;
  codespace: string;
  version: string;
  enabled: boolean;
  subscriptionStatus: SubscriptionStatus;
  /** Whether the system is currently receiving fresh data (realtime feed not overdue). */
  dataFresh: boolean;
  /** Epoch seconds of the most recent realtime feed update, or null if none cached. */
  lastUpdated: number | null;
}

export type SubscriptionStatus = 'STARTED' | 'STARTING' | 'STOPPED' | 'STOPPING';
