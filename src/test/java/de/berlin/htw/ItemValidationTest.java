package de.berlin.htw;

import de.berlin.htw.boundary.dto.Item;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

/**
 * Test the validation of the Item class.
 */
@QuarkusTest
public class ItemValidationTest {

    // to create Item for testing
    public Item createTestingItem(String id, String name, Float price, Integer count) {
        Item item = new Item();
        item.setProductId(id);
        item.setProductName(name);
        item.setPrice(price);
        item.setCount(count);
        return item;
    }

    @Test
    public void testItemNameTooLongValidation() {
        String invalidProductName = RandomStringUtils.randomAlphanumeric(260);

        Item tooLongNameItem = createTestingItem("1-2-3-4-5-6", invalidProductName, 10.0f, 1);

        given()
                .log().all()
                .when().header("X-User-Id", "2")
                .contentType("application/json")
                .body(tooLongNameItem)
                .when().post("/basket/{productId}", tooLongNameItem.getProductId())
                .then()
                .statusCode(400);
    }

//    @Test
//    public void testItemNameTooShortValidation() {
//        String invalidProductName = RandomStringUtils.randomAlphanumeric(0);
//
//        Item tooShortNameItem = createTestingItem("1-2-3-4-5-6", invalidProductName, 10.0f, 1);
//
//        given()
//                .log().all()
//                .when().header("X-User-Id", "2")
//                .contentType("application/json")
//                .body(tooShortNameItem)
//                .when().post("/basket/{productId}", tooShortNameItem.getProductId())
//                .then()
//                .statusCode(400);
//    }

    @Test
    public void testItemNameNullValidation() {
        String invalidProductName = null;

        Item nullNameItem = createTestingItem("1-2-3-4-5-6", invalidProductName, 10.0f, 1);

        given()
                .log().all()
                .when().header("X-User-Id", "2")
                .contentType("application/json")
                .body(nullNameItem)
                .when().post("/basket/{productId}", nullNameItem.getProductId())
                .then()
                .statusCode(400);
    }

    @Test
    public void testItemProductIdTooLongValidation() {
        String invaildId = "1-2-3-4-5-6-7-8-9-10-11-12-13-14-15";

        Item tooLongIdItem = createTestingItem(invaildId, "test", 10.0f, 1);

        given()
                .log().all()
                .when().header("X-User-Id", "2")
                .contentType("application/json")
                .body(tooLongIdItem)
                .when().post("/basket/{productId}", tooLongIdItem.getProductId())
                .then()
                .statusCode(400);
    }

    @Test
    public void testItemProductIdTooShortValidation() {
        String invaildId = "1-2-3-4-5";

        Item tooShortIdItem = createTestingItem(invaildId, "test", 10.0f, 1);

        given()
                .log().all()
                .when().header("X-User-Id", "2")
                .contentType("application/json")
                .body(tooShortIdItem)
                .when().post("/basket/{productId}", tooShortIdItem.getProductId())
                .then()
                .statusCode(400);
    }

    @Test
    public void testInvalidPatternValidation() {

        Item invalidPatternItem = createTestingItem("123456", "test", 10.0f, 1);

        given()
                .log().all()
                .when().header("X-User-Id", "2")
                .contentType("application/json")
                .body(invalidPatternItem)
                .when().post("/basket/{productId}", invalidPatternItem.getProductId())
                .then()
                .statusCode(400);
    }

    @Test
    public void testItemPriceTooLowValidation() {

        Item tooLowPriceItem = createTestingItem("1-2-3-4-5-6", "test", 9.9f, 1);

        given()
                .log().all()
                .when().header("X-User-Id", "2")
                .contentType("application/json")
                .body(tooLowPriceItem)
                .when().post("/basket/{productId}", tooLowPriceItem.getProductId())
                .then()
                .statusCode(400);
    }

    @Test
    public void testItemPriceTooHighValidation() {

        Item tooHighPriceItem = createTestingItem("1-2-3-4-5-6", "test", 100.1f, 1);

        given()
                .log().all()
                .when().header("X-User-Id", "2")
                .contentType("application/json")
                .body(tooHighPriceItem)
                .when().post("/basket/{productId}", tooHighPriceItem.getProductId())
                .then()
                .statusCode(400);
    }

    @Test
    public void testItemPriceNullValidation() {

        Item nullPriceItem = createTestingItem("1-2-3-4-5-6", "test", null, 1);

        given()
                .log().all()
                .when().header("X-User-Id", "2")
                .contentType("application/json")
                .body(nullPriceItem)
                .when().post("/basket/{productId}", nullPriceItem.getProductId())
                .then()
                .statusCode(400);
    }

    @Test
    public void testItemCountTooLowValidation() {

        Item tooLowCountItem = createTestingItem("1-2-3-4-5-6", "test", 10.0f, 0);

        given()
                .log().all()
                .when().header("X-User-Id", "2")
                .contentType("application/json")
                .body(tooLowCountItem)
                .when().post("/basket/{productId}", tooLowCountItem.getProductId())
                .then()
                .statusCode(400);
    }

//    @Test
//    public void testItemCountTooHighValidation() {
//
//        Item tooHighCountItem = createTestingItem("1-2-3-4-5-6", "test", 10.0f, 11);
//
//        given()
//                .log().all()
//                .when().header("X-User-Id", "2")
//                .contentType("application/json")
//                .body(tooHighCountItem)
//                .when().post("/basket/{productId}", tooHighCountItem.getProductId())
//                .then()
//                .statusCode(400);
//    }

    @Test
    public void testItemCountNullValidation() {

        Item nullCountItem = createTestingItem("1-2-3-4-5-6", "test", 10.0f, null);

        given()
                .log().all()
                .when().header("X-User-Id", "2")
                .contentType("application/json")
                .body(nullCountItem)
                .when().post("/basket/{productId}", nullCountItem.getProductId())
                .then()
                .statusCode(400);
    }

    @Test
    public void testItemCountNegativeValidation(){

        Item negativeCountItem = createTestingItem("1-2-3-4-5-6", "test", 10.0f, -1);

        given()
                .log().all()
                .when().header("X-User-Id", "2")
                .contentType("application/json")
                .body(negativeCountItem)
                .when().post("/basket/{productId}", negativeCountItem.getProductId())
                .then()
                .statusCode(400);
    }

    @Test
    public void testItemPriceNegativeValidation(){

        Item negativePriceItem = createTestingItem("1-2-3-4-5-6", "test", -10.0f, 1);

        given()
                .log().all()
                .when().header("X-User-Id", "2")
                .contentType("application/json")
                .body(negativePriceItem)
                .when().post("/basket/{productId}", negativePriceItem.getProductId())
                .then()
                .statusCode(400);
    }

}
