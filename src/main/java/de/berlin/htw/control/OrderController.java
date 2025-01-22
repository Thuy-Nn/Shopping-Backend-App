package de.berlin.htw.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.berlin.htw.boundary.dto.Item;
import de.berlin.htw.boundary.dto.Order;
import de.berlin.htw.entity.dao.OrdersRepository;
import de.berlin.htw.entity.dao.UserRepository;
import de.berlin.htw.entity.dto.OrdersEntity;
import de.berlin.htw.entity.dto.UserEntity;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotSupportedException;

import de.berlin.htw.boundary.dto.Orders;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Dependent
public class OrderController {

    @Inject
    OrdersRepository orderRepo;

    @Inject
    UserRepository userRepo;

    @Inject
    Logger logger;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Purpose: Get all completed orders for a user
     * @param userId
     * @return
     */
    public Orders getCompletedOrders(String userId) {
        // convert user id to int
        int id = Integer.parseInt(userId);

        // find user by id
        UserEntity user = userRepo.findUserById(id);
        logger.info("User with id " + userId + " is calling getCompletedOrders");

        // check if user exists
        if (user == null) {
            throw new NotSupportedException("User with id " + userId + " does not exist.");
        }

        // find orders as entity by user id
        List<OrdersEntity> ordersAsEntity = orderRepo.findOrdersByUserId(userId);
        logger.info("Found " + ordersAsEntity.size() + " orders for user with id " + userId);
        List<Order> orders = new ArrayList<>();
        // convert orders from entity to dto
        for (OrdersEntity oneEntity : ordersAsEntity) {
            Order order = new Order();
            order.setTotal(oneEntity.getTotal());

            // convert items from json to dto
            try {
                // convert json to array of items
                Item[] itemArray = objectMapper.readValue(oneEntity.getItems(), Item[].class);
                order.setItems(Arrays.asList(itemArray));
            } catch (IOException e) {
                // Handle the exception appropriately (e.g., log or throw a custom exception)
                e.printStackTrace();
            }


            // Add the order to the list
            orders.add(order);
        }


        // create orders dto
        Orders ordersDto = new Orders();
        ordersDto.setOrders(orders);
        logger.info("Returning " + ordersDto.getOrders().size() + " orders for user with id " + userId);

        ordersDto.setBalance(user.getBalance());
        logger.info("Returning balance " + ordersDto.getBalance() + " for user with id " + userId);
        return ordersDto;
    }
}

