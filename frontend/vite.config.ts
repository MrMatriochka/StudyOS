import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Front servi sur 5173 (seule origine autorisee par le CORS du back).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
  },
});
