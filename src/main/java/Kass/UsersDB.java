package Kass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Вложенный класс для работы с таблицей Users
public class UsersDB {
    private final Connection connection;
    private static final Logger logger = LoggerFactory.getLogger(UsersDB.class);

    public UsersDB(Connection connection) {
        this.connection = connection;
    }

    public void createUser(String username, String password, String role) throws SQLException {
        String sql = "INSERT INTO Users (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
            logger.info("Пользователь {} добавлен в систему.", username);
        } catch (SQLException e) {
            logger.error("Ошибка при добавлении пользователя {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, role FROM Users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }
            logger.info("Получено {} пользователей из базы.", users.size());
        } catch (SQLException e) {
            logger.error("Ошибка при получении списка пользователей: {}", e.getMessage(), e);
            throw e;
        }
        return users;
    }

    public User getUserById(int id) throws SQLException {
        String sql = "SELECT id, username, password, role FROM Users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role")
                    );
                    logger.info("Получен пользователь по ID {}: {}", id, user.username);
                    return user;
                } else {
                    logger.warn("Пользователь с ID {} не найден.", id);
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении пользователя по ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public boolean updateUser(int id, String username, String password, String role) throws SQLException {
        String sql = "UPDATE Users SET username = ?, password = ?, role = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.setInt(4, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Пользователь с ID {} успешно обновлён.", id);
            } else {
                logger.warn("Пользователь с ID {} не найден при обновлении.", id);
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении пользователя с ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public boolean deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM Users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Пользователь с ID {} успешно удалён.", id);
            } else {
                logger.warn("Пользователь с ID {} не найден при удалении.", id);
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении пользователя с ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Вложенный класс для представления записи Users
    public static class User {
        public final int id;
        public final String username;
        public final String password;
        public final String role;

        public User(int id, String username, String password, String role) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.role = role;
        }
    }
}