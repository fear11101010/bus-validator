package com.decard.exampleSrc.desfire.util;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Build;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {

    public static String removeAllNonAlphaNumeric(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll("[^A-Za-z0-9]", "");
    }

    public static int mod(int x, int y)
    {
        int result = x % y;
        return result < 0? result + y : result;
    }

    // position is 0 based starting from right to left
    public static byte setBitInByte(byte input, int pos) {
        return (byte) (input | (1 << pos));
    }

    // position is 0 based starting from right to left
    public static byte unsetBitInByte(byte input, int pos) {
        return (byte) (input & ~(1 << pos));
    }

    // https://stackoverflow.com/a/29396837/8166854
    public static boolean testBit(byte b, int n) {
        int mask = 1 << n; // equivalent of 2 to the nth power
        return (b & mask) != 0;
    }

    // https://stackoverflow.com/a/29396837/8166854
    public static boolean testBit(byte[] array, int n) {
        int index = n >>> 3; // divide by 8
        int mask = 1 << (n & 7); // n modulo 8
        return (array[index] & mask) != 0;
    }

    public static String printData(String dataName, byte[] data) {
        int dataLength;
        String dataString = "";
        if (data == null) {
            dataLength = 0;
            dataString = "IS NULL";
        } else {
            dataLength = data.length;
            dataString = bytesToHex(data);
        }
        StringBuilder sb = new StringBuilder();
        sb
                .append(dataName)
                .append(" length: ")
                .append(dataLength)
                .append(" data: ")
                .append(dataString);
        return sb.toString();
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuffer result = new StringBuffer();
        for (byte b : bytes)
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    public static String bytesToHexNpe(byte[] bytes) {
        if (bytes == null) return "";
        StringBuffer result = new StringBuffer();
        for (byte b : bytes)
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    public static String bytesToHexNpeUpperCase(byte[] bytes) {
        if (bytes == null) return "";
        StringBuffer result = new StringBuffer();
        for (byte b : bytes)
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString().toUpperCase();
    }

    public static String bytesToHexNpeUpperCaseBlank(byte[] bytes) {
        if (bytes == null) return "";
        StringBuffer result = new StringBuffer();
        for (byte b : bytes)
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1)).append(" ");
        return result.toString().toUpperCase();
    }

    public static String byteToHex(Byte input) {
        return String.format("%02X", input);
        //return String.format("0x%02X", input);
    }

    public static char intToUpperNibble(int input) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        //int v = input & 0xFF; // Cast byte to int, treating as unsigned value
        int v = input;
        return hexArray[v >>> 4]; // Select hex character from upper nibble
    }

    public static char byteToUpperNibble(Byte input) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        int v = input & 0xFF; // Cast byte to int, treating as unsigned value
        return hexArray[v >>> 4]; // Select hex character from upper nibble
    }

    public static char byteToLowerNibble(Byte input) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        int v = input & 0xFF; // Cast byte to int, treating as unsigned value
        return hexArray[v & 0x0F]; // Select hex character from lower nibble
    }

    public static byte nibblesToByte(char upperNibble, char lowerNibble) {
        String data = String.valueOf(upperNibble) + String.valueOf(lowerNibble);
        byte[] byteArray = hexStringToByteArray(data);
        return byteArray[0];
    }

    public static int byteToUpperNibbleInt(Byte input) {
        return (input & 0xF0 ) >> 4;
    }

    public static int byteToLowerNibbleInt(Byte input) {
        return input & 0x0F;
    }

    public static byte[] hexStringToByteArray(String s) {
        try {
            int len = s.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
            }
            return data;
        } catch (Exception e) {
            return null;
        }
    }


