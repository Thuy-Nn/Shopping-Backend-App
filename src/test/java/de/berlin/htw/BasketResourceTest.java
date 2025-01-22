package de.berlin.htw;

import de.berlin.htw.boundary.dto.Item;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;

@QuarkusTest
class BasketResourceTest {

    @Inject
    protected RedisDataSource redisDS;

    // to create Item for testing
    public Item createTestingItem(String id, String name, Float price, Integer count) {

        Item item = new Item();
        item.setProductId(id);
        item.setProductName(name);
        item.setPrice(price);
        item.setCount(count);
        return item;
    }



    /**
     * Test case for the 'getBasket' method.
     */
    @Test
    void testGetBasket() {
        given()
                .log().all()
                .when().header("X-User-Id", "1")
                .get("/basket")
                .then()
                .log().all()
                .statusCode(200);
    }

    /**
     * Test case for the 'addItem' method.
     *
     * @Test
     * void testAddItem()
     *
     * expected status 201 CREATED
     */
    @Test
    void testAddItem() {

        // clear redis before testing
        redisDS.flushall();
        Item testItemUser1 = createTestingItem("1-2-3-4-5-6", "TestItemUser1", 10.0f, 1);

        // add item to basket
        given()
                .log().all()
                .when().header("X-User-Id", "1")
                .contentType(ContentType.JSON)
                .body(testItemUser1)
                .post("/basket/" + testItemUser1.getProductId())
                .then()
                .log().all()
                .statusCode(201);
    }

    /**
     * Test case for the 'addItem' method.
     *
     * @Test
     * void testAddItem()
     *
     * expected status 409 CONFLICT
     * should be run after testAddItem() so the item is already in the basket
     */

    @Test
    void testAddItemConflict() {

        // clear redis before testing
        redisDS.flushall();
        Item testItemUser1 = createTestingItem("1-2-3-4-5-6", "TestItemUser1", 10.0f, 1);

        // add item to basket
        given()
                .log().all()
                .when().header("X-User-Id", "1")
                .contentType(ContentType.JSON)
                .body(testItemUser1)
                .post("/basket/" + testItemUser1.getProductId())
                .then()
                .log().all().statusCode(201);

        // add item to basket again with same productId -> should return 409 CONFLICT
        given()
                .log().all()
                .when().header("X-User-Id", "1")
                .contentType(ContentType.JSON)
                .body(testItemUser1)
                .post("/basket/" + testItemUser1.getProductId())
                .then()
                .log().all()
                .statusCode(409);


    }

    /**
     * Test case to verify the behavior of the addItem method when a bad request is made.
     *
     * This test case checks the behavior of the addItem method when it receives a bad request.
     * It creates several testing items with invalid data and sends requests to add them to the basket.
     * The expected behavior is that the method returns a status code of 400 for each request.
     * This test case covers scenarios where the product name is too long, the price is too high, the count is too low,
     * the ID is invalid, or the item is too expensive.
     *
     */

    @Test
    void testAddItemBadRequests() {

        String invalidProductName = RandomStringUtils.randomAlphanumeric(260);
        Item nameTooLong = createTestingItem("1-2-3-4-5-6", invalidProductName, 15.0f, 2);
        Item tooHighPriceItem = createTestingItem("2-2-3-4-5-6", "test", 100.1f, 1);
        Item tooLowCountItem = createTestingItem("3-2-3-4-5-6", "test", 10.0f, 0);
        Item invalidId = createTestingItem("1-2-3-4-5-7-8", "TestItemUser4", 15.0f, 2);
        Item tooExpensiveItem = createTestingItem("4-2-3-4-5-6", "test", 100.0f, 1);

        // add items to basket with invalid request
        given()
                .log().all()
                .when().header("X-User-Id", "1")
                .contentType(ContentType.JSON)
                .body(nameTooLong)
                .post("/basket/" + nameTooLong.getProductId())
                .then()
                .log().all()
                .statusCode(400);
        given()
                .when().header("X-User-Id", "1")
                .contentType(ContentType.JSON)
                .body(tooHighPriceItem)
                .post("/basket/" + tooHighPriceItem.getProductId())
                .then()
                .log().all()
                .statusCode(400);
        given()
                .when().header("X-User-Id", "1")
                .contentType(ContentType.JSON)
                .body(tooLowCountItem)
                .post("/basket/" + tooLowCountItem.getProductId())
                .then()
                .log().all()
                .statusCode(400);
        given()
                .when().header("X-User-Id", "1")
                .contentType(ContentType.JSON)
                .body(invalidId)
                .post("/basket/" + invalidId.getProductId())
                .then()
                .log().all()
                .statusCode(400);
        given()
                .when().header("X-User-Id", "2")
                .contentType(ContentType.JSON)
                .body(tooExpensiveItem)
                .post("/basket/" + tooExpensiveItem.getProductId())
                .then()
                .log().all()
                .statusCode(400);
    }


