DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS item_requests;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(512) NOT NULL UNIQUE
);

CREATE TABLE item_requests (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(1000) NOT NULL,
    requestor_id BIGINT NOT NULL,
    created TIMESTAMP NOT NULL,

    CONSTRAINT fk_requestor
        FOREIGN KEY (requestor_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    available BOOLEAN NOT NULL,
    owner_id BIGINT NOT NULL,
    request_id BIGINT,

    CONSTRAINT fk_owner
        FOREIGN KEY (owner_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_request
        FOREIGN KEY (request_id)
        REFERENCES item_requests(id)
        ON DELETE SET NULL
);

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    item_id BIGINT NOT NULL,
    booker_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,

    CONSTRAINT fk_item
        FOREIGN KEY (item_id)
        REFERENCES items(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_booker
        FOREIGN KEY (booker_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);