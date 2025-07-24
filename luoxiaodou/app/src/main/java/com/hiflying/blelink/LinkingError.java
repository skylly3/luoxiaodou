package com.hiflying.blelink;

public enum LinkingError {
    /**
     * The bluetooth adapter is disabled
     */
    BLUETOOTH_DISABLED,
    /**
     * The's no valid wifi connection
     */
    NO_VALID_WIFI_CONNECTION,
    /**
     * Ble device not found
     */
    BLE_NOT_FOUND,
    /**
     * Connect ble device found
     */
    CONNECT_BLE_FAILED,
    /**
     * Config ble device found
     */
    CONFIG_BLE_FAILED,
    /**
     * Not find any linked modules
     */
    FIND_DEVICE_FAILED
}
