package Kass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

import static Kass.App.connection;

public class Settings {
    private String Font;
    private int SizeFontBtn = 18;
    private int SizeFontLabels = 24;
    private int SizeFontTables = 18;
    private String Color = "Black";
    private String Orientation;

    public String getFont() {
        return Font;
    }

    public void setFont(String font) {
        Font = font;
    }

    public int getSizeFontBtn() {
        return SizeFontBtn;
    }

    public void setSizeFontBtn(int sizeFontBtn) {
        SizeFontBtn = sizeFontBtn;
    }

    public int getSizeFontLabels() {
        return SizeFontLabels;
    }

    public void setSizeFontLabels(int sizeFontLabels) {
        SizeFontLabels = sizeFontLabels;
    }

    public int getSizeFontTables() {
        return SizeFontTables;
    }

    public void setSizeFontTables(int sizeFontTables) {
        SizeFontTables = sizeFontTables;
    }

    public String getColor() {
        return Color;
    }

    public void setColor(String color) {
        Color = color;
    }

    public String getOrientation() {
        return Orientation;
    }

    public void setOrientation(String orientation) {
        Orientation = orientation;
    }


    public void showSettingsDialog() {
        JFrame settingsFrame = new JFrame("Настройки");
        settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        settingsFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        settingsFrame.setUndecorated(true);

        JButton usersButton = new JButton("Пользователи");
        usersButton.setFont(new Font(Font, java.awt.Font.PLAIN, SizeFontBtn));
        usersButton.addActionListener(usersSettings());

        JPanel panel = new JPanel();
        panel.add(usersButton);
        settingsFrame.add(panel);

        settingsFrame.setVisible(true);
    }

    private ActionListener usersSettings() {
        return e -> {
            UsersDB usersDB = new UsersDB(connection);
            JFrame usersFrame = new JFrame("Управление пользователями");
            usersFrame.setSize(800, 600);
            usersFrame.setLocationRelativeTo(null);
            usersFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // Панель с кнопками
            JPanel buttonPanel = new JPanel();
            JButton addButton = new JButton("Добавить");
            addButton.addActionListener(e1 -> {
                String username = JOptionPane.showInputDialog(usersFrame, "Введите имя пользователя:");
                String login = JOptionPane.showInputDialog(usersFrame, "Придумайте пароль:");
                String role = (String) JOptionPane.showInputDialog(usersFrame, "Выберите роль:",
                        "Роль", JOptionPane.QUESTION_MESSAGE, null,
                        new String[]{"Administrator", "User"}, "User");
                if (username != null && login != null && role != null) {
                    try {
                        usersDB.createUser(username, login, role);
                        JOptionPane.showMessageDialog(usersFrame, "Пользователь добавлен.");
                        usersFrame.dispose();
                        usersSettings().actionPerformed(e); // Обновить список
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(usersFrame, "Ошибка при добавлении пользователя: " + ex.getMessage(),
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            JButton editButton = new JButton("Изменить");
            JButton deleteButton = new JButton("Удалить");
            buttonPanel.add(addButton);
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);

            // Получаем список пользователей

            List<UsersDB.User> usersList;
            try {
                usersList = usersDB.getAllUsers();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Ошибка при загрузке пользователей: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Определяем заголовки таблицы
            String[] columnNames = {"ID", "Имя", "Логин", "Роль"};
            Object[][] data = new Object[usersList.size()][4];

            for (int i = 0; i < usersList.size(); i++) {
                UsersDB.User user = usersList.get(i);
                data[i][0] = user.id;
                data[i][1] = user.username;
                data[i][2] = "********";
                data[i][3] = user.role;
            }

            // Создаём таблицу
            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);

            // Добавляем компоненты на форму
            usersFrame.setLayout(new BorderLayout());
            usersFrame.add(buttonPanel, BorderLayout.NORTH);
            usersFrame.add(scrollPane, BorderLayout.CENTER);

            usersFrame.setVisible(true);
        };
    }
}