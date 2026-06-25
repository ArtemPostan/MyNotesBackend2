package app;

import app.models.UserEntity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserApiTest {

    @BeforeEach
    public void setUp() {
        Properties props = new Properties();
        String baseUrl = "http://localhost:8080"; // Дефолтное значение

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("test.properties")) {
            if (input != null) {
                props.load(input);
                baseUrl = props.getProperty("test.base.url", baseUrl);
            } else {
                System.out.println("Файл test.properties не найден, используем дефолтный URL");
            }
        } catch (Exception e) {
            System.out.println("Не удалось прочитать test.properties, ошибка: " + e.getMessage());
        }

        System.out.println("Тесты отправляют запросы на: " + baseUrl);
        RestAssured.baseURI = baseUrl;
    }

    @Test
    public void testCreateAndGetPlayer() {
        // Генерируем уникальный email для каждого запуска теста
        String uniqueEmail = "tester_" + UUID.randomUUID() + "@notes10.ru";

        String userJson = """
                {
                  "name": "Постаногов",
                  "email": "%s",
                  "password": "password123"
                }
                """.formatted(uniqueEmail);

        given()
                .contentType(ContentType.JSON)
                .body(userJson)
                .log().all()
                .when()
                .post("/api/users")
                .then()
                .statusCode(200) // Если бэкэнд возвращает 201 Created, поменяйте на 201
                .body("id", notNullValue())
                .body("name", equalTo("Постаногов"))
                .body("email", equalTo(uniqueEmail));

        // Проверяем поиск
        given()
                .queryParam("email", uniqueEmail)
                .when()
                .get("/api/users/search")
                .then()
                .statusCode(200)
                .body("name", equalTo("Постаногов"));
    }

    @Test
    public void testDeleteUserCorrectly() {
        String uniqueEmail = "delete_test_" + UUID.randomUUID() + "@test.ru";

        // Вместо передачи Entity с id=null, надежнее передать Map или DTO,
        // чтобы в JSON не улетало поле "id": null, если бэкэнд к этому чувствителен.
        Map<String, String> newUserMap = Map.of(
                "name", "Тестеров",
                "email", uniqueEmail,
                "password", "password123"
        );

        // 1. Создаем пользователя
        UserEntity createdUser = given()
                .contentType(ContentType.JSON)
                .body(newUserMap)
                .when()
                .post("/api/users")
                .then()
                .statusCode(200)
                .extract().as(UserEntity.class);

        // 2. Берем ID, который сгенерировал бэкэнд
        String realId = createdUser.getId();
        System.out.println("Будем удалять пользователя с ID: " + realId);

        // 3. Удаляем именно этого пользователя
        given()
                .pathParam("id", realId)
                .when()
                .delete("/api/users/{id}")
                .then()
                .log().all()
                .statusCode(200);
    }
}