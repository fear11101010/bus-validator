package com.decard.exampleSrc;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.decard.exampleSrc.model.Route;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.IntStream;

public class Utils {
    public static final byte[] AUTH_KEY = {0x6C, (byte) 0xF9, (byte) 0xB1, (byte) 0xC8, 0x44, (byte) 0xC2, 0x6D, (byte) 0x9D, (byte) 0xA3, 0x0E, (byte) 0xF0, 0x62, 0x13, (byte) 0xC9, 0x75, (byte) 0xD1};
    public static Route route;

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
                "Abudullahpur,0,45,45,45,45,45,45,60,60,60,60,60,60\n" +
                "House Building,25,0,45,45,45,45,45,60,60,60,60,60,60\n" +
                "Azompur,25,25,0,45,45,45,45,60,60,60,60,60,60\n" +
                "Airport,25,25,25,0,40,40,40,55,55,55,55,55,60\n" +
                "Khilket,25,25,25,25,0,40,40,55,55,55,55,55,60\n" +
                "Banani,25,25,25,25,25,0,35,35,35,35,35,35,60\n" +
                "Farmgate,45,45,45,40,40,35,0,25,25,25,25,25,60\n" +
                "Shahabag,60,60,60,55,55,35,35,0,25,25,25,25,60\n" +
                "Press Club,60,60,60,55,55,35,35,35,0,25,25,25,60\n" +
                "Palton,60,60,60,55,55,35,35,35,35,0,25,25,60\n" +
                "Gulistan,60,60,60,55,55,35,35,35,35,35,0,25,60\n" +
                "Motijheel,60,60,60,55,55,35,35,35,35,35,35,0,60\n" +
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
                        stations[s].setMaxFare(Integer.parseInt(words[stations.length]));
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

    public static String convertByteArrayToBit(byte[] bytes){
        StringBuilder bitString = new StringBuilder();
        for (byte b : bytes) {
            bitString.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0')).append(" ");
        }
        return bitString.toString().trim();
    }


     /*public void readBlackListFile(Context context){
        FileInputStream inputStream = null;
        try {
            File file = new File(context.getFilesDir(), "Route_table_obj.dat");
            if (!file.exists()) {
                return;
            }
            inputStream = new FileInputStream(file);
            byte[] fileContent = new byte[(int) file.length()];
            int readStatus = inputStream.read(fileContent);
            inputStream.close();
            DateTimeFormatter formatter = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            }
                StringBuilder hexString = new StringBuilder();

                // Convert byte array to hex string
                for (byte b : fileContent) {
                    hexString.append(String.format("%02X", b));
                }

                String strBulkBL = hexString.toString();

                // Parse the data
                for (int i = 168; i < strBulkBL.length(); i += 40) {
                    if (i + 40 <= strBulkBL.length()) {

                        MasterBlacklist masterBlacklist = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            masterBlacklist = MasterBlacklist
                                    .builder()
                                    .cardIdUnique(strBulkBL.substring(i, i + 18))
                                    .registeredOn(LocalDateTime.parse(strBulkBL.substring(i + 18 + 6, i + 18 + 6 + 14), formatter))
                                    .reasonCode(strBulkBL.substring(i + (18 + 6 + 14), i + (18 + 6 + 14 + 2)))
                                    .build();
                        }
                        masterBlacklistList.add(masterBlacklist);
                    }
                }
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
        List<MasterBlacklist> masterBlacklistList = new ArrayList<>();

            log.info("Parsed {} blacklist entries", masterBlacklistList.size());

            truncateTable();

            masterBlacklistRepository.saveAll(masterBlacklistList);

            // Print the results
        } catch (IOException e) {
            log.error("{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }*/

    public static boolean commonValidationCheck(byte[] cardFunctionCode){
        char[] bits = convertByteArrayToBit(cardFunctionCode).toCharArray();
            return bits[15] == '1' && bits[14] == '1' && bits[11] == '0' && bits[10] == '0' && bits[9] == '0'
                    && (bits[8] == '0' || bits[8] == '1') && bits[6] == '1';
    }
    public static boolean isCardActive(byte statusFlag){
        return (statusFlag & (1 << 7)) != 0 && (statusFlag & (1 << 6)) == 0;
    }
    public static boolean isVoidCard(byte cardControlCode){
        return (cardControlCode & (1 << 7)) == 0 && (cardControlCode & (1 << 6)) == 0;
    }
    public static boolean isCardBlacklisted(byte cardControlCode){
        return (cardControlCode & (1 << 7)) == 0 ;
    }
}
