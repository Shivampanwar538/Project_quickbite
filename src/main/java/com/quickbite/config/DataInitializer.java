package com.quickbite.config;

import com.quickbite.model.MenuItem;
import com.quickbite.model.Order;
import com.quickbite.model.User;
import com.quickbite.repository.MenuRepository;
import com.quickbite.repository.OrderRepository;
import com.quickbite.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;

    public DataInitializer(UserRepository userRepository,
                           MenuRepository menuRepository,
                           OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists to avoid duplicates
        if (userRepository.count() == 0) {
            initializeUsers();
        }

        if (menuRepository.count() == 0) {
            initializeMenuItems();
        }

        if (orderRepository.count() == 0) {
            initializeOrders();
        }
    }

    private void initializeUsers() {
        User student = new User();
        student.setUsername("user1");
        student.setPassword("user123");
        student.setRole("STUDENT");
        userRepository.save(student);

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setRole("ADMIN");
        userRepository.save(admin);

        System.out.println("✅ Users initialized in MongoDB");
    }

    private void initializeMenuItems() {
        MenuItem pizza = new MenuItem();
        pizza.setName("Margherita Pizza");
        pizza.setDescription("Classic cheese & tomato");
        pizza.setPrice(199.0);
        menuRepository.save(pizza);

        MenuItem burger = new MenuItem();
        burger.setName("Veggie Burger");
        burger.setDescription("Loaded with fresh veggies");
        burger.setPrice(149.0);
        menuRepository.save(burger);

        MenuItem coffee = new MenuItem();
        coffee.setName("Cold Coffee");
        coffee.setDescription("Chilled & refreshing");
        coffee.setPrice(99.0);
        menuRepository.save(coffee);

        MenuItem fries = new MenuItem();
        fries.setName("French Fries");
        fries.setDescription("Crispy golden fries");
        fries.setPrice(89.0);
        menuRepository.save(fries);

        System.out.println("✅ Menu items initialized in MongoDB");
    }

    private void initializeOrders() {
        // Get users and menu items for creating sample orders
        User user1 = userRepository.findByUsername("user1");
        MenuItem pizza = menuRepository.findAll().stream()
                .filter(item -> item.getName().equals("Margherita Pizza"))
                .findFirst().orElse(null);
        MenuItem burger = menuRepository.findAll().stream()
                .filter(item -> item.getName().equals("Veggie Burger"))
                .findFirst().orElse(null);
        MenuItem fries = menuRepository.findAll().stream()
                .filter(item -> item.getName().equals("French Fries"))
                .findFirst().orElse(null);

        if (user1 != null && pizza != null) {
            Order order1 = new Order();
            order1.setUser(user1);
            order1.setMenuItem(pizza);
            order1.setItemName(pizza.getName());
            order1.setQuantity(2);
            order1.setStatus("PENDING");
            orderRepository.save(order1);
        }

        if (user1 != null && burger != null) {
            Order order2 = new Order();
            order2.setUser(user1);
            order2.setMenuItem(burger);
            order2.setItemName(burger.getName());
            order2.setQuantity(1);
            order2.setStatus("COMPLETED");
            orderRepository.save(order2);
        }

        if (user1 != null && fries != null) {
            Order order3 = new Order();
            order3.setUser(user1);
            order3.setMenuItem(fries);
            order3.setItemName(fries.getName());
            order3.setQuantity(3);
            order3.setStatus("PENDING");
            orderRepository.save(order3);
        }

        System.out.println("✅ Sample orders initialized in MongoDB");
    }
}