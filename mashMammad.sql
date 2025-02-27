drop database mash_mammad;
create database mash_mammad;
use mash_mammad;


CREATE TABLE admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    adminId VARCHAR(45) NOT NULL,
    isDeleted bool DEFAULT 0,
    foreign key (adminId) references users(username)
);


CREATE TABLE city (
    city VARCHAR(45) PRIMARY KEY
);

CREATE TABLE users (
    username VARCHAR(45) PRIMARY KEY,
    password VARCHAR(45) NOT NULL,
    name VARCHAR(45) NOT NULL,
    lastName VARCHAR(45) NOT NULL,
    phoneNum VARCHAR(15) NOT NULL,
    isDeleted bool DEFAULT 0
);

CREATE TABLE address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    city VARCHAR(45) NOT NULL,
    userId VARCHAR(45) NOT NULL,
    address VARCHAR(200) NOT NULL,
    isDefault bool NOT NULL,
    mapLoc VARCHAR(100) NOT NULL,
    isDeleted bool DEFAULT 0,
    FOREIGN KEY (city) REFERENCES city(city),
    FOREIGN KEY (userId) REFERENCES users(username)
);
ALTER TABLE `restaurant` 
DROP FOREIGN KEY `restaurant_ibfk_2`;
ALTER TABLE `restaurant` 
DROP INDEX `managerId`;

CREATE TABLE restaurant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    managerId varchar(45),
    name VARCHAR(45) NOT NULL,
    profileImage MEDIUMBLOB,
    minPurchase float NOT NULL,
    city VARCHAR(45) NOT NULL,
    address VARCHAR(200) NOT NULL,
    mapLoc VARCHAR(200) NOT NULL,
    isDeleted bool DEFAULT 0,
    FOREIGN KEY (city) REFERENCES city(city),
    foreign key (managerId) references users(username)
);

CREATE TABLE openDay (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    restaurantId BIGINT NOT NULL,
    weekday ENUM('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday') NOT NULL,
    isDeleted bool DEFAULT 0,
    FOREIGN KEY (restaurantId) REFERENCES restaurant(id)
);

CREATE TABLE openHour (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openHour TIME NOT NULL,
    closeHour TIME NOT NULL,
    openDayId BIGINT NOT NULL,
    isDeleted bool DEFAULT 0,
    FOREIGN KEY (openDayId) REFERENCES openDay(id)
);

CREATE TABLE orderStatus (
    statusName VARCHAR(45) PRIMARY KEY
);

CREATE TABLE userOrder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    addressId BIGINT NOT NULL,
    orderTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    isPaid bool NOT NULL,
    orderStatus VARCHAR(45) NOT NULL,
    isDeleted bool DEFAULT 0,
    FOREIGN KEY (addressId) REFERENCES address(id),
    FOREIGN KEY (orderStatus) REFERENCES orderStatus(statusName)
);

CREATE TABLE item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    restaurantId BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    image MEDIUMBLOB,
    price float NOT NULL,
    ingredient VARCHAR(100) NOT NULL,
    isDeleted bool DEFAULT 0,
    FOREIGN KEY (restaurantId) REFERENCES restaurant(id)
);

CREATE TABLE orderItem (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    orderId BIGINT NOT NULL,
    itemId BIGINT NOT NULL,
    quantity INT NOT NULL,
    price float NOT NULL,
    isDeleted bool DEFAULT 0,
    FOREIGN KEY (orderId) REFERENCES userOrder(id),
    FOREIGN KEY (itemId) REFERENCES item(id)
);

CREATE TABLE feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    orderItemId BIGINT NOT NULL,
    reviewText TEXT,
    rate INT NOT NULL,
    isDeleted bool DEFAULT 0,
    FOREIGN KEY (orderItemId) REFERENCES orderItem(id)
);

CREATE TABLE category (
    categoryName VARCHAR(100) PRIMARY KEY,
    isDeleted bool DEFAULT 0
);

CREATE TABLE itemCategory (
    itemId BIGINT not null,
    categoryName VARCHAR(100) NOT NULL,
    PRIMARY KEY (categoryName, itemId),
    isDeleted bool DEFAULT 0,
    FOREIGN KEY (itemId) REFERENCES item(id),
    FOREIGN KEY (categoryName) REFERENCES category(categoryName)
);

CREATE TABLE deliveryFee (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
    restaurantId BIGINT not null,
    maxDistance int not null,
    isDeleted bool DEFAULT 0,
    cost float not null,
    FOREIGN KEY (restaurantId) REFERENCES restaurant(id)
);


-- Admin queries ---------------------------------------------------------------------------------------------------------------------

# add restaurant
INSERT INTO restaurant (managerId, name, profileImage, minPurchase, city, address, mapLoc)
VALUES ('manager_username', 'Restaurant Name', NULL, 100.0, 'City Name', 'Restaurant Address', 'Location Coordinates');

