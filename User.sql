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


DELIMITER //




CREATE PROCEDURE GetCities()
BEGIN
    SELECT city FROM city;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE AddCity(
    IN p_city VARCHAR(45)
)
BEGIN
    -- Check if the city already exists
    IF NOT EXISTS (SELECT * FROM city WHERE city = p_city) THEN
        INSERT INTO city (city) VALUES (p_city);
    END IF;
END //

DELIMITER ;



DELIMITER ;


DELIMITER $$

-- Retrieve all cities
CREATE PROCEDURE GetAllCities()
BEGIN
    SELECT city FROM city WHERE isDeleted = 0;
END$$

-- Add a new city
CREATE PROCEDURE AddCity(IN cityName VARCHAR(45))
BEGIN
    IF NOT EXISTS (SELECT * FROM city WHERE city = cityName) THEN
        INSERT INTO city (city) VALUES (cityName);
    END IF;
END$$

-- Insert new address for user
CREATE PROCEDURE AddAddress(
    IN userId VARCHAR(45),
    IN cityName VARCHAR(45),
    IN addressText VARCHAR(200),
    IN mapLocation VARCHAR(100),
    IN isDefaultAddress BOOL
)
BEGIN
    INSERT INTO address (city, userId, address, mapLoc, isDefault)
    VALUES (cityName, userId, addressText, mapLocation, isDefaultAddress);
END$$

DELIMITER ;


-- SetDefaultAddress
DELIMITER $$

CREATE PROCEDURE SetDefaultAddress(
    IN userId VARCHAR(45),
    IN addressId INT
)
BEGIN
    -- Reset all addresses for the user
    UPDATE address
    SET isDefault = FALSE
    WHERE userId = userId;

    -- Set the selected address as default
    UPDATE address
    SET isDefault = TRUE
    WHERE id = addressId;
END$$

DELIMITER ;




-- GetPaginatedRestaurants
DELIMITER $$
CREATE PROCEDURE GetPaginatedFoods(
    IN restaurantId INT,
    IN pageSize INT,
    IN offset INT
)
BEGIN
    SELECT i.id, 
           i.title, 
           i.price, 
           i.ingredient, 
           c.categoryName AS category
    FROM item i
    LEFT JOIN itemCategory ic ON i.id = ic.itemId
    LEFT JOIN category c ON ic.categoryName = c.categoryName
    WHERE i.restaurantId = restaurantId 
      AND i.isDeleted = 0
    ORDER BY i.id ASC
    LIMIT pageSize OFFSET offset;
END$$
DELIMITER ;


-- GetFoodsByRestaurantName
DELIMITER $$

CREATE PROCEDURE GetFoodsByRestaurant(
    IN restaurantId INT
)
BEGIN
    SELECT title, price, ingredient
    FROM item
    WHERE restaurantId = restaurantId AND isDeleted = 0;

END$$

DELIMITER ;


-- GetPaidOrders
DELIMITER $$

CREATE PROCEDURE GetPaidOrders(
IN userId VARCHAR(45)
)
BEGIN
    SELECT o.id, o.orderTime, SUM(oi.price * oi.quantity) AS totalPrice
    FROM userOrder o
    JOIN orderItem oi ON o.id = oi.orderId
    JOIN address a ON o.addressId = a.id
    WHERE o.isPaid = 1 AND o.isDeleted = 0 AND a.userId = userId
    GROUP BY o.id;
END$$

DELIMITER ;



-- GetUnpaidOrders

DELIMITER $$
CREATE PROCEDURE GetUnpaidOrders(IN userId VARCHAR(45))
BEGIN
    SELECT o.id, o.orderTime, SUM(oi.price * oi.quantity) AS totalPrice
    FROM userOrder o
    JOIN orderItem oi ON o.id = oi.orderId
    JOIN address a ON o.addressId = a.id
    WHERE o.isPaid = 0 AND o.isDeleted = 0 AND a.userId = userId
    GROUP BY o.id;
END$$

DELIMITER ;



-- GetOrCreateOrder
DELIMITER $$

CREATE PROCEDURE GetOrCreateOrder(
    IN p_restaurantId INT,
    IN p_userId VARCHAR(45),
    OUT p_orderId INT
)
BEGIN
    DECLARE existingOrderId INT;
    DECLARE defaultAddressId INT;

    -- Retrieve the user's default address
    SELECT id INTO defaultAddressId
    FROM address
    WHERE userId = p_userId AND isDefault = TRUE AND isDeleted = FALSE
    LIMIT 1;

    -- Check if the default address exists
    IF defaultAddressId IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'User does not have a default address.';
    END IF;

    -- Check for an existing unpaid order for the given user and restaurant through orderItem and item
    SELECT o.id
    INTO existingOrderId
    FROM userOrder o
    INNER JOIN orderItem oi ON o.id = oi.orderId
    INNER JOIN item i ON oi.itemId = i.id
    WHERE o.isPaid = 0
      AND o.isDeleted = 0
      AND i.restaurantId = p_restaurantId
      AND o.addressId = defaultAddressId
    LIMIT 1;

    -- If an unpaid order exists, return its ID
    IF existingOrderId IS NOT NULL THEN
        SET p_orderId = existingOrderId;
    ELSE
        -- Otherwise, create a new order linked to the user's default address
        INSERT INTO userOrder (addressId, isPaid, orderStatus)
        VALUES (defaultAddressId, 0);
        SET p_orderId = LAST_INSERT_ID();
    END IF;
