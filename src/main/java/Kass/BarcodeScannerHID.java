package Kass;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class BarcodeScannerHID {
    private StringBuilder barcode = new StringBuilder(); // Хранит набираемый штрих-код посимвольно
    private long lastKeyTime = 0; // Время последнего нажатия клавиши
    private final long THRESHOLD = 100; // Порог в мс: если между нажатиями больше — сбрасываем буфер

    public BarcodeScannerHID() {
        JFrame frame = new JFrame("Barcode Scanner"); // Создаём окно
        JTextField textField = new JTextField(20); // Поле для ввода с имитацией сканера

        textField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Игнорируем — обработка будет в keyPressed и keyReleased
            }

            @Override
            public void keyPressed(KeyEvent e) {
                long currentTime = System.currentTimeMillis();

                // Если между нажатиями прошло больше порога — начинаем новый штрих-код
                if (currentTime - lastKeyTime > THRESHOLD) {
                    barcode.setLength(0); // Очищаем предыдущий ввод
                }
                lastKeyTime = currentTime; // Обновляем время последнего нажатия
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Завершаем ввод при нажатии Enter
                    processBarcode(barcode.toString());
                    barcode.setLength(0); // Сбрасываем буфер
                } else {
                    // Добавляем символ в буфер
                    barcode.append(e.getKeyChar());
                }
            }
        });

        frame.add(textField); // Добавляем поле в окно
        frame.pack(); // Подгоняем размер окна
        frame.setVisible(true); // Отображаем окно
    }

    // Метод обработки полученного штрих-кода
    private void processBarcode(String barcode) {
        System.out.println("Отсканирован штрих-код: " + barcode);
        // Здесь может быть логика поиска товара, обновления интерфейса и т.д.
    }
}