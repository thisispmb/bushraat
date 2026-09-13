# Bushraat

Bushraat is a server-rendered digital library built with Jakarta Servlets, JDBC, Thymeleaf and PostgreSQL.

## Stack

- Java 21
- Jakarta Servlet 6.1 / Tomcat 11+
- PostgreSQL
- JDBC + HikariCP
- Thymeleaf 3.1
- BCrypt
- Vanilla JavaScript

## Local setup

1. Create a PostgreSQL database named `bushraat`.
2. Run `src/main/resources/sql`.
3. Check `Db.java` and set the local PostgreSQL connection values if they differ from your machine.
4. Deploy the generated WAR to Tomcat or run it from your IDE.
5. The application stores uploaded books and covers in `uploads/books` and `uploads/covers` under the configured storage
   root. Set `-Dbushraat.upload.dir=/absolute/path/to/uploads` to change it.

## Main routes

- `/` landing page
- `/login`, `/register`
- `/dashboard`
- `/saved`
- `/profile`
- `/books/{id}`
- `/read/{id}`
- `/admin`
- `/forgot-password`
- `/reset-password`

## Reader

PDF files are opened using the browser's native PDF viewer. The current PDF page can be saved from the reader toolbar.
PDF is the only supported book format in the current MVP.

## Password reset

Because this version is designed to run locally without an email provider, password reset links are generated on the
reset page for the local installation. The reset token is stored as a SHA-256 hash and expires after 30 minutes.

## Security notes

- Passwords are stored using BCrypt hashes.
- Authentication uses HTTP sessions.
- Admin routes are role protected.
- Uploaded filenames are replaced with UUIDs.
- Stored file paths are resolved inside the configured upload root.

## Role migration

This version uses three roles: `USER`, `LIBRARIAN`, and `SUPER_ADMIN`.

For an existing database, run `src/main/resources/sql` once before deploying. Then promote one or more trusted accounts
to `SUPER_ADMIN`.

The application allows multiple Super Administrators, but server-side transaction locking prevents the last Super
Administrator from being deleted or demoted. Librarians can manage books/categories and remove only `USER` accounts;
only Super Administrators can change roles.
