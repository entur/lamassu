import axios from 'axios';
import type { ValidationReports } from '../types/validation.ts';

const api = axios.create({
  baseURL: '/validation',
});

export const validationApi = {
  getValidationReports: async (): Promise<ValidationReports> => {
    const response = await api.get('/systems');
    return response.data;
  },
};