#add user
INSERT INTO users (username, password, name, lastName, phoneNum)
VALUES ('username', 'password', 'First Name', 'Last Name', '1234567890');


-- USERS TABLE  ---------------------------------------------------------------------------------
-- View all users
SELECT * FROM users WHERE isDeleted = 0;

-- Edit a user’s information
UPDATE users
SET password = 'new_password', name = 'New Name', lastName = 'New Last Name', phoneNum = '0987654321'
WHERE username = 'specific_username';

-- Mark a user as deleted
UPDATE users
SET isDeleted = 1
WHERE username = 'specific_username';

-- ADDRESS TABLE  ---------------------------------------------------------------------------------
-- View all addresses
SELECT * FROM address WHERE isDeleted = 0;

-- Edit an address
UPDATE address
SET city = 'New City', address = 'Updated Address', isDefault = 1, mapLoc = 'Updated Location Coordinates'
WHERE id = specific_address_id;

-- Mark an address as deleted
UPDATE address
SET isDeleted = 1
WHERE id = specific_address_id;

-- RESTAURANT TABLE. ---------------------------------------------------------------------------------
-- View all restaurants
SELECT * FROM restaurant WHERE isDeleted = 0;

-- Edit restaurant details
UPDATE restaurant
SET name = 'Updated Restaurant Name', profileImage = LOAD_FILE('/path/to/updated_image.jpg'), minPurchase = 150.0, city = 'Updated City', address = 'Updated Address', mapLoc = 'Updated Location Coordinates'
WHERE id = specific_restaurant_id;

-- Mark a restaurant as deleted
UPDATE restaurant
SET isDeleted = 1
WHERE id = specific_restaurant_id;

-- ITEM TABLE  ---------------------------------------------------------------------------------
-- View all items  
SELECT * FROM item WHERE isDeleted = 0;

-- Edit an item
UPDATE item
SET title = 'Updated Item Title', price = 20.0, ingredient = 'Updated Ingredients'
WHERE id = specific_item_id;

-- Mark an item as deleted
UPDATE item
SET isDeleted = 1
WHERE id = specific_item_id;

-- USER ORDER TABLE  ---------------------------------------------------------------------------------
-- View all orders
SELECT * FROM userOrder WHERE isDeleted = 0;

-- Edit an order status
UPDATE userOrder
SET orderStatus = 'Updated Status'
WHERE id = specific_order_id;

-- Mark an order as deleted
UPDATE userOrder
SET isDeleted = 1
WHERE id = specific_order_id;

-- ORDER ITEM TABLE  ---------------------------------------------------------------------------------
-- View all order items
SELECT * FROM orderItem WHERE isDeleted = 0;

# admin can not change these data
-- Edit an order item
-- UPDATE orderItem
-- SET quantity = 2, price = 50.0
-- WHERE id = specific_order_item_id;

-- Mark an order item as deleted
UPDATE orderItem
SET isDeleted = 1
WHERE id = specific_order_item_id;

-- FEEDBACK TABLE  ---------------------------------------------------------------------------------
-- View all feedback
SELECT * FROM feedback WHERE isDeleted = 0;

# admin can not change these data
-- Edit feedback
-- UPDATE feedback
-- SET reviewText = 'Updated Review Text', rate = 5
-- WHERE id = specific_feedback_id;

-- Mark feedback as deleted
UPDATE feedback
SET isDeleted = 1
WHERE id = specific_feedback_id;

-- CATEGORY TABLE  ---------------------------------------------------------------------------------
-- View all categories
SELECT * FROM category WHERE isDeleted = 0;

-- Edit a category
UPDATE category
SET categoryName = 'Updated Category Name'
WHERE categoryName = 'specific_category_name';

-- Mark a category as deleted
UPDATE category
SET isDeleted = 1
WHERE categoryName = 'specific_category_name';

-- ITEM CATEGORY TABLE. ---------------------------------------------------------------------------------
-- View all item-category relationships
SELECT * FROM itemCategory WHERE isDeleted = 0;

-- Edit an item-category relationship
UPDATE itemCategory
SET categoryName = 'Updated Category Name'
WHERE itemId = specific_item_id AND categoryName = 'specific_category_name';

-- Mark an item-category relationship as deleted
UPDATE itemCategory
SET isDeleted = 1
WHERE itemId = specific_item_id AND categoryName = 'specific_category_name';

-- DELIVERY FEE TABLE ---------------------------------------------------------------------------------
-- View all delivery fees
SELECT * FROM deliveryFee WHERE isDeleted = 0;

-- Edit a delivery fee
UPDATE deliveryFee
SET maxDistance = 20, cost = 50.0
WHERE id = specific_delivery_fee_id;

