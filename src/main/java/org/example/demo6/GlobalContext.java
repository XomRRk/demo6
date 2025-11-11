package org.example.demo6;

public class GlobalContext {
    private static String currentSwitchType;
    private static String currentSerialNumber;

    public static String getCurrentSwitchType() {
        return currentSwitchType;
    }

    public static void setCurrentSwitchType(String switchType) {
        currentSwitchType = switchType;
    }

    public static String getCurrentSerialNumber() {
        return currentSerialNumber;
    }

    public static void setCurrentSerialNumber(String serialNumber) {
        currentSerialNumber = serialNumber;
    }
}
