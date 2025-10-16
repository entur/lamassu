import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: process.env.PORT ? Number(process.env.PORT) : 5000,
    proxy: {
      '/admin/': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/validation/': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    outDir: '../src/main/resources/static/admin/ui',
    emptyOutDir: true,
  },
});
