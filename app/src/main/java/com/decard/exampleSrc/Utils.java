package com.decard.exampleSrc;

import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

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
    public static int charArrayToIntLE(byte[] data,int len){

        /*int result = 0;
        for(int i=len-1;i>=0;i--){
            result += data[i];
            if(i!=0){
                result = result << 8;
            }
        }
        return result;*/
        Log.d("charArrayToIntLE", Arrays.toString(data));
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        if(len>=4) {
            return byteBuffer.getInt();
        } else{
            return byteBuffer.getShort();
        }
    }
    public static void intToCharArrayLE(int in, byte[] out)
    {
        Arrays.fill(out,(byte)0x00);
        out[0] = (byte)in;
        in = in>> 8;
        out[1] = (byte)in;
        in = in>> 8;
        out[2] = (byte)in;
        in = in>> 8;
        out[3] = (byte)in;
    }
    public static byte[] intToCharArrayLE(int in)
    {
        Log.d("intToCharArrayLE", in+"");
        byte[] out = new byte[4];
        Arrays.fill(out,(byte)0x00);
        out[0] = (byte)in;
        in = in>> 8;
        out[1] = (byte)in;
        in = in>> 8;
        out[2] = (byte)in;
        in = in>> 8;
        out[3] = (byte)in;
        return out;
    }
    /*public static byte[] intToCharArrayLE(int in)
    {
        byte[] out = new byte[4];
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(in);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.get(out,0,4);
        return out;
    }*/

    public static Map<String,String> getYearMonthDateHourMinute(){
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Dhaka");
        calendar.setTimeZone(timeZone);
        Log.d("calenderYear", calendar.get(Calendar.YEAR)+"");
        Log.d("calenderMonth", (calendar.get(Calendar.MONTH)+1)+"");
        Log.d("calenderDay", calendar.get(Calendar.DATE)+"");
        Log.d("calenderMinute", calendar.get(Calendar.MINUTE)+"");
        Map<String,String> map = new HashMap<>();
        map.put("year",String.format("%7s",Integer.toBinaryString(calendar.get(Calendar.YEAR)%100)).replace(" ","0"));
        map.put("month",String.format("%4s",Integer.toBinaryString(calendar.get(Calendar.MONTH)+1)).replace(" ","0"));
        map.put("day",String.format("%5s",Integer.toBinaryString(calendar.get(Calendar.DATE))).replace(" ","0"));
        map.put("hour",String.format("%5s",Integer.toBinaryString(calendar.get(Calendar.HOUR_OF_DAY))).replace(" ","0"));
        map.put("minute",String.format("%6s",Integer.toBinaryString(calendar.get(Calendar.MINUTE))).replace(" ","0"));

        return map;
    }
}
