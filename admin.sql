DELIMITER //

CREATE PROCEDURE CheckIfAdmin(
    IN input_username VARCHAR(45),
    OUT is_admin BOOLEAN
)
BEGIN
    -- Directly check if the user is an admin
    SELECT COUNT(*) > 0 INTO is_admin
    FROM admins
    WHERE adminId = input_username AND isDeleted = 0;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE AddNewAdmin(
    IN requester_username VARCHAR(45), 
    IN new_admin_username VARCHAR(45), 
    OUT success BOOLEAN
)
BEGIN
    -- Check if the requester is an admin
    IF EXISTS (
        SELECT 1 
        FROM admins 
        WHERE adminId = requester_username AND isDeleted = 0
    ) THEN
        -- Ensure the new admin exists in the users table
        IF EXISTS (
            SELECT 1 
            FROM users 
            WHERE username = new_admin_username AND isDeleted = 0
        ) THEN
            -- Check if the new admin is already in the admins table
            IF EXISTS (
                SELECT 1 
                FROM admins 
                WHERE adminId = new_admin_username
            ) THEN
                -- If the new admin exists but is marked as deleted, update isDeleted to 0
                UPDATE admins
                SET isDeleted = 0
                WHERE adminId = new_admin_username;
            ELSE
                -- Add the new admin if not already in the admins table
                INSERT INTO admins (adminId) 
                VALUES (new_admin_username);
            END IF;

            SET success = TRUE;
        ELSE
            -- New admin does not exist in users
            SET success = FALSE;
        END IF;
    ELSE
        -- Requester is not an admin
        SET success = FALSE;
    END IF;
END //

DELIMITER ;



DELIMITER //

CREATE PROCEDURE DeleteUser(IN adminUsername VARCHAR(45), IN requestingAdminUsername VARCHAR(45))
BEGIN
    -- Declare necessary variables
    DECLARE isAdmin BOOLEAN;
    DECLARE isUserExist BOOLEAN;

    -- Verify if the requesting user is an admin
    SELECT COUNT(*) > 0 INTO isAdmin
    FROM admins
    WHERE adminId = requestingAdminUsername;

    IF NOT isAdmin THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Access denied! Only admins can delete other users.';
    END IF;

    -- Verify if the user exists and is not deleted in the users table
    SELECT COUNT(*) > 0 INTO isUserExist
    FROM users
    WHERE username = adminUsername AND isDeleted = 0;

    IF NOT isUserExist THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'User does not exist or is already deleted!';
    END IF;

    -- If the user to be deleted is an admin, set their isDeleted flag in admins table
    IF isAdmin THEN
        UPDATE admins
        SET isDeleted = 1
        WHERE adminId = adminUsername;
    END IF;

    -- Set the isDeleted flag of the user in the users table
    UPDATE users
    SET isDeleted = 1
    WHERE username = adminUsername;

    -- Set the isDeleted flag of all addresses of this user to 1
    UPDATE address
    SET isDeleted = 1
    WHERE userId = adminUsername;

    -- Return a success message
    SELECT 'User deleted successfully.' AS result;
END //

DELIMITER ;



DELIMITER //

-- Add a new city
CREATE PROCEDURE AddCity(IN cityName VARCHAR(45))
BEGIN
    IF NOT EXISTS (SELECT * FROM city WHERE city = cityName) THEN
        INSERT INTO city (city) VALUES (cityName);
    END IF;
END//

DELIMITER ;

DELIMITER //
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
END//

DELIMITER ;





DELIMITER //


CREATE PROCEDURE RetrieveAdminPrivilege(
    IN adminUsername VARCHAR(45)
)
BEGIN
    -- Check if the user is an admin
    IF EXISTS (
        SELECT 1 
        FROM admins 
        WHERE adminId = adminUsername AND isDeleted = 0
    ) THEN
        -- Set the admin's isDeleted flag to 1 to remove admin privileges
        UPDATE admins
        SET isDeleted = 1
        WHERE adminId = adminUsername;

        SELECT 'Admin privileges removed successfully.' AS result;
    ELSE
        -- The user is not an admin or already deleted
        SELECT 'User is not an active admin or has already been removed.' AS result;
    END IF;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE getUsersByPage(IN pageNumber INT)
