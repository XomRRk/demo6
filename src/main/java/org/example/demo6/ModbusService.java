package org.example.demo6;

import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortFactoryPJC;
import com.intelligt.modbus.jlibmodbus.serial.SerialUtils;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class ModbusService {
    private static ModbusService instance;
    private ModbusMaster master;
    private SerialParameters serialParameters;
    private boolean connected = false;

    private ModbusService() {
        serialParameters = new SerialParameters();
        serialParameters.setDevice("/dev/ttyUSB1");  // Укажи свой порт, например "COM3" на Windows
        serialParameters.setBaudRate(SerialPort.BaudRate.BAUD_RATE_115200);
        serialParameters.setDataBits(8);
        serialParameters.setStopBits(1);
        serialParameters.setParity(SerialPort.Parity.NONE);

        SerialUtils.setSerialPortFactory(new SerialPortFactoryPJC());
    }

    public static synchronized ModbusService getInstance() {
        if (instance == null) {
            instance = new ModbusService();
        }
        return instance;
    }

    private void notifyError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка Modbus");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public synchronized void connect() {
        if (master == null || !connected) {
            try {
                master = ModbusMasterFactory.createModbusMasterRTU(serialParameters);
                master.setResponseTimeout(1000);
                master.connect();
                connected = true;
                System.out.println("Modbus connected");
            } catch (Exception e) {
                connected = false;
                notifyError("Не удалось подключиться к Modbus: " + e.getMessage());
            }
        }
    }

    public synchronized void disconnect() {
        if (master != null && connected) {
            try {
                master.disconnect();
                connected = false;
                System.out.println("Modbus disconnected");
            } catch (Exception e) {
                notifyError("Ошибка при отключении Modbus: " + e.getMessage());
            }
        }
    }

    public synchronized int[] readInputRegisters(int slaveId, int startAddress, int quantity) {
        if (!connected) {
            connect();
            if (!connected) {
                notifyError("Modbus не подключен, чтение невозможно");
                return new int[0];
            }
        }
        try {
            return master.readInputRegisters(slaveId, startAddress, quantity);
        } catch (Exception e) {
            connected = false;
            notifyError("Ошибка при чтении регистров: " + e.getMessage());
            return new int[0];
        }
    }

    public synchronized void writeSingleRegister(int slaveId, int address, int value) {
        if (!connected) {
            connect();
            if (!connected) {
                notifyError("Modbus не подключен, запись невозможна");
                return;
            }
        }
        try {
            master.writeSingleRegister(slaveId, address, value);
        } catch (Exception e) {
            connected = false;
            notifyError("Ошибка при записи регистра: " + e.getMessage());
        }
    }
}
