export interface PublicFeedProviderStatus {
  systemId: string;
  operatorId: string;
  operatorName: string;
  codespace: string;
  version: string;
  enabled: boolean;
  subscriptionStatus: SubscriptionStatus;
}

export type SubscriptionStatus = 'STARTED' | 'STARTING' | 'STOPPED' | 'STOPPING';
