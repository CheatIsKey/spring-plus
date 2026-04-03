package org.example.expert.domain.config;

import org.example.expert.config.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class DataInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 유저_500만건_bulk_insert() {
        int totalCount = 5_000_000;
        int batchSize = 50_000;
        String encodedPassword = passwordEncoder.encode("1234");

        for (int i = 0; i < totalCount / batchSize; i++) {
            List<Object[]> batch = new ArrayList<>();

            for (int j = 0; j < batchSize; j++) {
                int idx = i * batchSize + j;
                String nickname = idx + " : " +
                        UUID.randomUUID().toString().replace("-", "").substring(0, 12);

                batch.add(new Object[]{
                        nickname,
                        idx + "@test.com",
                        encodedPassword,
                        "USER",
                        LocalDateTime.now(),
                        LocalDateTime.now()
                });
            }

            jdbcTemplate.batchUpdate(
                    "INSERT INTO users (nickname, email, password, user_role, created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?)", batch
            );

            System.out.println((i + 1) * batchSize + "건 완료");
        }
    }
}
