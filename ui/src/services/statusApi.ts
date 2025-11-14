import axios from 'axios';
import { PublicFeedProviderStatus } from '../types/status';

// Detect the base path from the current URL to support reverse proxies
// e.g., /status/ui or /mobility/v2/status/ui
const getBasePath = () => {
  const match = window.location.pathname.match(/^(.*)\/status\/ui/);
  return match ? match[1] + '/status' : '/status';
};

const api = axios.create({
  baseURL: getBasePath(),
  headers: {
    'Content-Type': 'application/json',
  },
});

export const statusApi = {
  /**
   * Get all feed providers with public status information
   */
  getPublicFeedProviders: async (): Promise<PublicFeedProviderStatus[]> => {
    const response = await api.get<PublicFeedProviderStatus[]>('/feed-providers');
    return response.data;
  },

  /**
   * Get a single feed provider's public status
   */
  getPublicFeedProvider: async (systemId: string): Promise<PublicFeedProviderStatus> => {
    const response = await api.get<PublicFeedProviderStatus>(`/feed-providers/${systemId}`);
    return response.data;
  },
};
