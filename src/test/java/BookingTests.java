import Entities.Booking;
import Entities.BookingDates;
import Entities.User;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.hamcrest.Matchers.*;

public class BookingTests {
    public static Faker faker;
    private static RequestSpecification request;
    private static Booking booking;
    private static BookingDates bookingDates;
    private static User user;

    @BeforeAll
    public static void Setup(){
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        faker = new Faker();
        user = new User(faker.name().username(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().safeEmailAddress(),
                faker.internet().password(8,10),
                faker.phoneNumber().toString());

        bookingDates = new BookingDates("2018-01-02", "2018-01-03");
        booking = new Booking(user.getFirstName(), user.getLastName(),
                (float)faker.number().randomDouble(2, 50, 100000),
                true, bookingDates,
                "");
        RestAssured.filters(new RequestLoggingFilter(),new ResponseLoggingFilter(), new ErrorLoggingFilter());
    }

    @BeforeEach
    void setRequest(){
        request = given().config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(ContentType.JSON)
                .auth().basic("admin", "password123");
    }

    @Test
    public void getAllBookingsById_returnOk(){
            Response response = request
                                    .when()
                                        .get("/booking")
                                    .then()
                                        .extract()
                                        .response();


        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    public void  getAllBookingsByUserFirstName_BookingExists_returnOk(){
                    request
                        .when()
                            .queryParam("firstName", "Carol")
                            .get("/booking")
                        .then()
                            .assertThat()
                            .statusCode(200)
                            .contentType(ContentType.JSON)
                        .and()
                        .body("results", hasSize(greaterThan(0)));

    }

    @Test
    public void  CreateBooking_WithValidData_returnOk() {

        given().config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(ContentType.JSON)
                .when()
                .body(booking)
                .post("/booking")
                .then()
                .body(matchesJsonSchemaInClasspath("createBookingRequestSchema.json"))
                .and()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON).and().time(lessThan(5000L));
    }

    @Test
    public void getBookingById_returnOk(){
        request
            .when()
            .pathParam("idd", 97)
            .get("/booking/{idd}")// a qtde de chaves sera a qtde de path params com os mesmos nomes
            .then()
            .body(matchesJsonSchemaInClasspath("createBookingRequestSchema.json"))
            .and()
            .assertThat()
            .statusCode(200)
            .contentType(ContentType.JSON).and().time(lessThan(5000L));

    }

    @Test
    public void putBookingById_returnOk(){
        request
                .auth().preemptive().basic("admin", "password123")
                .pathParam("idde", 15)
                .when()
                .body(booking)
                .put("/booking/{idde}")
                .then()
                .body(matchesJsonSchemaInClasspath("createBookingRequestSchema.json"))
                .and()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON).and().time(lessThan(5000L));

    }

    @Test
    public void patchBookingById_returnOk(){
        request
                .auth().preemptive().basic("admin", "password123")
                .pathParam("numb", 29)
                .when()
                .body(new BookingDates("2008-01-01", "2028-01-01"))
                .patch("/booking/{numb}")
                .then()
                .and()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON).and().time(lessThan(5000L));

    }

    @Test
    public void deleteBookingById_returnOk(){
        request
                .auth().preemptive().basic("admin", "password123")
                .pathParam("ids", 134)
                .when()
                .delete("/booking/{ids}")
                .then()
                .and()
                .assertThat()
                .statusCode(201)
                .and().time(lessThan(5000L));
    }
}
