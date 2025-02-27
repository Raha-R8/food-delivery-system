DELIMITER //

CREATE PROCEDURE GetOpenDaysForRestaurant(
    IN restaurantId BIGINT,
    OUT openDayIds TEXT
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE tempId BIGINT;
    DECLARE result TEXT DEFAULT '';
    DECLARE cur CURSOR FOR 
        SELECT id FROM openDay WHERE openDay.restaurantId = restaurantId AND isDeleted = 0;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO tempId;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Concatenate IDs as a comma-separated string
        IF result = '' THEN
            SET result = CAST(tempId AS CHAR);
        ELSE
            SET result = CONCAT(result, ',', CAST(tempId AS CHAR));
        END IF;
    END LOOP;

    CLOSE cur;

    SET openDayIds = result;
END //

DELIMITER ;
drop procedure GetOpenDaysForRestaurant;



DELIMITER //

CREATE PROCEDURE GetOpenDayName(
    IN openDayId BIGINT,
    OUT openDayName ENUM('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday')
)
BEGIN
    -- Retrieve the weekday for the given openDayId
    SELECT weekday INTO openDayName
    FROM openDay
    WHERE id = openDayId AND isDeleted = 0;

    -- If no open day is found, set openDayName to NULL
    IF openDayName IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Open Day not found or is deleted.';
    END IF;
END //

DELIMITER ;


drop procedure addItem;
DELIMITER //

CREATE PROCEDURE AddItem(
	IN restaurantId BIGINT,
    IN title VARCHAR(100),
    IN image MEDIUMBLOB,
    IN price FLOAT,
    IN ingredient VARCHAR(100),
    OUT itemId BIGINT,
    OUT isSuccess BOOLEAN
)
BEGIN
    DECLARE restaurantExists BOOLEAN;
    -- Insert new item into the item table
    INSERT INTO item (restaurantId, title, image, price, ingredient, isDeleted)
    VALUES (restaurantId, title, image, price, ingredient, 0);
    
    -- Retrieve the last inserted item ID
    SET itemId = LAST_INSERT_ID();
    SET isSuccess = TRUE;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE DeleteItemById(
    IN itemId BIGINT,
    OUT isSuccess BOOLEAN
)
BEGIN
    DECLARE itemExists BOOLEAN;

    -- Check if the item exists and is not already deleted
    SELECT COUNT(*) > 0 INTO itemExists FROM item WHERE id = itemId AND isDeleted = 0;

    IF itemExists THEN
        -- Soft delete the item
        UPDATE item SET isDeleted = 1 WHERE id = itemId;
        SET isSuccess = TRUE;
    ELSE
        SET isSuccess = FALSE;
    END IF;
END //

DELIMITER ;



DELIMITER //

CREATE PROCEDURE UpdateItemPrice(
    IN itemId BIGINT,
    IN newPrice FLOAT,
    OUT isSuccess BOOLEAN
)
BEGIN
    DECLARE itemExists BOOLEAN;

    -- Check if the item exists and is not deleted
    SELECT COUNT(*) > 0 INTO itemExists FROM item WHERE id = itemId AND isDeleted = 0;

    IF itemExists THEN
        -- Update the item's price
        UPDATE item SET price = newPrice WHERE id = itemId;
        SET isSuccess = TRUE;
    ELSE
        SET isSuccess = FALSE;
    END IF;
END //

DELIMITER ;



DELIMITER //

CREATE PROCEDURE AddOrUpdateDeliveryFee(
    IN restaurantId BIGINT,
    IN maxDistance INT,
    IN cost FLOAT,
    OUT isSuccess BOOLEAN
)
BEGIN
    DECLARE feeExists BOOLEAN;

    -- Check if a record with the given restaurantId and maxDistance exists and is not deleted
    SELECT COUNT(*) > 0 INTO feeExists 
    FROM deliveryFee 
    WHERE deliveryFee.restaurantId = restaurantId AND deliveryFee.maxDistance = maxDistance AND isDeleted = 0;

    IF feeExists THEN
        -- If it exists, update the cost
        UPDATE deliveryFee 
        SET cost = cost 
        WHERE deliveryFee.restaurantId = restaurantId AND deliveryFee.maxDistance = maxDistance;
        SET isSuccess = TRUE;
    ELSE
        -- If it doesn't exist, insert a new record
        INSERT INTO deliveryFee (restaurantId, maxDistance, cost, isDeleted) 
        VALUES (restaurantId, maxDistance, cost, 0);
        SET isSuccess = TRUE;
    END IF;
END //

DELIMITER ;



DELIMITER //

CREATE PROCEDURE GetPaginatedDeliveryFees(
    IN restaurantId BIGINT,
    IN pageSize INT,
    IN offset INT
)
BEGIN
    SELECT id, maxDistance, cost
    FROM deliveryFee
    WHERE deliveryFee.restaurantId = restaurantId AND isDeleted = 0
    ORDER BY maxDistance ASC
    LIMIT pageSize OFFSET offset;
END //

DELIMITER ;


DELIMITER //

CREATE PROCEDURE DeleteDeliveryFee(
    IN feeId BIGINT
)
BEGIN
    -- Check if the delivery fee exists
    DECLARE feeExists BOOLEAN;

    SELECT COUNT(*) > 0 INTO feeExists
    FROM deliveryFee
    WHERE id = feeId AND isDeleted = 0;

    IF feeExists THEN
        -- Logically delete the delivery fee
        UPDATE deliveryFee
        SET isDeleted = 1
        WHERE id = feeId;
    ELSE
        -- Raise an error if the fee does not exist
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Delivery fee does not exist or is already deleted.';
    END IF;
END //

DELIMITER ;

drop procedure GetPaginatedOrdersByRestaurantPath;
DELIMITER //

CREATE PROCEDURE GetPaginatedOrdersByRestaurantPath(
    IN restaurantId BIGINT,
    IN pageSize INT,
    IN offset INT
)
BEGIN
    -- Temporary table to store the order IDs
    CREATE TEMPORARY TABLE tempOrderIds (orderId BIGINT);

    -- Insert order IDs for items related to the restaurant into the temp table
    INSERT INTO tempOrderIds (orderId)
    SELECT oi.orderId
    FROM orderItem oi
    JOIN item i ON oi.itemId = i.id
    WHERE i.restaurantId = restaurantId;

    -- Fetch the user orders that are associated with these orderIds, paginated
    SELECT o.id, o.addressId, o.orderTime, o.isPaid, o.orderStatus, o.isDeleted
    FROM userOrder o
    JOIN tempOrderIds toi ON o.id = toi.orderId
    WHERE o.isDeleted = 0
    ORDER BY o.orderTime DESC
    LIMIT pageSize OFFSET offset;

    -- Drop the temporary table
    DROP TEMPORARY TABLE IF EXISTS tempOrderIds;
END //

DELIMITER ;

drop procedure GetPaginatedPendingOrdersByRestaurantPath;
DELIMITER //

CREATE PROCEDURE GetPaginatedPendingOrdersByRestaurantPath(
    IN restaurantId BIGINT,
    IN pageSize INT,
    IN offset INT
)
BEGIN
    -- Temporary table to store the order IDs
    CREATE TEMPORARY TABLE tempOrderIds (orderId BIGINT);

    -- Insert order IDs for items related to the restaurant into the temp table
    INSERT INTO tempOrderIds (orderId)
    SELECT oi.orderId
    FROM orderItem oi
    JOIN item i ON oi.itemId = i.id
    WHERE i.restaurantId = restaurantId;

    -- Fetch the user orders with 'Pending' status associated with these orderIds, paginated
    SELECT o.id, o.addressId, o.orderTime, o.isPaid, o.orderStatus, o.isDeleted
    FROM userOrder o
    JOIN tempOrderIds toi ON o.id = toi.orderId
    WHERE o.isDeleted = 0
	AND o.orderStatus = 'Pending'  -- Only orders with status 'Pending'
    ORDER BY o.orderTime DESC
    LIMIT pageSize OFFSET offset;

    -- Drop the temporary table
    DROP TEMPORARY TABLE IF EXISTS tempOrderIds;
END //

DELIMITER ;


