import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';
import { moveHtmlPlugin } from './vite-plugin-move-html';

export default defineConfig(({ mode }) => ({
  plugins: [
    react(),
    // Only apply the moveHtmlPlugin in build mode
    ...(mode === 'production' ? [moveHtmlPlugin()] : []),
  ],
  base: '/',
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
    outDir: '../src/main/resources/static',
    emptyOutDir: false,
    rollupOptions: {
      input: {
        admin: resolve(__dirname, 'admin.html'),
        status: resolve(__dirname, 'status.html'),
      },
      output: {
        entryFileNames: chunkInfo => {
          if (chunkInfo.name === 'admin') {
            return 'admin/ui/[name]-[hash].js';
          }
          if (chunkInfo.name === 'status') {
            return 'status/ui/[name]-[hash].js';
          }
          return '[name]-[hash].js';
        },
        chunkFileNames: 'common/[name]-[hash].js',
        assetFileNames: assetInfo => {
          if (assetInfo.name?.endsWith('.css')) {
            return '[name]-[hash].css';
          }
          return 'assets/[name]-[hash][extname]';
        },
      },
    },
  },
}));
