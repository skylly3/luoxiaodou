package com.hiflying.blelink;

import android.util.Log;

import java.nio.ByteBuffer;

public class TeaEncryptor {

    private static final String TAG = "TeaEncryptor";

    public static byte[] encrypt(byte[] plain, String key) throws Exception {

        if (plain == null) {
            throw new Exception("plain cannot be null");
        }

        if (key == null) {
            throw new Exception("key cannot be null");
        }

        byte[] keyBytes = key.getBytes();
        if (keyBytes.length < 16) {
            throw new Exception("The key must have length larger than 16");
        }

        int[] keyInts = GTransformer.bytes2ints(keyBytes);
        int plainLength = plain.length;
        byte[] plain2Ints = new byte[(plainLength / 8) * 8];
        System.arraycopy(plain, 0, plain2Ints, 0, plain2Ints.length);
        int[] plainInts = GTransformer.bytes2ints(plain2Ints);

        ByteBuffer resultBuffer = ByteBuffer.allocate(plainLength);

        for (int i = 0; i < plainInts.length; i += 2) {

            int delta = 0x9e3779b9;
            int sum = 0;
            int a = plainInts[i];
            int b = plainInts[i + 1];

            for (int j = 0; j < 8; j++) {

                sum += delta;
                a += ((b << 4) + keyInts[0]) ^ (b + sum) ^ ((b >>> 5) + keyInts[1]);
                b += ((a << 4) + keyInts[2]) ^ (a + sum) ^ ((a >>> 5) + keyInts[3]);
            }

            resultBuffer.putInt(a);
            resultBuffer.putInt(b);
        }

        resultBuffer.put(plain, plain2Ints.length, plainLength - plain2Ints.length);
        byte[] encrypted = resultBuffer.array();

        Log.d(TAG, String.format("encrypt plain-%s key-%s encrypted-%s", GTransformer.bytes2HexStringWithWhitespace(plain), key,
                GTransformer.bytes2HexStringWithWhitespace(encrypted)));

        return  encrypted;
    }
}
