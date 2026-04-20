-- Restaurant Management System
-- MySQL Schema

CREATE DATABASE IF NOT EXISTS restaurant_management;
USE restaurant_management;

-- ─────────────────────────────────────────────
-- Tables
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS waiters (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    name   VARCHAR(100) NOT NULL,
    phone  VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS restaurant_tables (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    table_number INT         NOT NULL UNIQUE,
    capacity     INT         NOT NULL,
    status       ENUM('AVAILABLE','OCCUPIED') NOT NULL DEFAULT 'AVAILABLE'
);

CREATE TABLE IF NOT EXISTS menu_items (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100)   NOT NULL,
    category         VARCHAR(50)    NOT NULL,
    base_price       DECIMAL(10,2)  NOT NULL,
    description      TEXT,
    prep_time_minutes INT           NOT NULL DEFAULT 10,
    is_available     BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS customers (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    phone      VARCHAR(20),
    email      VARCHAR(100),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    table_id     INT          NOT NULL,
    customer_id  INT,
    waiter_name  VARCHAR(100),
    status       ENUM('RECEIVED','COOKING','READY','SERVED','CANCELLED') NOT NULL DEFAULT 'RECEIVED',
    total_price  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    notes        TEXT,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (table_id)    REFERENCES restaurant_tables(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE IF NOT EXISTS order_items (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    order_id             INT           NOT NULL,
    menu_item_id         INT           NOT NULL,
    item_name            VARCHAR(200)  NOT NULL,
    quantity             INT           NOT NULL DEFAULT 1,
    unit_price           DECIMAL(10,2) NOT NULL,
    toppings_description VARCHAR(500),
    toppings_extra_cost  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    item_total           DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id)     REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

CREATE TABLE IF NOT EXISTS payments (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    order_id       INT           NOT NULL UNIQUE,
    amount         DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH','STRIPE','PAYPAL') NOT NULL,
    transaction_id VARCHAR(100),
    receipt_number VARCHAR(50)   NOT NULL UNIQUE,
    status         ENUM('SUCCESS','FAILED') NOT NULL DEFAULT 'SUCCESS',
    paid_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- ─────────────────────────────────────────────
-- Seed Data
-- ─────────────────────────────────────────────

INSERT INTO restaurant_tables (table_number, capacity) VALUES
(1,2),(2,4),(3,4),(4,6),(5,8),(6,2),(7,4),(8,6) AS new_t
ON DUPLICATE KEY UPDATE capacity = new_t.capacity;

INSERT INTO menu_items (name, category, base_price, description, prep_time_minutes) VALUES
('Margherita Pizza',  'Pizza',     250.00, 'Classic tomato & mozzarella',          15),
('Pepperoni Pizza',   'Pizza',     320.00, 'Loaded with spicy pepperoni',           15),
('Veggie Burger',     'Burger',    180.00, 'Fresh veggies with special sauce',      10),
('Chicken Burger',    'Burger',    220.00, 'Grilled chicken, lettuce & mayo',       12),
('Caesar Salad',      'Salad',     150.00, 'Romaine lettuce with caesar dressing',   8),
('Pasta Arrabiata',   'Pasta',     200.00, 'Spicy tomato pasta',                    12),
('Garlic Bread',      'Sides',      80.00, 'Toasted bread with garlic butter',       5),
('Coke',              'Beverage',   60.00, '300 ml chilled',                         2),
('Mango Lassi',       'Beverage',   90.00, 'Fresh mango yogurt drink',               3),
('Chocolate Brownie', 'Dessert',   120.00, 'Warm brownie with vanilla ice cream',    8) AS new_m
ON DUPLICATE KEY UPDATE base_price = new_m.base_price;
