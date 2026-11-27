# SimpleAPI

Spring Boot REST API with JWT authentication, an H2 file-based database, and CRUD for
**Customer**, **Product** and **Order**.

## Stack

- Java 21 (LTS)
- Spring Boot 3.5.x (Web, Data JPA, Security, Validation)
- H2 (file mode) — data stored under `./data/simpleapi.*`
- JWT auth (jjwt 0.12)
- springdoc-openapi (Swagger UI + OpenAPI docs)
- Maven

## Run

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

A default user is seeded on first startup: **`isidro` / `isidro`**.

## Documentation

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- H2 console: http://localhost:8080/h2-console

### H2 console access

The console is reachable remotely (`spring.h2.console.settings.web-allow-others=true`) and is
protected by **HTTP Basic** on the `/h2-console/**` route (separate security chain), so the
browser first asks for the app user:

1. Browser prompt (HTTP Basic): **`isidro` / `isidro`**
2. Then, in the console form: JDBC URL `jdbc:h2:file:./data/simpleapi;AUTO_SERVER=TRUE`,
   user `sa`, empty password.

> ⚠️ **Production warning:** exposing the H2 console publicly means a DB admin UI on the
> internet. The Basic gate uses the weak demo credential `isidro/isidro` and the DB login is
> `sa` with no password. For real use: change these credentials, put an edge gate in front
> (e.g. Cloudflare Access / IP allowlist) or — best — disable the console in production and
> inspect the DB over an SSH tunnel to `localhost` instead.

## Authentication flow

1. `POST /api/auth/login` with `{ "username": "isidro", "password": "isidro" }`
   → returns `accessToken` (15 min) + `refreshToken` (7 days).
2. Send `Authorization: Bearer <accessToken>` on all `/api/**` CRUD calls.
3. When the access token expires, `POST /api/auth/refresh` with
   `{ "refreshToken": "<token>" }` → returns a new `accessToken`.

In Swagger UI, click **Authorize** and paste the `accessToken`.

### Quick cURL example

```bash
# login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"isidro","password":"isidro"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')

# create a customer
curl -s -X POST http://localhost:8080/api/customers \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Ada Lovelace","email":"ada@example.com","phone":"+55 11 99999-0000"}'
```

## Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/login` | Authenticate, get tokens |
| POST | `/api/auth/refresh` | Renew access token |
| GET/POST | `/api/customers` | List / create customers |
| GET/PUT/DELETE | `/api/customers/{id}` | Get / update / delete a customer |
| GET/POST | `/api/products` | List / create products |
| GET/PUT/DELETE | `/api/products/{id}` | Get / update / delete a product |
| GET/POST | `/api/orders` | List / create orders |
| GET/PUT/DELETE | `/api/orders/{id}` | Get / update / delete an order |

## Entity model

```
Customer (1) ──< (N) Order (1) ──< (N) OrderItem (N) >── (1) Product
```

- `OrderItem.unitPrice` is a **snapshot** of the product price at purchase time.
- `Order.total` and `OrderItem.subtotal` are computed server-side (never sent by the client).
- Deleting a `Customer` with orders, or a `Product` referenced by orders, is blocked (HTTP 409).

## Example: create an order

```json
POST /api/orders
{
  "customerId": 1,
  "status": "PENDING",
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ]
}
```
