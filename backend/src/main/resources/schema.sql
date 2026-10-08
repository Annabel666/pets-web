DROP TABLE IF EXISTS album_hit;
DROP TABLE IF EXISTS album;
DROP TABLE IF EXISTS stage;
DROP TABLE IF EXISTS quote_tag;
DROP TABLE IF EXISTS quote_item;
DROP TABLE IF EXISTS voice;
DROP TABLE IF EXISTS artist;

CREATE TABLE artist (
  id INT PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  english_name VARCHAR(64) NOT NULL,
  born VARCHAR(32) NOT NULL,
  born_place VARCHAR(32) NOT NULL,
  roles VARCHAR(128) NOT NULL,
  tagline VARCHAR(255) NOT NULL,
  bio TEXT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE voice (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(128) NOT NULL,
  release_year INT NOT NULL,
  note VARCHAR(255) NOT NULL,
  bvid VARCHAR(32) NOT NULL,
  list_name VARCHAR(16) NOT NULL,
  autoplay TINYINT NOT NULL DEFAULT 0,
  sort_no INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quote_item (
  id VARCHAR(64) PRIMARY KEY,
  content TEXT NOT NULL,
  origin VARCHAR(255) NOT NULL,
  quote_year INT NOT NULL,
  sort_no INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quote_tag (
  quote_id VARCHAR(64) NOT NULL,
  tag VARCHAR(32) NOT NULL,
  sort_no INT NOT NULL,
  PRIMARY KEY (quote_id, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE stage (
  id VARCHAR(64) PRIMARY KEY,
  stage_date VARCHAR(32) NOT NULL,
  place VARCHAR(64) NOT NULL,
  venue VARCHAR(128) NOT NULL,
  title VARCHAR(128) NOT NULL,
  note VARCHAR(255) NOT NULL,
  kind VARCHAR(32) NOT NULL,
  voice_id VARCHAR(64) NULL,
  sort_no INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE album (
  id INT PRIMARY KEY,
  release_year INT NOT NULL,
  title VARCHAR(128) NOT NULL,
  label VARCHAR(64) NOT NULL,
  sort_no INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE album_hit (
  album_id INT NOT NULL,
  title VARCHAR(128) NOT NULL,
  sort_no INT NOT NULL,
  PRIMARY KEY (album_id, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hall_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(32) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_hall_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS guestbook (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  username VARCHAR(32) NOT NULL,
  content VARCHAR(500) NOT NULL,
  parent_id BIGINT NULL,
  reply_to VARCHAR(32) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_guestbook_created (created_at),
  KEY idx_guestbook_parent (parent_id),
  CONSTRAINT fk_guestbook_user FOREIGN KEY (user_id) REFERENCES hall_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hall_listen (
  user_id BIGINT NOT NULL,
  voice_id VARCHAR(64) NOT NULL,
  played_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (user_id, voice_id),
  CONSTRAINT fk_listen_user FOREIGN KEY (user_id) REFERENCES hall_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS guestbook_agree (
  message_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (message_id, user_id),
  CONSTRAINT fk_agree_message FOREIGN KEY (message_id) REFERENCES guestbook (id),
  CONSTRAINT fk_agree_user FOREIGN KEY (user_id) REFERENCES hall_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
