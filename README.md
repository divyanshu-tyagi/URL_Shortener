# URL Shortener API

A production-grade URL shortening service built with **Kotlin** and **Spring Boot 4**, featuring API key authentication, click analytics, Redis caching, and a full observability stack with Prometheus and Grafana.
 
---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Framework | Spring Boot 4 |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| Metrics | Prometheus + Grafana |
| Containerization | Docker + Docker Compose |
 
---

## Features

- **URL Shortening** — Generate short codes via Base62 encoding
- **Redirect** — 302 redirect from short code to original URL
- **Click Tracking** — Records device type, referrer, and timestamp per click
- **Analytics** — Per-URL stats: total clicks, last 7/30 days, daily breakdown, device breakdown, top referrers, peak hour
- **API Key Auth** — All write operations are scoped to an API key
- **Redis Caching** — Short code lookups cached for low-latency redirects
- **Observability** — Prometheus metrics exposed, Grafana dashboards available at `localhost:3000`
---

## Getting Started

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (make sure it's running)
### Run with Docker Compose

```bash
git clone https://github.com/divyanshu-tyagi/URL_Shortener
cd URL_Shortener
docker compose up
```

Wait for this line before making requests:
```
url-shortener-app | Started UrlShortnerApplicationKt in ... seconds
```

The API is now available at `http://localhost:8085`.
 
---


## API Reference

### 1. Create an API Key

All URL operations require an API key. Generate one first.

**POST** `/api/auth/keys`

```json
{
  "ownerName": "your-name"
}
```

**Response:**
```json
{
  "id": "uuid",
  "key": "sk_xxxxxxxxxxxxxxxx",
  "ownerName": "your-name",
  "createdAt": "2026-06-07T10:00:00Z"
}
```

> Save the `key` — pass it as `X-API-Key: sk_xxx` header in all subsequent requests.
 
---

### 2. Shorten a URL

**POST** `/api/urls`  
**Header:** `X-API-Key: sk_xxx`

```json
{
  "originalUrl": "https://github.com/divyanshu-tyagi"
}
```

**Response `201`:**
```json
{
  "shortCode": "000001",
  "shortUrl": "http://localhost:8085/000001",
  "originalUrl": "https://github.com/divyanshu-tyagi",
  "createdAt": "2026-06-07T10:00:00Z"
}
```
 
---

### 3. Redirect

**GET** `/{shortCode}`

Redirects to the original URL with `302 Found`. Also records the click (device type, referrer, timestamp).

```
GET http://localhost:8085/000001
→ 302 → https://github.com/divyanshu-tyagi
```
 
---

### 4. List My URLs

**GET** `/api/urls`  
**Header:** `X-API-Key: sk_xxx`

Returns all active URLs created with this API key.
 
---

### 5. Get Analytics

**GET** `/api/urls/{shortCode}/analytics`

```json
{
  "shortCode": "000001",
  "totalClicks": 42,
  "clicksLast7Days": 10,
  "clicksLast30Days": 38,
  "clicksByDay": [
    { "date": "2026-06-06", "count": 5 },
    { "date": "2026-06-07", "count": 5 }
  ],
  "deviceBreakdown": {
    "Desktop": 30,
    "Mobile": 12
  },
  "topReferrers": [
    { "referrer": "Direct", "count": 20 },
    { "referrer": "https://google.com", "count": 22 }
  ],
  "peakHour": 14
}
```
 
---

### 6. Delete a URL

**DELETE** `/api/urls/{shortCode}`  
**Header:** `X-API-Key: sk_xxx`

Deactivates the short URL. Returns `204 No Content`.
 
---

## Project Structure

```
src/
├── controller/
│   ├── AuthController.kt       # API key generation
│   ├── UrlController.kt        # Shorten, list, delete, analytics
│   └── RedirectController.kt   # Short code redirect + click tracking
├── service/
│   ├── UrlService.kt
│   ├── AnalyticsService.kt
│   ├── ClickTrackingService.kt
│   └── ApiKeyService.kt
├── repository/
│   ├── UrlRepository.kt
│   ├── ClickRepository.kt
│   └── ApiKeyRepository.kt
├── model/
├── dto/
└── security/
```
 
---

## Environment

The app runs with the `prod` profile inside Docker. Configuration is defined in `application-prod.properties` and injected via `docker-compose.yml` environment variables.

Key variables:

| Variable | Default |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/urlshortener` |
| `SPRING_REDIS_HOST` | `redis` |
| `SERVER_PORT` | `8080` |
 
---