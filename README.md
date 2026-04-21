# NAIS-projekat

## Javni demo (GitHub Pages + backend)

- Frontend na GitHub Pages; backend mora biti javno dostupan.
- **Brzo za asistentkinju (bez tvog računara):** podigni ceo stack na [Railway](https://railway.app) iz ovog repoa (`docker-compose.yml`), javni domen na servisu **gateway-api**, pa u GitHub Actions varijablu `VITE_API_BASE_URL` nalepi taj HTTPS URL i pokreni workflow *Deploy frontend to GitHub Pages*.
- Detaljno korak po korak: [deploy/railway.txt](deploy/railway.txt).

Lokalno: `docker compose up -d` u ovom folderu; gateway obično `http://localhost:9000`.