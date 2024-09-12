package com.decard.exampleSrc;

import org.junit.Test;

import static org.junit.Assert.*;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        SimpleDateFormat sf = new SimpleDateFormat("HH");
        String substring = sf.format(new Date()).substring(0, 2);
        String hour = String.format("%5s", Integer.toBinaryString(Integer.parseInt(substring))).replace(" ","0");

        int parseInt = Integer.parseInt(hour + "000", 2);
        String hexString = String.format("%02X",parseInt);
        byte[] bytes = Utils.hexToByte(hexString);
        Log.d("hexString", hexString);
    }

    @Test
    public void getDate(){
        int len = "1006012e30d4b6153b61010F22018000".length();
        System.out.println(len);
    }
}