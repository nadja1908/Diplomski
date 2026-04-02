import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

/**
 * Lokalni razvoj: statistika ide na Columnar (9050); ostatak /api na Gateway (9000)
 * koji prosleđuje na Relational — isti tok kao login i profil (izbegava 403/CORS razlike).
 */
const gateway = process.env.VITE_GATEWAY_URL ?? 'http://localhost:9000'
const columnar = process.env.VITE_COLUMNAR_URL ?? 'http://localhost:9050'

export default defineConfig({
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
