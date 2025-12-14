package Kass;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import jssc.SerialPortEventListener;

public class JSSCScanner {
    private static final String PORT_NAME = "COM3";
    private static final int BAUD_RATE = 9600;

    public static void main(String[] args) {
        SerialPort serialPort = new SerialPort(PORT_NAME);

        try {
            if (!serialPort.isOpened()) {
                serialPort.openPort();
                serialPort.setParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            }

            serialPort.addEventListener(event -> {
                if (event.isRXCHAR() && event.getEventValue() > 0) {
                    try {
                        String barcode = serialPort.readString(event.getEventValue());
                        System.out.println("Штрих-код: " + barcode.trim());
                    } catch (SerialPortException ex) {
                        System.err.println("Ошибка при чтении данных с порта: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            });

        } catch (SerialPortException e) {
            System.err.println("Ошибка инициализации последовательного порта: " + e.getMessage());
            e.printStackTrace();
        }
    }
}