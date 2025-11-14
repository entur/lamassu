import axios from 'axios';
import type { ValidationReports } from '../types/validation.ts';

// Detect the base path from the current URL to support reverse proxies
// Works for both /admin/ui and /status/ui paths
const getBasePath = () => {
  const match = window.location.pathname.match(/^(.*?)\/(admin|status)\/ui/);
  return match ? match[1] + '/validation' : '/validation';
};

const api = axios.create({
  baseURL: getBasePath(),
});

export const validationApi = {
  getValidationReports: async (): Promise<ValidationReports> => {
    const response = await api.get('/systems');
    return response.data;
  },
};
