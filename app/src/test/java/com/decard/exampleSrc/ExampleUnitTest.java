package com.decard.exampleSrc;

import org.junit.Test;

import static org.junit.Assert.*;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
    @Test
    public void downloadFile() throws IOException {
        URL url = new URL("http://192.168.191.168:5000/api/masterBlackList/file");

        // Open a connection to the URL
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");
        httpConn.setConnectTimeout(5000);
        httpConn.setReadTimeout(5000);

        // Add the authorization header
        httpConn.setRequestProperty("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJkZXZpY2VTZXJpYWxObyI6IkJWMDEiLCJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzI2OTk4NzE1LCJleHAiOjE3MjcwODUxMTV9.WNu6Cau4CuE4_my-KWSon6KqdemjXRKdZ8qiVMTeUns");

        // Check the response code
        int responseCode = httpConn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Open input stream to read the file
            InputStream inputStream = httpConn.getInputStream();
            byte[] buffer = new byte[4096];
            int bytesRead = -1;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                StringBuilder builder = new StringBuilder();
                for (byte b:buffer) {
                    builder.append(String.format("%02X",b));
                }
                System.out.println(builder.toString());

            }
        }
    }
}