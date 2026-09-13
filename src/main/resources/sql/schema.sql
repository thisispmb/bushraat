CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100)        NOT NULL,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    role          VARCHAR(20)         NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT users_role_check CHECK (role IN ('USER', 'LIBRARIAN', 'SUPER_ADMIN'))
);


CREATE TABLE categories
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);


CREATE TABLE books
(
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    author           VARCHAR(255) NOT NULL,
    category_id      BIGINT REFERENCES categories (id),
    description      TEXT,
    cover_image_path TEXT,
    file_path        TEXT,
    file_type        VARCHAR(10)  NOT NULL,
    uploaded_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT books_file_type_check
        CHECK (file_type = 'PDF')
);


CREATE TABLE reading_progress
(
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    book_id        BIGINT    NOT NULL REFERENCES books (id) ON DELETE CASCADE,
    last_page_read INTEGER   NOT NULL DEFAULT 1,
    last_read_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT reading_progress_unique
        UNIQUE (user_id, book_id)
);


CREATE TABLE favorites
(
    id       BIGSERIAL PRIMARY KEY,
    user_id  BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    book_id  BIGINT    NOT NULL REFERENCES books (id) ON DELETE CASCADE,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT favorites_unique
        UNIQUE (user_id, book_id)
);

CREATE TABLE IF NOT EXISTS password_reset_tokens
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at    TIMESTAMP
);