    /**
     * Test case for the 'clearBasket' method.
     *
     * @Test
     * void clearBasket()
     *
     * expected status 200 OK
     */

    @Test
    void clearBasketTest() {

        // clear redis before testing
        redisDS.flushall();

        // add item to basket
        Item testItemUser1 = createTestingItem("1-2-3-4-5-6", "TestItemUser1", 10.0f, 1);
        given()
                .log().all()
                .when().header("X-User-Id", "1")
                .contentType(ContentType.JSON)
                .body(testItemUser1)
                .post("/basket/" + testItemUser1.getProductId())
                .then()
                .log().all()
                .statusCode(201);

        // clear the basket
        given()
                .log().all()
                .when().header("X-User-Id", "1")
                .delete("/basket")
                .then()
                .log().all()
                .statusCode(204);
    }


    /**
     * Test case for removeItem method.
     */
    @Test
    void removeItemTest() {

        // clear redis before testing
        redisDS.flushall();

        Item testItemUser1 = createTestingItem("1-2-3-4-5-6", "TestItemUser1", 10.0f, 1);

        // add item to basket
        given()
                .log().all()
                .when().header("X-User-Id", "1")
                .contentType(ContentType.JSON)
                .body(testItemUser1)
                .post("/basket/" + testItemUser1.getProductId())
                .then()
                .log().all()
                .statusCode(201);

        // remove item from basket
        given()
                .log().all()
                .when().header("X-User-Id", "1")
                .delete("/basket/" + testItemUser1.getProductId())
                .then()
                .log().all()
                .statusCode(200);
    }

    /**
     * Test case for removeItem method. Invalid requests
     * shoould return 404 NOT FOUND
     */
    @Test
    void removeItemBadRequestsTest() {
        // clear redis before testing
        redisDS.flushall();
        // removing an item when a basket doesn't exist yet
        given()
                .log().all()
                .when().header("X-User-Id", "1")
                .delete("/basket/" + "1-2-3-4-5-6")
                .then()
                .log().all()
                .statusCode(404);
        // adding an item so a basket gets created
        Item testItemUser1 = createTestingItem("2-2-3-4-5-6", "TestItemUser1", 10.0f, 1);
        given()
                .log().all()
                .when().header("X-User-Id", "1")
                .contentType(ContentType.JSON)
                .body(testItemUser1)
                .post("/basket/" + testItemUser1.getProductId())
                .then()
                .log().all()
                .statusCode(201);

        // removing an item with an invalid productId
        given()
                .log().all()
                .when().header("X-User-Id", "1")
                .delete("/basket/" + "1-2-3-4-5-7")
                .then()
                .log().all()
                .statusCode(404);

    }





    @Test
    void testCheckoutSucess() {
        // clear redis before testing
        redisDS.flushall();
        Item testItemUser1 = createTestingItem("1-2-3-4-5-6", "TestItemUser1", 10.0f, 1);
        Item testItemUser2 = createTestingItem("2-2-3-4-5-6", "TestItemUser2", 15.0f, 1);
        Item testItemUser3 = createTestingItem("3-2-3-4-5-6", "TestItemUser3", 20.0f, 1);

        // add items to basket
        given()
                .log().all()
                .when().header("X-User-Id", "3")
                .contentType(ContentType.JSON)
                .body(testItemUser1)
                .post("/basket/" + testItemUser1.getProductId())
                .then()
                .log().all()
                .statusCode(201);
        given()
                .log().all()
                .when().header("X-User-Id", "3")
                .contentType(ContentType.JSON)
                .body(testItemUser2)
                .post("/basket/" + testItemUser2.getProductId())
                .then()
                .log().all()
                .statusCode(201);
        given()
                .log().all()
                .when().header("X-User-Id", "3")
                .contentType(ContentType.JSON)
                .body(testItemUser3)
                .post("/basket/" + testItemUser3.getProductId())
                .then()
                .log().all()
                .statusCode(201);

        // checkout
        given()
                .log().all()
                .when().header("X-User-Id", "3")
                .contentType(ContentType.JSON)
                .post("/basket/")
                .then()
                .log().all()
                .statusCode(201);
    }


    @Test
    void testCheckoutEmptyBasket() {
        // clear redis before testing
        redisDS.flushall();

        // add items to basket
        Item testItemUser1 = createTestingItem("1-2-3-4-5-6", "TestItemUser1", 10.0f, 1);

        given()
                .log().all()
                .when().header("X-User-Id", "1")
                .contentType(ContentType.JSON)
                .body(testItemUser1)
                .post("/basket/" + testItemUser1.getProductId())
                .then()
                .log().all()
                .statusCode(201);

        // clear the basket
        given()
                .log().all()
                .when().header("X-User-Id", "1")
                .delete("/basket")
                .then()
                .log().all()
                .statusCode(204);

        // checkout
        given()
                .log().all()
                .when().header("X-User-Id", "1")
                .contentType(ContentType.JSON)
                .post("/basket/")
                .then()
                .log().all()
                .statusCode(400);
    }

}