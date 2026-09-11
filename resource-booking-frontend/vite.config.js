import react, { reactCompilerPreset } from '@vitejs/plugin-react'
import babel from '@rolldown/plugin-babel'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [
    react(),
    babel({ presets: [reactCompilerPreset()] })
  ],

  server: {
    proxy: {
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/resources': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/reservations': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})