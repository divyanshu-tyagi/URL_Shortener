# URL Shortener API

Production-grade URL shortener with **Redis caching**, **sliding window rate limiting**, **click analytics**, and **Prometheus metrics**.

**Stack:** Kotlin · Spring Boot 3 · PostgreSQL · Redis · Docker · GitHub Actions

---

## Quick Start (Docker)

```bash
git clone https://github.com/divyanshu-tyagi/url-shortener
cd url-shortener
docker compose up --build
```

| Service    | URL                              |
|------------|----------------------------------|
| API        | http://localhost:8080            |
| Prometheus | http://localhost:9090            |
| Grafana    | http://localhost:3000 (admin/admin) |

---

## API Usage

### 1. Generate an API Key
```bash
curl -X POST http://localhost:8080/api/auth/keys \
  -H "Content-Type: application/json" \
  -d '{"ownerName": "Divyanshu"}'
```
Save the returned `apiKey` — it's shown only once.

### 2. Shorten a URL
```bash
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -H "X-API-Key: sk_your_key_here" \
  -d '{"url": "https://github.com/divyanshu-tyagi"}'
```

### 3. Redirect
```bash
curl -L http://localhost:8080/aB3xK9
```

### 4. Analytics
```bash
curl http://localhost:8080/api/urls/aB3xK9/analytics \
  -H "X-API-Key: sk_your_key_here"
```

### 5. Rate limit headers (every response)
```
X-RateLimit-Limit:     10
X-RateLimit-Remaining: 7
X-RateLimit-Reset:     1715123460
```

---

## Architecture

```
Client → Spring Boot App
           ├── ApiKeyAuthFilter   (auth + rate limiting)
           ├── RedirectController (cache-aside lookup + async tracking)
           ├── UrlController      (CRUD + analytics)
           └── AuthController     (API key generation)
                    │
         ┌──────────┴──────────┐
      PostgreSQL             Redis
   (URLs, clicks,         (URL cache,
    API keys)            rate counters)
```

## Key Design Decisions

| Decision | Why |
|----------|-----|
| Base62 over UUID | Shorter codes, zero collisions, deterministic |
| Sliding window rate limit | Prevents burst at window boundary vs fixed window |
| Async click tracking | Redirect latency unaffected by DB write |
| Cache null sentinel | Prevents DB hammering for dead/expired links |
| Fail-open on Redis down | Redis outage never breaks the app |

---

## Metrics (Prometheus)

| Metric | Description |
|--------|-------------|
| `urls_created_total` | Short URLs created |
| `url_redirects_total` | Redirects served |
| `rate_limit_hits_total` | Rate limit rejections |
| `cache_hits_total` | Redis cache hits |
| `cache_misses_total` | Redis cache misses |