/*
    public static int byteToInt(byte b) {
        return (int) b & 0xFF;
    }
    public static byte intToByte(int i) { return (byte) (i & 0xff);}
*/
/*
see https://stackoverflow.com/questions/7401550/how-to-convert-int-to-unsigned-byte-and-back

Java 8 provides Byte.toUnsignedInt to convert byte to int by unsigned conversion.
In Oracle's JDK this is simply implemented as return ((int) x) & 0xff;
because HotSpot already understands how to optimize this pattern, but it could be
intrinsified on other VMs. More importantly, no prior knowledge is needed to
understand what a call to toUnsignedInt(foo) does.

In total, Java 8 provides methods to convert byte and short to unsigned int and long,
and int to unsigned long. A method to convert byte to unsigned short was deliberately
omitted because the JVM only provides arithmetic on int and long anyway.

To convert an int back to a byte, just use a cast: (byte)someInt. The resulting
narrowing primitive conversion will discard all but the last 8 bits.

A byte is always signed in Java. You may get its unsigned value by binary-anding it with 0xFF, though:

int i = 234;
byte b = (byte) i;
System.out.println(b); // -22
int i2 = b & 0xFF;
System.out.println(i2); // 234
 */



    public static String getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result + "";
    }

    public static String printByteBinary(byte bytes){
        byte[] data = new byte[1];
        data[0] = bytes;
        return printByteArrayBinary(data);
    }

    public static String printByteArrayBinary(byte[] bytes){
        String output = "";
        for (byte b1 : bytes){
            String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
            //s1 += " " + Integer.toHexString(b1);
            //s1 += " " + b1;
            output = output + " " + s1;
            //System.out.println(s1);
        }
        return output;
    }

    // conversion from www.java2s.com
    // http://www.java2s.com/example/java-utility-method/byte-array-to-char-index-0.html
    private char[] convertByteArrayToCharArray(byte[] bytes) {
        char[] buffer = new char[bytes.length >> 1];
        for (int i = 0; i < buffer.length; i++) {
            int bpos = i << 1;
            char c = (char) (((bytes[bpos] & 0x00FF) << 8) + (bytes[bpos + 1] & 0x00FF));
            buffer[i] = c;
        }
        return buffer;
    }

    // http://www.java2s.com/example/java-utility-method/char-to-byte-array-index-0.html
    private byte[] convertCharArrayToByteArray(char[] buffer) {
        byte[] b = new byte[buffer.length << 1];
        for (int i = 0; i < buffer.length; i++) {
            int bpos = i << 1;
            b[bpos] = (byte) ((buffer[i] & 0xFF00) >> 8);
            b[bpos + 1] = (byte) (buffer[i] & 0x00FF);
        }
        return b;
    }

    /**
     * Reverse a byte Array (e.g. Little Endian -> Big Endian).
     * Hmpf! Java has no Array.reverse(). And I don't want to use
     * Commons.Lang (ArrayUtils) from Apache....
     *
     * @param array The array to reverse (in-place).
     */
    public static void reverseByteArrayInPlace(byte[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            byte temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

    // converts an int to a 2 byte long array inversed = LSB
    public static byte[] intTo2ByteArrayInversed(int value) {
        return new byte[]{
                (byte) value,
                (byte) (value >> 8)};
    }

    public static int intFrom2ByteArrayInversed(byte[] bytes) {
        return  ((bytes[1] & 0xFF) << 8 ) |
                ((bytes[0] & 0xFF) << 0 );
    }

    // bytes are signed value, e.g. 0x83 would - without this method - return -125
    public static int intFromByteUnsigned(byte value) {
        return Byte.toUnsignedInt(value);
    }

    // converts an int to a 3 byte long array
    public static byte[] intTo3ByteArray(int value) {
        return new byte[] {
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value};
    }

    // converts an int to a 3 byte long array inversed
    public static byte[] intTo3ByteArrayInversed(int value) {
        return new byte[] {
                (byte)value,
                (byte)(value >> 8),
                (byte)(value >> 16)};
    }

    public static int intFrom3ByteArrayInversed(byte[] bytes) {
        return  ((bytes[2] & 0xFF) << 16) |
                ((bytes[1] & 0xFF) << 8 ) |
                ((bytes[0] & 0xFF) << 0 );
    }

    private int byteArrayLength3InversedToInt(byte[] data) {
        return (data[2] & 0xff) << 16 | (data[1] & 0xff) << 8 | (data[0] & 0xff);
    }
    /**
     * Returns a byte array with length = 4
     * @param value
     * @return
     */
    public static byte[] intToByteArray4(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }

    // Little Endian = LSB order
    public static byte[] intTo4ByteArrayInversed(int myInteger){
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(myInteger).array();
    }

    // packing an array of 4 bytes to an int, big endian, minimal parentheses
    // operator precedence: <<, &, |
    // when operators of equal precedence (here bitwise OR) appear in the same expression, they are evaluated from left to right
    public static int intFromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    /// packing an array of 4 bytes to an int, big endian, clean code
    public static int intFromByteArrayV3(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF) << 0 );
    }

    public static int byteArrayLength4NonInversedToInt(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    //
    public static int byteArrayLength4InversedToInt(byte[] bytes) {
        return bytes[3] << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
    }

    public static int intFrom4ByteArrayInversed(byte[] bytes) {
        return bytes[3] << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
    }

    public static byte[] longTo8Bytes(long l) {
        final int LongBYTES = 8;
        byte[] result = new byte[LongBYTES];
        for (int i = LongBYTES - 1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= Byte.SIZE;
        }
        return result;
    }

    public static long byte8ArrayToLong(final byte[] b) {
        final int LongBYTES = 8;
        long result = 0;
        for (int i = 0; i < LongBYTES; i++) {
            result <<= Byte.SIZE;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    /**
     * splits a byte array in chunks
     *
     * @param source
     * @param chunksize
     * @return a List<byte[]> with sets of chunksize
     */
    public static List<byte[]> divideArrayToList(byte[] source, int chunksize) {
        List<byte[]> result = new ArrayList<byte[]>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunksize);
            result.add(Arrays.copyOfRange(source, start, end));
            start += chunksize;
        }
        return result;
    }


    /**
     * checks if a byte array is part of the List<byte[]>
     * @param arrays
     * @param other
     * @return true on success
     */
    public static boolean listContains(List<byte[]> arrays, byte[] other) {
        for (byte[] b : arrays)
            if (Arrays.equals(b, other)) return true;
        return false;
    }

    // gives an 19 byte long timestamp yyyy.MM.dd HH:mm:ss
    public static String getTimestamp() {
        // gives a 19 character long string
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return ZonedDateTime
                    .now(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));
        } else {
            return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
        }
    }

    /**
     * Important: as Instant is available on Android 26+ you need to add a backwards library to run this on older Android versions
     */
    /*
    we need to setup an option in build.gradle (app)_
    ...
    compileOptions {
        // Flag to enable support for the new language APIs, important for timestamps
        coreLibraryDesugaringEnabled true
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    ...
    dependencies {
        [...]
        coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:1.0.9'
        [...]
    }
     */

    public static byte[] getActualInstant8Bytes() {
        return longTo8Bytes(getActualInstant());
    }
    public static long getActualInstant() {
        Instant instant = Instant.now();
        return instant.getEpochSecond();
    }

    /**
     * the getTimestampString methods return a String like '2023 09 07 16:27:23' or '20230907 165047',
     * inputs can be a <long> timestamp or <byte[]> timestamp8Bytes
     */

    public static String getTimestampString19Chars(byte[] timestamp8Bytes) {
        if ((timestamp8Bytes == null) || (timestamp8Bytes.length != 8)) return "";
        return getTimestampString19Chars(byte8ArrayToLong(timestamp8Bytes));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimestampString19Chars(long timestamp) {
        java.sql.Date date = new java.sql.Date(timestamp * 1000);
        return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(date);
    }

    public static String getTimestampString15Chars(byte[] timestamp8Bytes) {
        if ((timestamp8Bytes == null) || (timestamp8Bytes.length != 8)) return "";
        return getTimestampString15Chars(byte8ArrayToLong(timestamp8Bytes));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimestampString15Chars(long timestamp) {
        java.sql.Date date = new java.sql.Date(timestamp * 1000);
        return new SimpleDateFormat("yyyyMMdd HHmmss").format(date);
    }

    // the following methods work with ZonedDateTime

    // the ZoneId can be 'ZoneId utcTimeZone = ZoneId.of("UTC");'
    // or get the device's ZoneId: getDevicesZoneId();

    public static byte[] getActualZonedDateTime8Bytes() {
        return longTo8Bytes(getActualZonedDateTime(getDevicesZoneId()));
    }

    public static byte[] getActualZonedDateTime8Bytes(ZoneId zoneId) {
        return longTo8Bytes(getActualZonedDateTime(zoneId));
    }
    public static long getActualZonedDateTime(ZoneId zoneId) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        return zonedDateTime.toEpochSecond();

    }

    // returns for Europe/Germany Summertime: '07.09.23 17:32'
    public static String getZoneDatedStringShortDefault(byte[] instant8Bytes) {
        return getZoneDatedStringShort(instant8Bytes, getDevicesZoneId(), getDevicesLocale());
    }

    // returns for Europe/Germany Summertime: '07.09.2023 17:32:37'
    public static String getZoneDatedStringMediumDefault(byte[] instant8Bytes) {
        return getZoneDatedStringMedium(instant8Bytes, getDevicesZoneId(), getDevicesLocale());
    }

    // returns for Europe/Germany Summertime: '7. September 2023 17:32:37 MESZ'
    public static String getZoneDatedStringLongDefault(byte[] instant8Bytes) {
        return getZoneDatedStringLong(instant8Bytes, getDevicesZoneId(), getDevicesLocale());
    }

    // returns for Europe/Germany Summertime: 'Donnerstag, 7. September 2023 17:32:37 Mitteleuropaeische Sommerzeit'
    public static String getZoneDatedStringFullDefault(byte[] instant8Bytes) {
        return getZoneDatedStringFull(instant8Bytes, getDevicesZoneId(), getDevicesLocale());
    }


    public static String getZoneDatedStringShort(byte[] instant8Bytes, ZoneId zoneId, Locale locale) {
        if ((instant8Bytes == null) || (instant8Bytes.length != 8)) return "";
        return getZoneDatedStringShort(byte8ArrayToLong(instant8Bytes), zoneId, locale);
    }

    public static String getZoneDatedStringShort(long instantLong, ZoneId zoneId, Locale locale) {
        Instant instant = Instant.ofEpochSecond(instantLong);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(locale);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
        return zonedDateTime.format(formatter);
    }

    public static String getZoneDatedStringMedium(byte[] instant8Bytes, ZoneId zoneId, Locale locale) {
        if ((instant8Bytes == null) || (instant8Bytes.length != 8)) return "";
        return getZoneDatedStringMedium(byte8ArrayToLong(instant8Bytes), zoneId, locale);
    }

    public static String getZoneDatedStringMedium(long instantLong, ZoneId zoneId, Locale locale) {
        Instant instant = Instant.ofEpochSecond(instantLong);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(locale);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
        return zonedDateTime.format(formatter);
    }

    public static String getZoneDatedStringLong(byte[] instant8Bytes, ZoneId zoneId, Locale locale) {
        if ((instant8Bytes == null) || (instant8Bytes.length != 8)) return "";
        return getZoneDatedStringLong(byte8ArrayToLong(instant8Bytes), zoneId, locale);
    }

    public static String getZoneDatedStringLong(long instantLong, ZoneId zoneId, Locale locale) {
        Instant instant = Instant.ofEpochSecond(instantLong);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
                .withLocale(locale);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
        return zonedDateTime.format(formatter);
    }

    public static String getZoneDatedStringFull(byte[] instant8Bytes, ZoneId zoneId, Locale locale) {
        if ((instant8Bytes == null) || (instant8Bytes.length != 8)) return "";
        return getZoneDatedStringFull(byte8ArrayToLong(instant8Bytes), zoneId, locale);
    }

    public static String getZoneDatedStringFull(long instantLong, ZoneId zoneId, Locale locale) {
        Instant instant = Instant.ofEpochSecond(instantLong);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                .withLocale(locale);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
        return zonedDateTime.format(formatter);
    }

    // get the  device's ZoneId
    private static ZoneId getDevicesZoneId() {
        return ZoneId.systemDefault();
    }

    // this is the locale of the Android device, not the app
    private static Locale getDevicesLocale(){
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            locale = Resources.getSystem().getConfiguration().locale;
        }
        return locale;
    }

    public static boolean isValidUrl(String url) {
        if (url.length() == 0) return false;
        try {
            // it will check only for scheme and not null input
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

    public static String removeTrailingSlashes(String s) {
        return s.replaceAll("/+$", "");
    }


    public static byte[] generateTestData(int length) {
        /**
         * this method will generate a byte array of size 'length' and will hold a byte sequence
         * 00 01 .. FE FF 00 01 ..
         */
        // first generate a basis array
        byte[] basis = new byte[256];
        for (int i = 0; i < 256; i++) {
            basis[i] = (byte) (i & 0xFF);
        }
        // second copying the basis array to the target array
        byte[] target = new byte[length];
        if (length < 256) {
            target = Arrays.copyOfRange(basis, 0, length);
            return target;
        }
        // now length is > 256 so we do need multiple copies
        int numberOfChunks = length / 256;
        int dataLoop = 0;
        for (int i = 0; i < numberOfChunks; i++) {
            System.arraycopy(basis, 0, target, dataLoop, 256);
            dataLoop += 256;
        }
        // if some bytes are missing we are copying now
        if (dataLoop < length) {
            System.arraycopy(basis, 0, target, dataLoop, length - dataLoop);
        }
        return target;
    }

    public static String parseTextrecordPayload(byte[] ndefPayload) {
        int languageCodeLength = Array.getByte(ndefPayload, 0);
        int ndefPayloadLength = ndefPayload.length;
        byte[] languageCode = new byte[languageCodeLength];
        System.arraycopy(ndefPayload, 1, languageCode, 0, languageCodeLength);
        byte[] message = new byte[ndefPayloadLength - 1 - languageCodeLength];
        System.arraycopy(ndefPayload, 1 + languageCodeLength, message, 0, ndefPayloadLength - 1 - languageCodeLength);
        return new String(message, StandardCharsets.UTF_8);
    }

    /**
     * NFC Forum "URI Record Type Definition"<p>
     * This is a mapping of "URI Identifier Codes" to URI string prefixes,
     * per section 3.2.2 of the NFC Forum URI Record Type Definition document.
     */
    // source: https://github.com/skjolber/ndef-tools-for-android
    public static final String[] URI_PREFIX_MAP = new String[] {
            "", // 0x00
            "http://www.", // 0x01
            "https://www.", // 0x02
            "http://", // 0x03
            "https://", // 0x04
            "tel:", // 0x05
            "mailto:", // 0x06
            "ftp://anonymous:anonymous@", // 0x07
            "ftp://ftp.", // 0x08
            "ftps://", // 0x09
            "sftp://", // 0x0A
            "smb://", // 0x0B
            "nfs://", // 0x0C
            "ftp://", // 0x0D
            "dav://", // 0x0E
            "news:", // 0x0F
            "telnet://", // 0x10
            "imap:", // 0x11
            "rtsp://", // 0x12
            "urn:", // 0x13
            "pop:", // 0x14
            "sip:", // 0x15
            "sips:", // 0x16
            "tftp:", // 0x17
            "btspp://", // 0x18
            "btl2cap://", // 0x19
            "btgoep://", // 0x1A
            "tcpobex://", // 0x1B
            "irdaobex://", // 0x1C
            "file://", // 0x1D
            "urn:epc:id:", // 0x1E
            "urn:epc:tag:", // 0x1F
            "urn:epc:pat:", // 0x20
            "urn:epc:raw:", // 0x21
            "urn:epc:", // 0x22
    };

    public static String parseUrirecordPayload(byte[] ndefPayload) {
        int uriPrefix = Array.getByte(ndefPayload, 0);
        int ndefPayloadLength = ndefPayload.length;
        byte[] message = new byte[ndefPayloadLength - 1];
        System.arraycopy(ndefPayload, 1, message, 0, ndefPayloadLength - 1);
        return URI_PREFIX_MAP[uriPrefix] + new String(message, StandardCharsets.UTF_8);
    }


 }
