export type Authentication = {
  scheme: 'OAUTH2_CLIENT_CREDENTIALS_GRANT' | 'BEARER_TOKEN' | 'HTTP_HEADERS';
  properties: Record<string, string>;
};

export interface PricingPlan {
  planId: string;
  name: string;
  price: number;
  currency: string;
  description?: string;
}

export interface FeedProvider {
  systemId: string;
  operatorId: string;
  operatorName: string;
  codespace: string;
  url: string;
  language: string;
  authentication?: Authentication | null;
  excludeFeeds?: string[] | null;
  aggregate: boolean;
  vehicleTypes?: string[] | null;
  pricingPlans?: PricingPlan[] | null;
  version: string;
  enabled: boolean;
}

export type SubscriptionStatus = 'STARTED' | 'STARTING' | 'STOPPED' | 'STOPPING';

export interface CacheKey {
  key: string;
  type: string;
}

export interface VehicleOrphan {
  vehicleId: string;
}