import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const gateway = process.env.VITE_GATEWAY_URL ?? 'http://localhost:9000'
const columnar = process.env.VITE_COLUMNAR_URL ?? 'http://localhost:9050'

export default defineConfig({
  // Relative base makes the build work on GitHub Pages project URLs.
  base: './',
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/student/me/statistics': {
        target: columnar,
        changeOrigin: true,
      },
      '/api': { target: gateway, changeOrigin: true },
    },
  },
})
