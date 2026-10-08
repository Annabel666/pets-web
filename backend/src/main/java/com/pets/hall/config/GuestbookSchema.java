package com.pets.hall.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class GuestbookSchema implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public GuestbookSchema(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer columns = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'guestbook' AND COLUMN_NAME = 'parent_id'",
                Integer.class);
        if (columns != null && columns == 0) {
            jdbcTemplate.execute("ALTER TABLE guestbook ADD COLUMN parent_id BIGINT NULL");
            jdbcTemplate.execute("CREATE INDEX idx_guestbook_parent ON guestbook (parent_id)");
        }
        Integer replyTo = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'guestbook' AND COLUMN_NAME = 'reply_to'",
                Integer.class);
        if (replyTo != null && replyTo == 0) {
            jdbcTemplate.execute("ALTER TABLE guestbook ADD COLUMN reply_to VARCHAR(32) NULL");
        }
    }
}
