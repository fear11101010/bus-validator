package com.decard.exampleSrc;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.decard.exampleSrc.model.Route;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.IntStream;

public class Utils {
    public static final byte[] AUTH_KEY = {0x6C, (byte) 0xF9, (byte) 0xB1, (byte) 0xC8, 0x44, (byte) 0xC2, 0x6D, (byte) 0x9D, (byte) 0xA3, 0x0E, (byte) 0xF0, 0x62, 0x13, (byte) 0xC9, 0x75, (byte) 0xD1};
    private static Route route;

    public static String byteToHex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                stringBuilder.append("0");
            }
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }

    public static byte[] hexToByte(@NonNull String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) +
                    Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }

    public static int charArrayToIntLE(byte[] data, int len) {

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
        if (len >= 4) {
            return byteBuffer.getInt();
        } else {
            return byteBuffer.getShort();
        }
    }

    public static void intToCharArrayLE(int in, byte[] out) {
        Arrays.fill(out, (byte) 0x00);
        out[0] = (byte) in;
        in = in >> 8;
        out[1] = (byte) in;
        in = in >> 8;
        out[2] = (byte) in;
        in = in >> 8;
        out[3] = (byte) in;
    }

    public static byte[] intToCharArrayLE(int in) {
        Log.d("intToCharArrayLE", in + "");
        byte[] out = new byte[4];
        Arrays.fill(out, (byte) 0x00);
        out[0] = (byte) in;
        in = in >> 8;
        out[1] = (byte) in;
        in = in >> 8;
        out[2] = (byte) in;
        in = in >> 8;
        out[3] = (byte) in;
        return out;
    }
    public static byte[] convertToTwosComplement(int in,int len)
    {
        int onesComplement = ~in;
        int twosComplement = onesComplement+1;
        byte[] bytes = intToCharArrayLE(twosComplement);
        return Arrays.copyOfRange(bytes,0,Math.min(4,len));
    }

    public static Map<String, String> getYearMonthDateHourMinute() {
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Dhaka");
        calendar.setTimeZone(timeZone);
        Log.d("calenderYear", calendar.get(Calendar.YEAR) + "");
        Log.d("calenderMonth", (calendar.get(Calendar.MONTH) + 1) + "");
        Log.d("calenderDay", calendar.get(Calendar.DATE) + "");
        Log.d("calenderMinute", calendar.get(Calendar.MINUTE) + "");
        Map<String, String> map = new HashMap<>();
        map.put("year", String.format("%7s", Integer.toBinaryString(calendar.get(Calendar.YEAR) % 100)).replace(" ", "0"));
        map.put("month", String.format("%4s", Integer.toBinaryString(calendar.get(Calendar.MONTH) + 1)).replace(" ", "0"));
        map.put("day", String.format("%5s", Integer.toBinaryString(calendar.get(Calendar.DATE))).replace(" ", "0"));
        map.put("hour", String.format("%5s", Integer.toBinaryString(calendar.get(Calendar.HOUR_OF_DAY))).replace(" ", "0"));
        map.put("minute", String.format("%6s", Integer.toBinaryString(calendar.get(Calendar.MINUTE))).replace(" ", "0"));

        return map;
    }

    public static void createFile(Context context) {
        String data = "Number of route,1,Last update,20170503,effective date,20170503,end date,20991231\n" +
                "Route name,Abudullahpur to Motijheel,number of bus stop,12\n" +
                "Abudullahpur,0,45,45,45,45,45,45,60,60,60,60,60,0,45,45,60\n" +
                "House Building,25,0,45,45,45,45,45,60,60,60,60,60,25,25,45,60\n" +
                "Azompur,25,25,0,45,45,45,45,60,60,60,60,60,25,25,45,60\n" +
                "Airport,25,25,25,0,40,40,40,55,55,55,55,55,25,25,40,55\n" +
                "Khilket,25,25,25,25,0,40,40,55,55,55,55,55,25,25,25,60\n" +
                "Banani,25,25,25,25,25,0,35,35,35,35,35,35,25,25,35,35\n" +
                "Farmgate,45,45,45,40,40,35,0,25,25,25,25,25,35,45,25,25\n" +
                "Shahabag,60,60,60,55,55,35,35,0,25,25,25,25,35,60,25,25\n" +
                "Press Club,60,60,60,55,55,35,35,35,0,25,25,25,35,60,25,25\n" +
                "Palton,60,60,60,55,55,35,35,35,35,0,25,25,35,60,25,25\n" +
                "Gulistan,60,60,60,55,55,35,35,35,35,35,0,25,35,60,25,25\n" +
                "Motijheel,60,60,60,55,55,35,35,35,35,35,35,0,35,60,0,35\n" +
                "number of record for route,14\n" +
                "number of record for file,16";
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput("Route_table_obj.dat", Context.MODE_PRIVATE);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.d("finally block", e.getMessage());
                }
            }
        }
    }

    public static void initializeRoute(Context context) {
        try {
            File file = new File(context.getFilesDir(), "Route_table_obj.dat");
            if (!file.exists()) {
                return;
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            route = new Route();
            readTotalNumberOfRoute(reader);
            String line = null;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                for (; i < route.getNumberOfRoute(); i++) {
                    readTotalNumberOfStation(line);
                    Route.Station[] stations = route.getStations().get(route.getRouteName().get(i));
                    assert stations != null;
                    for (int s=0;s<stations.length;s++) {
                        stations[s] = new Route.Station();
                        line = reader.readLine();
                        String[] words = line.split(",");
                        stations[s].setName(words[0]);
                        int[] fare = new int[stations.length];
                        for (int j = 0; j < stations.length; j++) {
                            fare[j] = Integer.parseInt(words[j + 1]);
                        }
                        stations[s].setPosition(s);
                        stations[s].setFare(fare);
                        stations[s].setCode(Route.STATION_CODES.get(words[0]));
                        stations[s].setMinUpStreamFare(Integer.parseInt(words[stations.length]));
                        stations[s].setMaxUpStreamFare(Integer.parseInt(words[stations.length + 1]));
                        stations[s].setMinDownStreamFare(Integer.parseInt(words[stations.length + 2]));
                        stations[s].setMaxDownStreamFare(Integer.parseInt(words[stations.length + 3]));
                    }
                }
            }
        } catch (Exception e) {
            Log.d("route_file_read_ex", e.getMessage());
            e.printStackTrace();
        }
    }

    private static void readTotalNumberOfRoute(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line != null) {
            String[] words = line.split(",");
            route.setNumberOfRoute(Integer.parseInt(words[1]));
        }
    }

    private static void readTotalNumberOfStation(String line) throws IOException {
        String[] words = line.split(",");
        route.getRouteName().add(words[1]);
        route.getStations().put(words[1], new Route.Station[Integer.parseInt(words[3])]);
    }

    public static int byteToInteger(byte[] bytes){
        String hex = byteToHex(bytes);
        return Integer.parseInt(hex,16);
    }
}
