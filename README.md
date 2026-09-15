# Bushraat

Bushraat is a server-rendered digital library built with **Jakarta Servlets, JDBC, Thymeleaf, and PostgreSQL**. It uses
**Supabase PostgreSQL** for the database and **Cloudflare R2** for book and cover storage.

## Stack

* Java 21
* Jakarta Servlet 6.1 / Tomcat 11
* PostgreSQL + JDBC + HikariCP
* Supabase PostgreSQL
* Cloudflare R2
* Thymeleaf
* Vanilla JavaScript
* PDF.js
* BCrypt

## Storage

```text
Browser
   |
   v
Jakarta Servlets
   |----------> Supabase PostgreSQL
   |
   +----------> Cloudflare R2
                 ├── books/
                 └── covers/
```

## Configuration

Create `.env` from `.env.example` and set:

```text
R2_ACCOUNT_ID
R2_ACCESS_KEY_ID
R2_SECRET_ACCESS_KEY
R2_BUCKET_NAME

BUSHRAAT_DB_URL
BUSHRAAT_DB_USER
BUSHRAAT_DB_PASSWORD
```

`.env` is ignored by Git. **Never commit credentials.**

For deployment, configure these variables in the hosting provider.

## Database

Run:

```text
src/main/resources/sql/schema.sql
```

Promote the first administrator if needed:

```sql
UPDATE users
SET role = 'SUPER_ADMIN'
WHERE email = 'your-email@example.com';
```

## Run

```bash
./mvnw clean package
```

Windows:

```powershell
.\mvnw.cmd clean package
```

Deploy:

```text
target/bushraat-1.0-SNAPSHOT.war
```

to Tomcat 11.

## Features

* Authentication and profiles
* Book catalog and search
* Favorites
* Continue reading and progress tracking
* PDF.js reader
* R2 book and cover storage
* Admin book/category management
* Librarian and Super Admin roles
* Password reset
* Custom error pages

## Docker

```bash
docker build -t bushraat .
docker run --rm -p 8080:8080 --env-file .env bushraat
```
