package de.berlin.htw.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import de.berlin.htw.entity.dto.UserEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class UserRepository {


    /**
     * Inject a entity manager to access the persistence context
     */
    @PersistenceContext
    EntityManager entityManager;

    /**
     * Purpose: Find a user by id
     * @param id
     * @return
     */
    public UserEntity findUserById(final Integer id) {
        return entityManager.find(UserEntity.class, id);
    }

    /**
     * Purpose: Persist a user in the database
     * Difference to merge: persist will throw an exception if the entity already exists
     * @param user
     */
    @Transactional
    public void persistUser(final UserEntity user) {
        entityManager.persist(user);
    }

    /**
     * Purpose: Update a user in the database
     * @param user
     */
    @Transactional
    public void updateUser(final UserEntity user) {
        entityManager.merge(user);
    }
    
}