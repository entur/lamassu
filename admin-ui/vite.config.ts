import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Check if building for status UI (set via env var)
const isStatusBuild = process.env.BUILD_TARGET === 'status';

export default defineConfig({
  plugins: [react()],
  // Set base to the directory where static assets are served from
  // But we'll handle routing with absolute paths in React Router
  base: isStatusBuild ? '/status/ui/' : '/admin/ui/',
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
      '/status/': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    outDir: isStatusBuild
      ? '../src/main/resources/static/status/ui'
      : '../src/main/resources/static/admin/ui',
    emptyOutDir: true,
    rollupOptions: isStatusBuild
      ? {
          input: './index-status.html',
        }
      : undefined,
  },
});