-- Mark a delivery fee as deleted
UPDATE deliveryFee
SET isDeleted = 1
WHERE id = specific_delivery_fee_id;


-- restaurant manager ---------------------------------------------------------------------------------------------------------------------
-- Change restaurant details (name, min purchase, city, address, map location)
UPDATE restaurant
SET name = 'Updated Restaurant Name', minPurchase = 150.0, city = 'Updated City', address = 'Updated Address', mapLoc = 'Updated Location Coordinates'
WHERE managerId = 'manager_username' AND id = specific_restaurant_id;

-- Change restaurant image (profile image)
UPDATE restaurant
SET profileImage = LOAD_FILE('/path/to/updated_image.jpg')
WHERE managerId = 'manager_username' AND id = specific_restaurant_id;

-- Restaurant Manager can update the food list

-- Add new menu item (food)
INSERT INTO item (restaurantId, title, image, price, ingredient)
VALUES (specific_restaurant_id, 'New Item Title', NULL, 20.0, 'New Ingredients');

-- Update item details (price, ingredients, title)
UPDATE item
SET title = 'Updated Item Title', price = 20.0, ingredient = 'Updated Ingredients'
WHERE restaurantId = specific_restaurant_id AND id = specific_item_id;

-- Remove item from menu (mark as deleted)
UPDATE item
SET isDeleted = 1
WHERE restaurantId = specific_restaurant_id AND id = specific_item_id;

-- Restaurant Manager can change the opening hours

-- Update opening hours (openDay and openHour)
UPDATE openHour
SET openHour = '08:00:00', closeHour = '22:00:00'
WHERE openDayId = specific_openDay_id;

-- Restaurant Manager can set the delivery price

-- Update delivery fee for the restaurant
UPDATE deliveryFee
SET maxDistance = 20, cost = 50.0
WHERE restaurantId = specific_restaurant_id;

-- Restaurant Manager can view orders and update their status

-- View list of pending orders (where order status is pending)
SELECT userOrder.*
FROM userOrder
JOIN orderItem ON userOrder.id = orderItem.orderId
JOIN item ON orderItem.itemId = item.id
WHERE userOrder.isDeleted = 0
  AND userOrder.orderStatus = 'Pending'
  AND item.restaurantId = (
      SELECT id FROM restaurant WHERE managerId = 'manager_username' AND isDeleted = 0
  );


-- Update order status (approve or reject)
UPDATE userOrder
SET orderStatus = 'Approved'
WHERE id = specific_order_id;

UPDATE userOrder
SET orderStatus = 'Rejected'
WHERE id = specific_order_id;

-- Restaurant Manager can review order history

-- View order history (list of all orders)
SELECT userOrder.*, orderItem.*, item.*
FROM userOrder
JOIN orderItem ON userOrder.id = orderItem.orderId
JOIN item ON orderItem.itemId = item.id
WHERE userOrder.isDeleted = 0
  AND item.restaurantId = (
      SELECT id FROM restaurant WHERE managerId = 'manager_username' AND isDeleted = 0
  )
ORDER BY userOrder.orderTime DESC;


-- customer ---------------------------------------------------------------------------------------------------------------------

-- 1. Customer Registers an Address as the Main Address
INSERT INTO address (city, userId, address, isDefault, mapLoc)
VALUES ('City Name', 'customer_username', 'Main Address', 1, 'Map Location');

-- 2. Customer Adds a New Address to the System
INSERT INTO address (city, userId, address, isDefault, mapLoc)
VALUES ('New City', 'customer_username', 'Additional Address', 0, 'New Map Location');

-- 3. Customer Views Their Own Order History
SELECT userOrder.*, orderItem.*, item.*
FROM userOrder
JOIN orderItem ON userOrder.id = orderItem.orderId
JOIN item ON orderItem.itemId = item.id
WHERE userOrder.isDeleted = 0
  AND userOrder.addressId IN (
      SELECT id FROM address WHERE userId = 'customer_username' AND isDeleted = 0
  )
ORDER BY userOrder.orderTime DESC;

-- 4. Customer Reorders an Order

-- Step 1: Find the items from a previous order
SELECT orderItem.itemId, orderItem.quantity, orderItem.price
FROM orderItem
WHERE orderItem.orderId = 'previous_order_id' AND orderItem.isDeleted = 0;

-- Step 2: Create a new order for the customer (with the same address, order status, etc.)
INSERT INTO userOrder (addressId, orderStatus, isPaid)
VALUES ('customer_address_id', 'Pending', 0);

-- Step 3: Add items to the new order
INSERT INTO orderItem (orderId, itemId, quantity, price)
VALUES ('new_order_id', 'item_id', quantity, price);











-- Insert data into users
INSERT INTO users (username, password, name, lastName, phoneNum)
VALUES 
('john_doe', 'password123', 'John', 'Doe', '1234567890'),
('jane_smith', 'password456', 'Jane', 'Smith', '0987654321');

