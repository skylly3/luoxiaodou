package com.hiflying.blelink;

import android.util.Log;

/**
 * CRC calculator 
 */  
public class CRC {

    private static final String TAG = "CRC";


    /******************************************************************************
     * Name:    CRC-8/MAXIM         x8+x5+x4+1
     * Poly:    0x31
     * Init:    0x00
     * Refin:   True
     * Refout:  True
     * Xorout:  0x00
     * Alias:   DOW-CRC,CRC-8/IBUTTON
     * Use:     Maxim(Dallas)'s some devices,e.g. DS18B20
     *****************************************************************************/
    public static byte crc8Maxim(byte[] data) {

        byte crc = 0;
        for (int i = 0; i < data.length ; i++) {

            crc ^= data[i];

            for (int j = 0; j < 8; j++) {

                if ((crc & 0x01) == 0x01) {
                    crc = (byte) (((crc & 0xff ))>>> 1);
                    crc ^= 0x8C;
                }else {
                    crc = (byte) (((crc & 0xff ))>>> 1);
                }
            }
        }

        Log.d(TAG, String.format("crc8Maxim: data-%s crc-%s", GTransformer.bytes2HexStringWithWhitespace(data),
                GTransformer.bytes2HexStringWithWhitespace(new byte[]{crc})));

        return crc;
    }
}  