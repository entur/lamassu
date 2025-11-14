import axios from 'axios';
import type { FeedProvider, SubscriptionStatus } from '../types/admin';

// Detect the base path from the current URL to support reverse proxies
// e.g., /admin/ui or /mobility/v2/admin/ui
const getBasePath = () => {
  const match = window.location.pathname.match(/^(.*)\/admin\/ui/);
  return match ? match[1] + '/admin' : '/admin';
};

const api = axios.create({
  baseURL: getBasePath(),
});

export const adminApi = {
  // Feed Provider operations
  getAllProviders: async (): Promise<FeedProvider[]> => {
    const response = await api.get('/feed-providers');
    return response.data;
  },

  getProvider: async (systemId: string): Promise<FeedProvider> => {
    const response = await api.get(`/feed-providers/${systemId}`);
    return response.data;
  },

  createProvider: async (provider: FeedProvider): Promise<FeedProvider> => {
    const response = await api.post('/feed-providers', provider);
    return response.data;
  },

  updateProvider: async (provider: FeedProvider): Promise<FeedProvider> => {
    const response = await api.put(`/feed-providers/${provider.systemId}`, provider);
    return response.data;
  },

  deleteProvider: async (systemId: string): Promise<void> => {
    await api.delete(`/feed-providers/${systemId}`);
  },

  // Subscription operations
  startSubscription: async (systemId: string): Promise<void> => {
    await api.post(`/feed-providers/${systemId}/start`);
  },

  stopSubscription: async (systemId: string): Promise<void> => {
    await api.post(`/feed-providers/${systemId}/stop`);
  },

  restartSubscription: async (systemId: string): Promise<void> => {
    await api.post(`/feed-providers/${systemId}/restart`);
  },

  getSubscriptionStatuses: async (): Promise<Record<string, SubscriptionStatus>> => {
    const response = await api.get('/feed-providers/subscription-statuses');
    return response.data;
  },

  getSubscriptionStatus: async (systemId: string): Promise<SubscriptionStatus> => {
    const response = await api.get(`/feed-providers/${systemId}/subscription-status`);
    return response.data;
  },

  setFeedProviderEnabled: async (systemId: string, enabled: boolean): Promise<void> => {
    await api.post(`/feed-providers/${systemId}/set-enabled?enabled=${enabled}`);
  },

  // Bulk operations
  bulkStartSubscriptions: async (systemIds: string[]): Promise<Record<string, string>> => {
    const response = await api.post('/feed-providers/bulk/start', systemIds);
    return response.data;
  },

  bulkStopSubscriptions: async (systemIds: string[]): Promise<Record<string, string>> => {
    const response = await api.post('/feed-providers/bulk/stop', systemIds);
    return response.data;
  },

  bulkRestartSubscriptions: async (systemIds: string[]): Promise<Record<string, string>> => {
    const response = await api.post('/feed-providers/bulk/restart', systemIds);
    return response.data;
  },

  bulkSetEnabled: async (
    systemIds: string[],
    enabled: boolean
  ): Promise<Record<string, string>> => {
    const response = await api.post('/feed-providers/bulk/set-enabled', { systemIds, enabled });
    return response.data;
  },

  // Cache operations
  getCacheKeys: async (): Promise<string[]> => {
    const response = await api.get('/cache_keys');
    return response.data;
  },

  clearVehicleCache: async (): Promise<number> => {
    const response = await api.post('/clear_vehicle_cache');
    return response.data;
  },

  clearOldCache: async (): Promise<string[]> => {
    const response = await api.post('/clear_old_cache');
    return response.data;
  },

  clearDatabase: async (): Promise<void> => {
    await api.post('/clear_db');
  },

  // Vehicle orphan operations
  getVehicleOrphans: async (): Promise<string[]> => {
    const response = await api.get('/vehicle_orphans');
    return response.data;
  },

  clearVehicleOrphans: async (): Promise<string[]> => {
    const response = await api.delete('/vehicle_orphans');
    return response.data;
  },
};
