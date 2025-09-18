
INSERT INTO users (username, password, role) VALUES ('user1', 'user123', 'STUDENT');
INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN');
-- Menu Items
INSERT INTO menu_item (name, description, price) VALUES ('Margherita Pizza', 'Classic cheese & tomato', 199.0);
INSERT INTO menu_item (name, description, price) VALUES ('Veggie Burger', 'Loaded with fresh veggies', 149.0);
INSERT INTO menu_item (name, description, price) VALUES ('Cold Coffee', 'Chilled & refreshing', 99.0);
INSERT INTO menu_item (name, description, price) VALUES ('French Fries', 'Crispy golden fries', 89.0);

-- Sample Orders (linking to menu items logically)
INSERT INTO orders (item_name, quantity, status, menu_item_id, user_id) VALUES ('Margherita Pizza', 2, 'Pending',1,1);
INSERT INTO orders (item_name, quantity, status, menu_item_id, user_id) VALUES ('Veggie Burger', 1, 'Completed',2,1);
INSERT INTO orders (item_name, quantity, status, menu_item_id, user_id) VALUES ('French Fries', 3, 'Pending',4,1);


