package org.hibernate.bugs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using the Java Persistence API.
 */
class JPAUnitTestCase {

    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("templatePU");
    }

    @AfterEach
    void destroy() {
        entityManagerFactory.close();
    }

    @Test
    void testFlushAfterOneToManyUpdate_WithGeneratedValues() {
        // Create an entity manager and begin a transaction
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        // Create a base pizza
        final Pizza pizza = new Pizza();

        // Make sure the pizza is persisted
        entityManager.persist(pizza);

        // Add a topping to the pizza
        final Topping topping = new Topping();
        topping.setName("Pepperoni");
        topping.setPizza(pizza);
        pizza.getToppings().add(topping);

        // Flush and clear to save topping, reset context and make queries against the DB
        entityManager.flush();
        entityManager.clear();

        // Let's try adding a new topping via mutation
        final Pizza retrievedPizza1 = entityManager.find(Pizza.class, pizza.getId());

        // Create a new topping
        final Topping newTopping1 = new Topping();
        newTopping1.setName("Cheese");
        newTopping1.setPizza(retrievedPizza1);

        // Let's mutate the existing list
        retrievedPizza1.getToppings().add(newTopping1);

        // Flush and clear to save topping, reset context and make queries against the DB
        // This does NOT fail on mutation, which is good
        entityManager.flush();
        entityManager.clear();

        // Now let's try adding a new topping via a new list
        final Pizza retrievedPizza2 = entityManager.find(Pizza.class, pizza.getId());

        // Create a new topping
        final Topping newTopping2 = new Topping();
        newTopping2.setName("Mushroom");
        newTopping2.setPizza(retrievedPizza2);

        // This time, instead of mutating the existing list, we're creating a new list
        retrievedPizza2.setToppings(List.of(retrievedPizza2.getToppings().get(0), newTopping2));

        // Here, we attempt to flush, but it fails, though internally, the topping is still persisted
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> entityManager.flush())
                .withMessage("Misuse of castNonNull: called with a null argument")
                .withStackTraceContaining("GeneratedValuesProcessor.processGeneratedValues");

        // Clear to make assertions against the DB
        entityManager.clear();

        // Get all pizzas
        final List<Pizza> results = entityManager
                .createQuery("select pizza from Pizza pizza", Pizza.class)
                .getResultList();

        // Illustrate that there is only one pizza with three toppings, even though the flush failed
        assertThat(results)
                .singleElement()
                .extracting(Pizza::getToppings)
                .asInstanceOf(InstanceOfAssertFactories.list(Topping.class))
                .hasSize(3)
                .extracting(Topping::getName)
                .containsExactlyInAnyOrder("Pepperoni", "Cheese", "Mushroom");

        // Close the entity manager and transaction
        entityManager.getTransaction().commit();
        entityManager.close();
    }

}
