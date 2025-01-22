package de.berlin.htw.entity.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.berlin.htw.boundary.dto.Order;
import de.berlin.htw.entity.dto.OrdersEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class OrdersRepository {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private UserRepository userRepository;

    /**
     * Purpose: Persist an order in the database
     * @param userId
     * @param order
     * @return
     */
    @Transactional
    public OrdersEntity checkout(String userId, Order order) {
        String itemJson = null;
        // Create a new OrdersEntity instance
        OrdersEntity orderEntity = new OrdersEntity();

        // Convert the items of the order to JSON using Jackson ObjectMapper
        // why? because we want to store the items as a String in the database and not as a List
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            itemJson = objectMapper.writeValueAsString(order.getItems());
            orderEntity.setItems(itemJson);
        } catch (JsonProcessingException e) {
            // Handle the exception (e.g., log it or throw a custom exception)
            e.printStackTrace();
        }

        // Set the items and total for the OrdersEntity
        orderEntity.setItems(itemJson);
        orderEntity.setTotal(order.getTotal());

        // Retrieve the user associated with the given userId using userRepo
        orderEntity.setUser(userRepository.findUserById(Integer.parseInt(userId)));

        // Persist the OrdersEntity in the database
        em.persist(orderEntity);

        // Return the persisted OrdersEntity
        return orderEntity;

    }

    /**
     * Purpose: Find all orders for a user directly from the database with a JPQL query
     * @param userId
     * @return
     */
    public List<OrdersEntity> findOrdersByUserId(String userId) {
        return em.createQuery("SELECT o FROM OrdersEntity o WHERE o.user.id = :id", OrdersEntity.class)
                .setParameter("id", userId)
                .getResultList();
    }

}

