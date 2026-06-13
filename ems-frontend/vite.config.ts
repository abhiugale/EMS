import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    host: true,
    proxy: {
      '/api': {
        target: 'http://ems-backend:8080',
        changeOrigin: true,
        secure: false,
      },
      '/ws': {
        target: 'ws://ems-backend:8080',
        ws: true,
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
