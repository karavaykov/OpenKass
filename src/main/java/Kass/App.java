package Kass;

import com.google.gson.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import Kass.CashierDB;

public class App {
    private static JTable table;
    private static DefaultTableModel mainModel;
    private static Settings Settings;
    private static Connection connection;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Settings = new Settings();
            try {
                BDConnect();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


            // Показываем форму авторизации перед запуском основного окна
            try {
                if (showLoginDialog()) {
                    JFrame frame = createMainFrame();
                    initializeTable();
                    frame.add(createTopPanel(), BorderLayout.NORTH);
                    frame.add(new JScrollPane(table), BorderLayout.CENTER);
                    frame.add(createLeftButtonPanel(), BorderLayout.WEST);
                    frame.add(createRightButtonPanel(), BorderLayout.EAST);
                    frame.setVisible(true);
                } else {
                    System.exit(0); // Если вход не выполнен, закрываем приложение
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void BDConnect() throws SQLException {
        CashierDB cashierDB = new CashierDB("TestDB");
        connection = cashierDB.getConnection();


    }


    // Метод для отображения формы авторизации
    private static boolean showLoginDialog() throws SQLException {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel labelLogin = new JLabel("Логин:");
        JTextField fieldLogin = new JTextField(15);
        JLabel labelPassword = new JLabel("Пароль:");
        JPasswordField fieldPassword = new JPasswordField(15);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(labelLogin, gbc);
        gbc.gridx = 1;
        panel.add(fieldLogin, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(labelPassword, gbc);
        gbc.gridx = 1;
        panel.add(fieldPassword, gbc);


        UsersDB users = new UsersDB(connection);
        List<UsersDB.User> usersList = users.getAllUsers();

        int result = JOptionPane.showConfirmDialog(null, panel, "Авторизация сотрудника",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String login = fieldLogin.getText();
            String password = new String(fieldPassword.getPassword());

            return usersList.stream().anyMatch(emp ->
                    emp.username.equals(login) && emp.password.equals(password));
        }
        return false;
    }


    private static JFrame createMainFrame() {
        JFrame frame = new JFrame("Касса");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return frame;
    }

    private static void initializeTable() {
        table = new JTable();
        table.setRowHeight(30);
        table.setFont(new Font(Settings.getFont(), Font.PLAIN, Settings.getSizeFontTables()));
        String[] columns = {"Товар", "Количество", "Цена", "Ставка НДС", "Сумма"};
        mainModel = new DefaultTableModel(columns, 0);
        table.setModel(mainModel);
    }

    private static Component createTopPanel() {
        JLabel label = new JLabel("Рабочее место кассира");
        label.setFont(new Font(Settings.getFont(), Font.BOLD, Settings.getSizeFontLabels()));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private static Component createLeftButtonPanel() {
        JButton buttonAdd = new JButton("Добавить");
        buttonAdd.setFont(new Font(Settings.getFont(), Font.BOLD, Settings.getSizeFontBtn()));
        buttonAdd.addActionListener(e -> addProduct());

        JButton buttonDelete = new JButton("Удалить");
        buttonDelete.setFont(new Font(Settings.getFont(), Font.BOLD, Settings.getSizeFontBtn()));
        buttonDelete.addActionListener(e -> DeleteProduct());


        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(buttonAdd);
        panel.add(buttonDelete, BorderLayout.SOUTH);
        return panel;
    }

    private static Component createRightButtonPanel() {
        JButton buttonAdd = new JButton("ПробитьЧек");
        buttonAdd.setFont(new Font(Settings.getFont(), Font.BOLD, Settings.getSizeFontBtn()));
        buttonAdd.addActionListener(e -> PrintCheck());

        JButton ApplyDiscount = new JButton("Применить скидку");
        ApplyDiscount.setFont(new Font(Settings.getFont(), Font.BOLD, Settings.getSizeFontBtn()));
        ApplyDiscount.addActionListener(e -> ApplyDiscount());

        JButton buttonChecks = new JButton("Открыть чеки");
        buttonChecks.setFont(new Font(Settings.getFont(), Font.BOLD, Settings.getSizeFontBtn()));
        buttonChecks.addActionListener(e -> getChecks());


        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(buttonAdd);
        panel.add(ApplyDiscount, BorderLayout.SOUTH);
        panel.add(buttonChecks, BorderLayout.SOUTH);
        return panel;
    }

    private static void getChecks() {
        JOptionPane.showMessageDialog(null, "В разработке", "Внимание", JOptionPane.INFORMATION_MESSAGE);


    }

    private static void PrintCheck() {
        if (mainModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Невозможно пробить чек: таблица товаров пуста.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            FiscalReceipt receipt = createFiscalReceiptFromTable();
            boolean saved = FiscalStorage.saveReceipt(receipt);

            if (saved) {
                JOptionPane.showMessageDialog(null,
                        "Чек успешно пробит и зафискализирован!\n" +
                                "Номер чека: " + receipt.getReceiptId() + "\n" +
                                "Сумма: " + String.format("%.2f", receipt.getTotalAmount()) + " руб.\n" +
                                "Дата: " + receipt.getTimestamp(),
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
                mainModel.setRowCount(0);
            } else {
                JOptionPane.showMessageDialog(null, "Ошибка при сохранении данных фискализации!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при формировании чека.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static FiscalReceipt createFiscalReceiptFromTable() {
        List<FiscalItem> items = new ArrayList<>();
        double totalAmount = 0.0;

        for (int i = 0; i < mainModel.getRowCount(); i++) {
            String productName = (String) mainModel.getValueAt(i, 0);
            int quantity = (int) mainModel.getValueAt(i, 1);
            double price = parseFormattedDouble((String) mainModel.getValueAt(i, 2));
            double amount = parseFormattedDouble((String) mainModel.getValueAt(i, 4));
            totalAmount += amount;

            items.add(new FiscalItem(productName, quantity, price, amount));
        }

        return new FiscalReceipt(
                generateReceiptId(),
                new java.util.Date(),
                items,
                totalAmount,
                "ОПЛАТА НАЛИЧНЫМИ"
        );
    }

    private static double parseFormattedDouble(String value) {
        return Double.parseDouble(value.replace(',', '.'));
    }

    // Внутренний класс для элемента чека
    private static class FiscalItem {
        private final String name;
        private final int quantity;
        private final double price;
        private final double amount;

        public FiscalItem(String name, int quantity, double price, double amount) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
            this.amount = amount;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }

        public double getAmount() {
            return amount;
        }
    }

    // Внутренний класс для чека
    private static class FiscalReceipt {
        private final String receiptId;
        private final java.util.Date timestamp;
        private final List<FiscalItem> items;
        private final double totalAmount;
        private final String paymentMethod;

        public FiscalReceipt(String receiptId, java.util.Date timestamp, List<FiscalItem> items, double totalAmount, String paymentMethod) {
            this.receiptId = receiptId;
            this.timestamp = timestamp;
            this.items = List.copyOf(items); // неизменяемая копия
            this.totalAmount = totalAmount;
            this.paymentMethod = paymentMethod;
        }

        public String getReceiptId() {
            return receiptId;
        }

        public java.util.Date getTimestamp() {
            return new java.util.Date(timestamp.getTime());
        }

        public List<FiscalItem> getItems() {
            return new java.util.ArrayList<>(items);
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }
    }

    private static String generateReceiptId() {
        return "CHK-" + System.currentTimeMillis();
    }

    private static class FiscalStorage {
        private static final String FILE_PATH = "fiscal_receipts.json";
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

        public static boolean saveReceipt(FiscalReceipt receipt) {
            List<FiscalReceipt> receipts = readAllReceipts();
            receipts.add(receipt);

            try (FileWriter writer = new FileWriter(FILE_PATH)) {
                GSON.toJson(receipts, writer);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private static List<FiscalReceipt> readAllReceipts() {
            File file = new java.io.File(FILE_PATH);
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }

            try (Scanner scanner = new Scanner(file)) {
                StringBuilder content = new StringBuilder();
                while (scanner.hasNextLine()) {
                    content.append(scanner.nextLine());
                }

                JsonElement jsonElement = JsonParser.parseString(content.toString().trim());
                if (jsonElement.isJsonArray()) {
                    JsonArray array = jsonElement.getAsJsonArray();
                    List<FiscalReceipt> receipts = new ArrayList<>();
                    for (com.google.gson.JsonElement element : array) {
                        receipts.add(GSON.fromJson(element, FiscalReceipt.class));
                    }
                    return receipts;
                } else if (jsonElement.isJsonObject()) {
                    return List.of(GSON.fromJson(jsonElement, FiscalReceipt.class));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }
    }

    private static void ApplyDiscount() {
        JOptionPane.showMessageDialog(null, "В разработке", "Успех", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void DeleteProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            mainModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(null, "Пожалуйста, выберите строку для удаления.", "Ошибка", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static void addProduct() {
        JFrame productFrame = new JFrame("Товары");
        productFrame.setSize(400, 300);
        productFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        productFrame.setLocationRelativeTo(null);

        JTable productTable = new JTable();
        productTable.setRowHeight(30);
        productTable.setFont(new Font(Settings.getFont(), Font.PLAIN, Settings.getSizeFontTables()));
        DefaultTableModel productModel = new DefaultTableModel(new String[]{"Товар", "Количество"}, 0);
        for (Object[] row : getProducts()) {
            productModel.addRow(row);
        }
        productTable.setModel(productModel);

        productFrame.add(new JScrollPane(productTable), BorderLayout.CENTER);
        productFrame.add(createSelectButtonPanel(productTable, productFrame), BorderLayout.SOUTH);
        productFrame.setVisible(true);
    }

    private static Component createSelectButtonPanel(JTable productTable, JFrame productFrame) {
        JButton selectButton = new JButton("Выбрать");
        selectButton.setFont(new Font(Settings.getFont(), Font.BOLD, Settings.getSizeFontBtn()));
        selectButton.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow != -1) {
                String productName = (String) productTable.getValueAt(selectedRow, 0);
                int quantity = (int) productTable.getValueAt(selectedRow, 1);
                double price = Math.round(Math.random() * 100 + 10);
                double vatRate = 20.0;
                double amount = quantity * price;

                mainModel.addRow(new Object[]{
                        productName,
                        quantity,
                        String.format("%.2f", price),
                        vatRate + "%",
                        String.format("%.2f", amount)
                });
            }
            productFrame.dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(selectButton);
        return buttonPanel;
    }

    private static Object[][] getProducts() {
        return new Object[][]{
                {"Хлеб", 10}, {"Молоко", 5}, {"Яйца", 30}, {"Сахар", 20}, {"Мука", 15},
                {"Соль", 50}, {"Рис", 12}, {"Макароны", 8}, {"Картофель", 100}, {"Лук", 40},
                {"Морковь", 35}, {"Яблоки", 60}, {"Бананы", 25}, {"Апельсины", 20}, {"Гречка", 10},
                {"Сливочное масло", 7}, {"Сыр", 15}, {"Колбаса", 12}, {"Ветчина", 10}, {"Курица", 8},
                {"Говядина", 6}, {"Свинина", 5}, {"Рыба", 9}, {"Творог", 11}, {"Сметана", 14},
                {"Йогурт", 18}, {"Кефир", 13}, {"Сок", 22}, {"Вода", 100}, {"Чай", 30},
                {"Кофе", 8}, {"Печенье", 25}, {"Шоколад", 20}, {"Мороженое", 15}, {"Томаты", 45},
                {"Огурцы", 40}, {"Перец", 25}, {"Капуста", 18}, {"Брокколи", 12}, {"Цветная капуста", 10},
                {"Грибы", 7}, {"Авокадо", 6}, {"Манго", 5}, {"Ананас", 4}, {"Виноград", 10},
                {"Сливы", 14}, {"Персики", 12}, {"Нектарины", 11}, {"Груши", 9}, {"Лимоны", 20}, {"Киви", 18}
        };
    }
}