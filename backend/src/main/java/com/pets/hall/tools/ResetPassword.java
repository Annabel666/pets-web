package com.pets.hall.tools;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class ResetPassword {
    private static final String URL = "jdbc:mysql://[::1]:3307/pets_hall"
            + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8"
            + "&connectionCollation=utf8mb4_unicode_ci&serverTimezone=Asia/Shanghai";

    private ResetPassword() {}

    public static void main(String[] args) throws Exception {
        String[] pair = readPair();
        String username = pair[0].trim();
        String password = pair[1];
        if (!username.matches("^[\\u4e00-\\u9fffA-Za-z0-9_]{2,20}$")) {
            System.err.println("名字用 2 到 20 位中文、字母或数字。");
            System.exit(2);
        }
        if (password.length() < 6 || password.length() > 64) {
            System.err.println("密码用 6 到 64 位。");
            System.exit(2);
        }
        String hash = new BCryptPasswordEncoder().encode(password);
        try (Connection connection = DriverManager.getConnection(URL, "root", "pets_hall");
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE hall_user SET password_hash = ? WHERE username = ?")) {
            statement.setString(1, hash);
            statement.setString(2, username);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                System.err.println("没有这个名字。");
                System.exit(1);
            }
        } catch (Exception ex) {
            System.err.println("数据库没有连上。请先确认 Docker 里的 pets-mysql 已启动。");
            System.exit(1);
        }
        System.out.println("已经改好：" + username);
    }

    private static String[] readPair() throws Exception {
        String file = System.getProperty("pets.file");
        if (file == null || file.isBlank()) {
            System.err.println("用法：scripts\\reset-password.ps1 -Username 名字 -Password 新密码");
            System.exit(2);
        }
        List<String> lines = Files.readAllLines(Path.of(file), StandardCharsets.UTF_8);
        if (lines.size() < 2) {
            System.err.println("用法：scripts\\reset-password.ps1 -Username 名字 -Password 新密码");
            System.exit(2);
        }
        return new String[] {lines.get(0), lines.get(1)};
    }
}
