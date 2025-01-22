package de.berlin.htw.control;
import de.berlin.htw.boundary.dto.Order;
import de.berlin.htw.entity.dao.OrdersRepository;
import de.berlin.htw.entity.dto.OrdersEntity;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import de.berlin.htw.boundary.dto.Basket;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.redis.datasource.list.ListCommands;
import de.berlin.htw.boundary.dto.Item;
import de.berlin.htw.entity.dao.UserRepository;
import de.berlin.htw.entity.dto.UserEntity;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import java.util.*;



/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 * */
@Dependent
public class BasketController {

    @Inject
    protected RedisDataSource redisDS;

    @Inject
    Logger logger;


    @Inject
    UserRepository userRepo;

    @Inject
    OrdersRepository orderRepo;


    /**
     * Purpose: This interface provides commands for working with Redis values, specifically those stored as strings.
     * The generic types <String, Integer> indicate that keys are strings, and values are integers.
     */
//    protected ValueCommands<String, Integer> countCommands;
//    protected ListCommands<String, String> stringListCommands;

    protected ValueCommands<String, Basket> basketBasketCommands;

    /**
     * Initializes the class by retrieving necessary data from Redis.
     */
    @PostConstruct
    protected void init() {
//        countCommands = redisDS.value(Integer.class);

//        stringListCommands = redisDS.list(String.class);

        basketBasketCommands = redisDS.value(Basket.class);
    }

    /**
     * Saves the basket into Redis.
     *
     * @param userId - the user id that the basket belongs to
     * @param basket - the basket to be saved
     * @return the saved basket
     */
    public Basket saveBasket(String userId, Basket basket) {
        // Set the basket in Redis with a TTL of 120 seconds
        basketBasketCommands.setex(userId, 120, basket);
        logger.info("Saved basket for user: " + userId);

        return basket;
    }