BEGIN
    DECLARE offsetValue INT;

    -- Calculate the offset based on the page number
    SET offsetValue = (pageNumber - 1) * 5;

    -- Query to fetch admin users
    SELECT u.username, u.name, u.lastName, u.phoneNum, 'Admin' AS userType
    FROM users u
    JOIN admins a ON u.username = a.adminId
    WHERE u.isDeleted = 0 AND a.isDeleted = 0
    
    UNION

    -- Query to fetch regular users
    SELECT username, name, lastName, phoneNum, 'User' AS userType
    FROM users
    WHERE isDeleted = 0 AND username NOT IN (
        SELECT adminId FROM admins WHERE isDeleted = 0
    )
    
    -- Apply pagination using LIMIT and OFFSET
    ORDER BY userType DESC, username ASC
    LIMIT 5 OFFSET offsetValue;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE GetDeletedAdminsPage(
    IN pageNumber INT,
    IN pageSize INT
)
BEGIN
    DECLARE offset INT;

    -- Calculate the offset
    SET offset = (pageNumber - 1) * pageSize;

    -- Select deleted admins with pagination
    SELECT adminId
    FROM admins
    WHERE isDeleted = 1
    ORDER BY adminId ASC
    LIMIT pageSize OFFSET offset;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE GetDeletedUsersPage(
    IN pageNumber INT,
    IN pageSize INT
)
BEGIN
    DECLARE offset INT;

    -- Calculate the offset for pagination
    SET offset = (pageNumber - 1) * pageSize;

    -- Select deleted users with pagination
    SELECT username, name, lastName, phoneNum
    FROM users
    WHERE isDeleted = 1
    ORDER BY username ASC
    LIMIT pageSize OFFSET offset;
END //

DELIMITER ;

drop procedure addrestaurant;
DELIMITER //

CREATE PROCEDURE AddRestaurant(
    IN managerId VARCHAR(45),
    IN name VARCHAR(45),
    IN profileImage MEDIUMBLOB,
    IN minPurchase FLOAT,
    IN cityName VARCHAR(45),
    IN address VARCHAR(200),
    IN mapLoc VARCHAR(200),
	OUT isSuccess BOOLEAN,
    OUT restaurantId BIGINT
 
)
BEGIN
    DECLARE isValidUser BOOLEAN;

    -- Check if the manager exists in the users table and is not deleted
    SELECT COUNT(*) > 0 INTO isValidUser
    FROM users
    WHERE username = managerId AND isDeleted = 0;

    IF NOT isValidUser THEN
        SET isSuccess = FALSE;
        SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Invalid manager! The user does not exist or is marked as deleted.';
    ELSE
        -- Check if the city exists, if not, insert it
        IF NOT EXISTS (SELECT 1 FROM city WHERE city = cityName) THEN
            INSERT INTO city (city) VALUES (cityName);
        END IF;

        -- Insert the restaurant
        INSERT INTO restaurant (
            managerId, name, profileImage, minPurchase, city, address, mapLoc, isDeleted
        ) VALUES (
            managerId, name, profileImage, minPurchase, cityName, address, mapLoc, 0
        );

        -- Retrieve the auto-incremented ID of the newly added restaurant
        SET restaurantId = LAST_INSERT_ID();
        SET isSuccess = TRUE;
    END IF;
END //

DELIMITER ;



drop procedure addopenday;
DELIMITER //

CREATE PROCEDURE AddOpenDay(
    IN restaurantId BIGINT,
    IN weekday ENUM('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'),
    OUT openDayId BIGINT,
    OUT isSuccess BOOLEAN
)
BEGIN
    -- Check if the restaurant exists and is not deleted
    IF NOT EXISTS (
        SELECT 1 FROM restaurant WHERE id = restaurantId AND isDeleted = 0
    ) THEN
        SET isSuccess = FALSE;
        SET openDayId = NULL;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Restaurant does not exist or is deleted.';
        
    END IF;

    -- Check if the open day already exists (not deleted)
    IF EXISTS (
        SELECT 1 FROM openDay WHERE restaurantId = openDay.restaurantId AND weekday = openDay.weekday AND isDeleted = 0
    ) THEN
        -- If the open day exists, return the existing open day ID
        SELECT id INTO openDayId
        FROM openDay
        WHERE restaurantId = restaurantId AND weekday = weekday AND isDeleted = 0;
    ELSE
        -- If the open day doesn't exist, create a new one
        INSERT INTO openDay (restaurantId, weekday, isDeleted)
        VALUES (restaurantId, weekday, 0);
        SET openDayId = LAST_INSERT_ID();
    END IF;

    SET isSuccess = TRUE;
