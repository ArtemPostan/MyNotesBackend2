package app.controllers;

import app.models.UserEntity;
import app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://myfronten2.website.yandexcloud.net")
public class UserController {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/search")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        return userRepository.findByEmail(email)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(user)) // <- Подсказали тип Java
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь с такой почтой не найден"));
    }

    @GetMapping
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public UserEntity createUser(@RequestBody UserEntity user) {
        logger.info("Получен запрос на создание пользователяя: {}", user.getEmail()); // Лог

        if (user.getId() == null) {
            user.setId(java.util.UUID.randomUUID().toString());
        }

        try {
            UserEntity savedUser = userRepository.save(user);
            logger.info("Пользователь успешно сохранен с ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("Ошибка при сохранении пользователя в БД!", e); // Это выведет ВСЮ ошибку в логи
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        logger.info("--- ЗАПРОС НА УДАЛЕНИЕ ПРИНЯТ: ID = {} ---", id);
        userRepository.deleteById(id);
    }

    @GetMapping("/debug/paths")
    public String debug() {
        return "Я работаю!";
    }


}
