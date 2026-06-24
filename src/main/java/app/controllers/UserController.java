package app.controllers;

import app.models.UserEntity;
import app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/search")
    public UserEntity getUserByEmail(@RequestParam String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден!"));
    }
    @GetMapping
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public UserEntity createUser(@RequestBody UserEntity user) {
        logger.info("Получен запрос на создание пользователя: {}", user.getEmail()); // Лог

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


}
