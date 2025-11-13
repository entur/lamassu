import axios from 'axios';
import { PublicFeedProviderStatus } from '../types/status';

const api = axios.create({
  baseURL: '/status',
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
