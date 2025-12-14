package Kass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Вложенный класс для работы с таблицей Checks
public class ChecsDB {
    private final Connection connection;
    private static final Logger logger = LoggerFactory.getLogger(ChecsDB.class);

    public ChecsDB(Connection connection) {
        this.connection = connection;
    }

    public void createCheck(String checkNumber, String date, double totalAmount, int cashierId) throws SQLException {
        String sql = "INSERT INTO Checks (checkNumber, date, totalAmount, cashierId) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, checkNumber);
            pstmt.setString(2, date);
            pstmt.setDouble(3, totalAmount);
            pstmt.setInt(4, cashierId);
            pstmt.executeUpdate();
            logger.info("Чек №{} на сумму {} добавлен для кассира ID {}.", checkNumber, totalAmount, cashierId);
        } catch (SQLException e) {
            logger.error("Ошибка при добавлении чека №{}: {}", checkNumber, e.getMessage(), e);
            throw e;
        }
    }

    public List<Check> getAllChecks() throws SQLException {
        List<Check> checks = new ArrayList<>();
        String sql = "SELECT id, checkNumber, date, totalAmount, cashierId FROM Checks";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                checks.add(new Check(
                        rs.getInt("id"),
                        rs.getString("checkNumber"),
                        rs.getString("date"),
                        rs.getDouble("totalAmount"),
                        rs.getInt("cashierId")
                ));
            }
            logger.info("Получено {} чеков из базы.", checks.size());
        } catch (SQLException e) {
            logger.error("Ошибка при получении списка чеков: {}", e.getMessage(), e);
            throw e;
        }
        return checks;
    }

    // Вложенный класс для представления записи Checks
    public static class Check {
        public final int id;
        public final String checkNumber;
        public final String date;
        public final double totalAmount;
        public final int cashierId;

        public Check(int id, String checkNumber, String date, double totalAmount, int cashierId) {
            this.id = id;
            this.checkNumber = checkNumber;
            this.date = date;
            this.totalAmount = totalAmount;
            this.cashierId = cashierId;
        }
    }
}