    /**
     * Retrieves the basket for the given user ID from Redis.
     *
     * @param id the user ID that the basket belongs to
     * @return the Basket object for the user
     * @throws WebApplicationException If the user doesn't exist.
     */
    public Basket getBasket(String id) {
        // Find the user from the user repository
        UserEntity user = userRepo.findUserById(Integer.parseInt(id));
        if (user == null) {
            logger.info("No user found with id " + id);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        logger.info("Retrieved user from database: " + user.getName() + " " + user.getId());

        // Get the basket from Redis
        Basket basket = basketBasketCommands.get(id);

        // If the basket doesn't exist, create a new one
        if (basket == null) {
            logger.info("No Basket found.Creating new basket for user " + id + ".....");;
            basket = createBasketForUser(id);
        }
        // Calculate the remaining balance in the basket
        basket.setRemainingBalance(user.getBalance() - basket.getTotal());
        logger.info("Calculated remaining balance for user: " + basket.getRemainingBalance());


        return basket;
    }

    /**
     * Adds an item to the basket.
     *
     * @param userId - the user id that the basket belongs to
     * @param item - the item to be added
     * @return the updated basket
     * @throws WebApplicationException If the item already exists in the basket.
     * @throws WebApplicationException If  the item is too expensive.
     */
    public Basket addItem(String userId, Item item) {
        Basket basket = basketBasketCommands.get(userId);  // Get the basket from Redis

        // If the basket doesn't exist, create a new one
        if (basket == null) {
            logger.info("No Basket found. Creating new basket for user " + userId + ".....");
            basket = createBasketForUser(userId);  // Create a new basket if it doesn't exist
        }
        // If the item already exists, throw an exception
        else if (isItemInBasket(userId, item.getProductId())) {
            logger.info("Item already exists in basket - nothing to add");
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
        else {
            logger.info("Item does not exist in basket - adding item " + item.getProductId() + ".....");
            logger.info("Item total price: " + item.getPrice() * item.getCount());
        }

        // Check if there's enough balance to add the item
        if (basket.getRemainingBalance() < item.getPrice() * item.getCount()) {
            logger.info("Not enough Balance");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        basket.getItems().add(item);  // Add the item to the basket
        logger.info("Item added to basket: " + item.getProductName() + " x " + item.getCount());
        logger.info("Item price: " + item.getPrice());

        // Update the total price of the basket
        basket.setTotal(basket.getTotal() + (item.getPrice() * item.getCount()));
        logger.info("Total price of purchase in basket: " + basket.getTotal());

        // Update the remaining balance of the basket
        basket.setRemainingBalance(basket.getRemainingBalance() - (item.getPrice() * item.getCount()));
        logger.info("Remaining balance of basket: " + basket.getRemainingBalance());

        saveBasket(userId, basket);  // Save the basket in Redis with a TTL of 120 seconds
        logger.info("Saved basket for user: " + userId + " with balance: " + basket.getRemainingBalance() + " and total: " + basket.getTotal() + ".....");

        return basket;
    }


    /**
     * Removes an item from the basket.
     *
     * @param userId    - the user id that the basket belongs to
     * @param productId - the product id of the item to be removed
     * @return the updated basket
     * @throws WebApplicationException If the item doesn't exist in the basket.
     * @throws WebApplicationException If the basket doesn't exist.
     */
    public Basket removeItem(String userId, String productId) {
        // Get the basket from Redis
        Basket basket = basketBasketCommands.get(userId);
        // If the basket doesn't exist, throw an exception
        if (basket == null) {
            logger.info("No Basket found. Nothing to remove");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        // If the item doesn't exist, throw an exception
        if (!isItemInBasket(userId, productId)) {
            logger.info("Item does not exist in basket - nothing to remove");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else {
            logger.info("Item exists in basket - removing item " + productId + ".....");
        }

        // Find the item to remove from the basket
        Item itemToRemove = findItemInBasket(userId, productId);

        // Remove the item from the basket
        basket.getItems().remove(itemToRemove);
        logger.info("Item removed from basket ");

        // Update the total price of the basket
        basket.setTotal(basket.getTotal() - (itemToRemove.getPrice() * itemToRemove.getCount()));
        logger.info("Total price of purchase in basket: " + basket.getTotal());

        // Update the remaining balance of the basket
        basket.setRemainingBalance(basket.getRemainingBalance() + (itemToRemove.getPrice() * itemToRemove.getCount()));
        logger.info("Remaining balance of basket: " + basket.getRemainingBalance());

        // Save the basket in Redis with a TTL of 120 seconds
        saveBasket(userId, basket);

        return basket;
    }

    /**
     * Change the number of an item in the basket.
     *
     * @param userId    The ID of the user.
     * @param productId The ID of the product.
     * @param item      The number of that product in the basket
     * @return The updated basket.
     * @throws WebApplicationException If the item doesn't exist in the basket.
     * @throws WebApplicationException If the basket doesn't exist.
     */
    public Basket patchBasket(String userId, String productId, Item item) throws WebApplicationException {
        // Get the basket from Redis
        Basket basket = basketBasketCommands.get(userId);
        logger.info("Retrieved basket from Redis" + basket.getRemainingBalance());

        // If the basket doesn't exist, throw an exception
        if (basket == null) {
            logger.info("No Basket found. Nothing to patch");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // If the item doesn't exist, throw an exception
        if (!isItemInBasket(userId, productId)) {
            logger.info("Item does not exist in basket - nothing to patch");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else {
            logger.info("Item exists in basket - patching item " + productId + ".....");
        }

        // Find the item to patch from the basket
        Item itemToPatch = findItemInBasket(userId, productId);
        itemToPatch.setCount(itemToPatch.getCount() + item.getCount());
        logger.info("Item patched successfully");

        // Update the total price of the basket
        basket.setTotal(basket.getTotal() + (item.getPrice() * item.getCount()));
        logger.info("Total price of purchase in basket: " + basket.getTotal());

        // Update the remaining balance of the basket
        basket.setRemainingBalance(basket.getRemainingBalance() - (item.getPrice() * item.getCount()));
        logger.info("Remaining balance of basket: " + basket.getRemainingBalance());

        // Save the basket in Redis with a TTL of 120 seconds
        saveBasket(userId, basket);
        return basket;
    }

    /**
     * Clears the basket for the given user.
     *
     * @param userId The ID of the user.
     * @return The updated basket after clearing.
     * @throws WebApplicationException If the basket doesn't exist.
     */
    public Basket clearBasket(String userId) {
        Basket basket = basketBasketCommands.get(userId); // Get the basket for the user
        if (basket == null) {
            logger.info("No Basket found. Nothing to clear");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // reverting the balance to the original value
        UserEntity user = userRepo.findUserById(Integer.parseInt(userId)); // Find the user by ID
        basket.setRemainingBalance(user.getBalance()); // Set the remaining balance of the basket to the user's balance
        basket.getItems().clear(); // Clear the items in the basket
        saveBasket(userId, basket); // Save the updated basket
        return basket; // Return the cleared basket
    }

    /**
     * Checks if the specified item is present in the user's basket.
     *
     * @param userId    the ID of the user
     * @param productId the ID of the product
     * @return true if the item is present in the basket, false otherwise
     * @throws WebApplicationException If the basket doesn't exist.
     */
    public boolean isItemInBasket(String userId, String productId) {
        Basket basket = basketBasketCommands.get(userId); // Get the basket for the user
        if (basket == null) {
            logger.info("No Basket found. Nothing to check");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return basket.getItems().stream().anyMatch(i -> i.getProductId().equals(productId)); // Check if the item exists in the basket
    }

    /**
     * Finds an item in the basket with the given product ID.
     *
     * @param userId    the ID of the user
     * @param productId the ID of the product to find
     * @return the item if found, otherwise null
     */
    public Item findItemInBasket(String userId, String productId) {
        Basket basket = basketBasketCommands.get(userId); // Get the basket for the user

        // Find the item in the basket
        return basket.getItems().stream() // Stream the items in the basket
                .filter(i -> i.getProductId().equals(productId)) // Filter the items by the product ID
                .findFirst() // Find the first item
                .orElse(null); // Return null if no item is found
    }


    /**
     * Creates a new basket for the specified user.
     *
     * @param userId the ID of the user
     * @return the newly created basket
     */
    public Basket createBasketForUser(String userId) {
        Basket basket = new Basket();  // Create a new basket
        UserEntity user = userRepo.findUserById(Integer.parseInt(userId)); // Find the user by ID
        basket.setRemainingBalance(user.getBalance()); // Set the remaining balance of the basket to the user's balance
        basket.setItems(new ArrayList<>()); // Create a new list of items
        basket.setTotal(0.0f); // Set the total price of the basket to 0
        logger.info("Created new basket for user " + userId ); // Log the creation of the basket
        logger.info("Remaining balance of basket: " + basket.getRemainingBalance()); // Log the remaining balance of the basket
        return basket;  // Return the new basket
    }


    /**
     * Checks out the basket for the specified user.
     * @param userId
     * @return the order
     */
    public OrdersEntity checkoutBasket(String userId) {
        // convert String to Integer for userId
        Integer userIdInt = Integer.parseInt(userId);
        // get the UserEntity from the database
        UserEntity user = userRepo.findUserById(userIdInt);
        // get the basket from Redis
        Basket basket = basketBasketCommands.get(userId);
        // if the basket does not exist, throw an exception
        if (basket == null) {
            logger.info("No Basket found. Nothing to checkout");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        else if (basket.getTotal() > user.getBalance()) {
            logger.info("Basket total price: " + basket.getTotal() + " > " + "User balance: " + user.getBalance());
            logger.info("Not enough Balance");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        else if (basket.getItems().size() < 1) {
            logger.info("Basket is empty or doesn't have enough items");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        else {
            logger.info("Basket exists - checking out basket " + userId + ".....");



        }

        // setting up order and put items in it
        Order order = new Order();
        order.setItems(basket.getItems());
        order.setTotal(basket.getTotal());

        logger.info("Order items: " + order.getItems());
        logger.info("Order total price: " + order.getTotal());

        // create a new OrdersEntity instance to save the order in the database
        OrdersEntity orderEntity = orderRepo.checkout(userId, order);
        logger.info("Order checked out successfully");
        // update the balance of the user
        user.setBalance(user.getBalance() - basket.getTotal());
        logger.info("User balance: " + user.getBalance());
        // save the updated user in the database
        userRepo.updateUser(user);
        logger.info("User balance updated successfully");
        // clear the basket
        clearBasket(userId);
        logger.info("Basket cleared successfully");
        // return the order
        return orderEntity;

    }

}