END$$

DELIMITER ;


-- AddFoodToOrder
DELIMITER $$

CREATE PROCEDURE AddFoodToOrder(
    IN orderId INT,
    IN foodId INT,
    IN quantity INT
)
BEGIN
    INSERT INTO orderItem (orderId, itemId, quantity, price)
    VALUES (orderId, foodId, quantity, (SELECT price FROM item WHERE id = foodId))
    ON DUPLICATE KEY UPDATE
        quantity = quantity + VALUES(quantity);
END$$

DELIMITER ;


-- RemoveFoodFromOrder
DELIMITER $$

CREATE PROCEDURE RemoveFoodFromOrder(
    IN orderId INT,
    IN foodId INT
)
BEGIN
    DELETE FROM orderItem WHERE orderId = orderId AND itemId = foodId;
END$$

DELIMITER ;

-- PayForOrder
DELIMITER $$

CREATE PROCEDURE PayForOrder(
    IN orderId INT
)
BEGIN
    UPDATE userOrder SET isPaid = 1 WHERE id = orderId;
END$$

DELIMITER ;


-- UpdateUserInfo
DELIMITER $$

CREATE PROCEDURE UpdateUserInfo(
    IN p_oldUsername VARCHAR(45),
    IN p_newUsername VARCHAR(45),
    IN p_firstName VARCHAR(45),
    IN p_lastName VARCHAR(45),
    IN p_password VARCHAR(45)
)
BEGIN
    -- Update the user's information. Only update non-NULL fields.
    UPDATE users
    SET
        username = IFNULL(p_newUsername, username),
        name = IFNULL(p_firstName, name),
        lastName = IFNULL(p_lastName, lastName),
        password = IFNULL(p_password, password)
    WHERE username = p_oldUsername AND isDeleted = 0;
END$$

DELIMITER ;


-- GetOwnedRestaurants

DELIMITER //

CREATE PROCEDURE GetOwnedRestaurants(
    IN managerUsername VARCHAR(45),
    IN offsetValue INT,
    IN limitValue INT
)
BEGIN
    SELECT id, name, city, address, minPurchase
    FROM restaurant
    WHERE managerId = managerUsername AND isDeleted = 0
    ORDER BY id
    LIMIT limitValue OFFSET offsetValue;
END //

DELIMITER ;

-- UpdateRestaurantName
DELIMITER //

CREATE PROCEDURE UpdateRestaurantName(
    IN restaurantId BIGINT,
    IN newName VARCHAR(45)
)
BEGIN
    UPDATE restaurant 
    SET name = newName 
    WHERE id = restaurantId AND isDeleted = 0;
END //

DELIMITER ;

DELIMITER $$


-- PayOrder
CREATE PROCEDURE PayOrder(
    IN p_orderId BIGINT
)
BEGIN
    UPDATE userOrder
    SET isPaid = TRUE
    WHERE id = p_orderId AND isPaid = FALSE AND isDeleted = FALSE;
END$$

DELIMITER ;



-- UpdateRestaurantSettings

DELIMITER $$
CREATE PROCEDURE UpdateRestaurantSettings(
    IN p_id BIGINT,
    IN p_name VARCHAR(45),
    IN p_image MEDIUMBLOB,
    IN p_city VARCHAR(45),
    IN p_address VARCHAR(200),
    IN p_mapLoc VARCHAR(200)
)
BEGIN
    UPDATE restaurant
    SET 
        name = IF(p_name IS NULL OR p_name = '', name, p_name),
        profileImage = IF(p_image IS NULL, profileImage, p_image),
        city = IF(p_city IS NULL OR p_city = '', city, p_city),
        address = IF(p_address IS NULL OR p_address = '', address, p_address),
        mapLoc = IF(p_mapLoc IS NULL OR p_mapLoc = '', mapLoc, p_mapLoc)
    WHERE id = p_id;
END $$

DELIMITER ;

DELIMITER $$
CREATE PROCEDURE GetPaginatedRestaurants(
    IN pageSize INT,
    IN offset INT
)
BEGIN
    SELECT id, name, city, address, minPurchase
    FROM restaurant
    WHERE isDeleted = 0
    LIMIT pageSize OFFSET offset;
END$$
DELIMITER ;