package com.decard.exampleSrc;

import androidx.annotation.NonNull;

public class Utils {
    public static final byte[] AUTH_KEY = {0x6C,(byte)0xF9,(byte)0xB1,(byte)0xC8,0x44,(byte)0xC2,0x6D,(byte)0x9D,(byte)0xA3,0x0E,(byte)0xF0,0x62,0x13,(byte)0xC9,0x75,(byte)0xD1};
    public static String byteToHex(byte[] bytes){
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b:bytes){
            String hex = Integer.toHexString(0xFF & b);
            if(hex.length()==1){
                stringBuilder.append("0");
            }
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }

    public static byte[] hexToByte(@NonNull String hex){
        byte[] bytes = new byte[hex.length()/2];
        for(int i=0;i<hex.length();i+=2){
            bytes[i/2] = (byte) ((Character.digit(hex.charAt(i),16)<<4)+
                    Character.digit(hex.charAt(i+1),16));
        }
        return bytes;
    }
}