-- Insert data into city
INSERT INTO city (city)
VALUES 
('New York'),
('Los Angeles');

-- Insert data into admins
INSERT INTO admins (adminId)
VALUES 
('john_doe');  -- assuming john_doe is an admin

-- Insert data into address
INSERT INTO address (city, userId, address, isDefault, mapLoc)
VALUES 
('New York', 'john_doe', '123 Main St, NY', 1, '40.7128, -74.0060'),
('Los Angeles', 'jane_smith', '456 Sunset Blvd, LA', 0, '34.0522, -118.2437');

-- Insert data into restaurant
INSERT INTO restaurant (managerId, name, profileImage, minPurchase, city, address, mapLoc)
VALUES 
('john_doe', 'John’s Diner', NULL, 50.0, 'New York', '123 Main St, NY', '40.7128, -74.0060'),
('jane_smith', 'Jane’s Café', NULL, 60.0, 'Los Angeles', '456 Sunset Blvd, LA', '34.0522, -118.2437');

-- Insert data into openDay
INSERT INTO openDay (restaurantId, weekday)
VALUES 
(1, 'Monday'),
(1, 'Tuesday'),
(2, 'Wednesday');

-- Insert data into openHour
INSERT INTO openHour (openHour, closeHour, openDayId)
VALUES 
('08:00:00', '22:00:00', 1),
('08:00:00', '22:00:00', 2),
('09:00:00', '20:00:00', 3);

-- Insert data into orderStatus
INSERT INTO orderStatus (statusName)
VALUES 
('Pending'),
('Approved'),
('Rejected');

-- Insert data into userOrder
INSERT INTO userOrder (addressId, orderStatus, isPaid)
VALUES 
(1, 'Pending', 0),
(2, 'Approved', 1);

-- Insert data into item
INSERT INTO item (restaurantId, title, price, ingredient)
VALUES 
(1, 'Burger', 15.0, 'Beef, Lettuce, Tomato'),
(1, 'Fries', 5.0, 'Potatoes'),
(2, 'Coffee', 3.0, 'Coffee Beans');

-- Insert data into orderItem
INSERT INTO orderItem (orderId, itemId, quantity, price)
VALUES 
(1, 1, 2, 15.0),
(2, 3, 1, 3.0);

-- Insert data into feedback
INSERT INTO feedback (orderItemId, reviewText, rate)
VALUES 
(1, 'Great burger!', 5),
(2, 'Delicious coffee', 4);

-- Insert data into category
INSERT INTO category (categoryName)
VALUES 
('Burgers'),
('Beverages');

-- Insert data into itemCategory
INSERT INTO itemCategory (itemId, categoryName)
VALUES 
(1, 'Burgers'),
(3, 'Beverages');

-- Insert data into deliveryFee
INSERT INTO deliveryFee (restaurantId, maxDistance, cost)
VALUES 
(1, 10, 5.0),
(2, 15, 7.0);


use mash_mammad;
DELIMITER $$

CREATE PROCEDURE AuthenticateUser(
    IN p_username VARCHAR(45),
    IN p_password VARCHAR(45),
    OUT p_isAuthenticated BOOLEAN
)
BEGIN
    -- Check if the user exists and the password matches
    SELECT 
        CASE 
            WHEN EXISTS (
                SELECT 1 
                FROM users 
                WHERE username = p_username 
                AND password = p_password 
                AND isDeleted = 0
            ) THEN TRUE
            ELSE FALSE
        END
    INTO p_isAuthenticated;
END $$

DELIMITER ;
-- 
drop procedure registeruser;

DELIMITER //

CREATE PROCEDURE RegisterUser (
    IN p_username VARCHAR(45),
    IN p_password VARCHAR(45),
    IN p_name VARCHAR(45),
    IN p_lastName VARCHAR(45),
    IN p_phoneNum VARCHAR(15),
    OUT isRegistered BOOLEAN
)
BEGIN
    DECLARE userExists INT;

    -- Check if username already exists
    SELECT COUNT(*)
    INTO userExists
    FROM users
    WHERE username = p_username;

    IF userExists > 0 THEN
        SET isRegistered = FALSE; -- User already exists
    ELSE
        -- Insert new user
        INSERT INTO users (username, password, name, lastName, phoneNum)
        VALUES (p_username, p_password, p_name, p_lastName, p_phoneNum);
        
        SET isRegistered = TRUE;
    END IF;
END //

DELIMITER ;

INSERT INTO city (city) VALUES ('Tehran');
INSERT INTO city (city) VALUES  ('Shiraz');
INSERT INTO city (city) VALUES ('Isfahan');
INSERT INTO city (city) VALUES ('Mashhad');
INSERT INTO city (city) VALUES ('Tabriz');


