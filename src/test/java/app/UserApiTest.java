package app;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApiTest {
    // Читаем хост. Если его нет в файле — берем http://localhost по умолчанию
    @Value("${test.server.host:http://localhost}")
    private String configHost;

    // Читаем порт. Если его нет в файле — берем 8080 по умолчанию
    @Value("${test.server.port:8080}")
    private int configPort;

    // Флаг: тестируем удаленный сервер или локальный? (по умолчанию false)
    @Value("${test.server.is-remote:false}")
    private boolean isRemote;

    // Локальный порт, который Spring поднимает для H2
    @LocalServerPort
    private int localPort;

    @BeforeEach
    public void setUp() {
        if (isRemote) {
            // Настройки для VDS или Яндекс Облака
            RestAssured.baseURI = configHost;
            RestAssured.port = configPort;
        } else {
            // Настройки для локального тестирования (то, что работает сейчас)
            RestAssured.baseURI = "http://localhost";
            RestAssured.port = localPort;
        }
    }

    @Test
    public void testCreateAndGetPlayer() {
        String userJson = """
                {
                  "login": "Тестеров",
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
            .body("login", equalTo("Тестеров"))
            .body("email", equalTo("tester@notes.ru"));

        given()
            .queryParam("email", "tester@notes.ru")
        .when()
            .get("/api/users/search")
        .then()
            .statusCode(200)
            .body("login", equalTo("Тестеров"));
    }
}