package app;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserApiTest {

    @BeforeEach
    public void setUp() {
        // Укажите здесь URL вашего задеплоенного в Яндекс Облаке/VDS приложения
        RestAssured.baseURI = "https://bbaqjmjeinr8jbgrn77t.containers.yandexcloud.net"; // или IP-адрес вашей VDS

        // Если ваше приложение в облаке работает на стандартном 80 или 443 порту,
        // строчку с портом можно вообще убрать (или раскомментировать, если порт специфический, например 8080)
        // RestAssured.port = 8080;
    }

    @Test
    public void testCreateAndGetPlayer() {
        String userJson = """
                {
                  "name": "Тестеров",
                  "email": "tester@notes.ru",
                  "password": "password123"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(userJson)
                .log().all()
                .when()
                .post("/api/users")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", equalTo("Тестеров"))
                .body("email", equalTo("tester@notes.ru"));

        given()
                .queryParam("email", "tester@notes.ru")
                .when()
                .get("/api/users/search")
                .then()
                .statusCode(200)
                .body("name", equalTo("Тестеров"));
    }
}