END //

DELIMITER ;



DELIMITER //

CREATE PROCEDURE AddOpenHour(
    IN openHour TIME,
    IN closeHour TIME,
    IN openDayId BIGINT,
    OUT isSuccess BOOLEAN
)
BEGIN
    DECLARE openDayExists BOOLEAN;

    -- Check if the open day exists and is not deleted
    SELECT COUNT(*) > 0 INTO openDayExists
    FROM openDay
    WHERE id = openDayId AND isDeleted = 0;

    IF NOT openDayExists THEN
        SET isSuccess = FALSE;
        SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Open day does not exist or is marked as deleted.';
    END IF;

    -- Add the open hour
    INSERT INTO openHour (openHour, closeHour, openDayId, isDeleted)
    VALUES (openHour, closeHour, openDayId, 0);

    SET isSuccess = TRUE;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE DeleteRestaurant(
    IN restaurantId BIGINT,
    OUT isSuccess BOOLEAN
)
BEGIN
    DECLARE restaurantExists BOOLEAN;

    -- Check if the restaurant exists and is not already deleted
    SELECT COUNT(*) > 0 INTO restaurantExists
    FROM restaurant
    WHERE id = restaurantId AND isDeleted = 0;

    IF NOT restaurantExists THEN
        SET isSuccess = FALSE;
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Restaurant does not exist or is already deleted.';
    END IF;

    -- Mark the restaurant as deleted
    UPDATE restaurant SET isDeleted = 1 WHERE id = restaurantId;

    -- Mark associated items as deleted
    UPDATE item SET isDeleted = 1 WHERE restaurantId = restaurantId;

    -- Mark associated delivery fees as deleted
    UPDATE deliveryFee SET isDeleted = 1 WHERE restaurantId = restaurantId;

    -- Mark associated openDays as deleted
    UPDATE openDay SET isDeleted = 1 WHERE restaurantId = restaurantId;

    -- Mark associated openHours as deleted for all the openDays
    UPDATE openHour
    SET isDeleted = 1
    WHERE openDayId IN (SELECT id FROM openDay WHERE restaurantId = restaurantId);

    SET isSuccess = TRUE;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE GetRestaurantsPage(
    IN pageNumber INT,
    IN pageSize INT
)
BEGIN
    DECLARE offset INT;

    -- Calculate the offset for pagination
    SET offset = (pageNumber - 1) * pageSize;

    -- Fetch restaurants with pagination
    SELECT id, name, city, address, mapLoc, minPurchase
    FROM restaurant
    WHERE isDeleted = 0
    ORDER BY name ASC
    LIMIT pageSize OFFSET offset;
END //

DELIMITER ;
SET SQL_SAFE_UPDATES = 0;


delete from restaurant;

SET SQL_SAFE_UPDATES = 1;


DELIMITER //
CREATE PROCEDURE SoftDeleteCategory(
    IN categoryNameParam VARCHAR(100)
)
BEGIN
    UPDATE category 
    SET isDeleted = 1 
    WHERE categoryName = categoryNameParam;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE GetCategoriesPaged(
    IN pageNumber INT,
    IN pageSize INT
)
BEGIN
    DECLARE offsetValue INT;
    SET offsetValue = (pageNumber - 1) * pageSize;

    SELECT categoryName, isDeleted 
    FROM category 
    ORDER BY categoryName 
    LIMIT pageSize OFFSET offsetValue;
END //
DELIMITER ;
ALTER TABLE restaurant 
ADD CONSTRAINT fk_managerId
FOREIGN KEY (`managerId`) 
REFERENCES `users`(`username`) 
ON DELETE CASCADE;

DELIMITER //

CREATE PROCEDURE AddCategory(
    IN p_categoryName VARCHAR(100)
)
BEGIN
    INSERT INTO category (categoryName) VALUES (p_categoryName);
END //

DELIMITER ;
