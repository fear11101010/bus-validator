package com.decard.exampleSrc.ui;


import com.decard.exampleSrc.desfire.util.Utils;

/**
 * This class holds all the constants used in the Test Environment
 * Warning: DO NOT STORE KEYS IN PLAIN - always use a secure keystore for this purpose
 */

public class Constants {

    /**
     * keystore
     */

    public static char[] KEYSTORE_PASSWORD = "changeit".toCharArray();

    /**
     * applications
     */

    public static final byte[] MASTER_APPLICATION_IDENTIFIER = Utils.hexStringToByteArray("000000"); // AID '00 00 00'
    public static final byte[] APPLICATION_IDENTIFIER_AES = Utils.hexStringToByteArray("A1A2A3"); // AID 'A1 A2 A3'



    /**
     * files
     */

    // standard 00, 01, 02
    public static final byte STANDARD_FILE_PLAIN_NUMBER = (byte) 0x00;
    public static final byte STANDARD_FILE_MACED_NUMBER = (byte) 0x01;
    public static final byte STANDARD_FILE_FULL_NUMBER = (byte) 0x02;
    // backup 03, 04, 05
    public static final byte BACKUP_FILE_PLAIN_NUMBER = (byte) 0x03;
    public static final byte BACKUP_FILE_MACED_NUMBER = (byte) 0x04;
    public static final byte BACKUP_FILE_FULL_NUMBER = (byte) 0x05;
    // value files 06, 07, 08
    public static final byte VALUE_FILE_PLAIN_NUMBER = (byte) 0x06; // 06
    public static final byte VALUE_FILE_MACED_NUMBER = (byte) 0x07; // 07
    public static final byte VALUE_FILE_FULL_NUMBER = (byte) 0x08; // 08
    // linear record 09, 10, 11
    public static final byte LINEAR_RECORD_FILE_PLAIN_NUMBER = (byte) 0x09; // 09
    public static final byte LINEAR_RECORD_FILE_MACED_NUMBER = (byte) 0x0A; // 10
    public static final byte LINEAR_RECORD_FILE_FULL_NUMBER = (byte) 0x0B; // 11
    // cyclic record 12, 13, 14
    public static final byte CYCLIC_RECORD_FILE_PLAIN_NUMBER = (byte) 0x0C; // 12
    public static final byte CYCLIC_RECORD_FILE_MACED_NUMBER = (byte) 0x0D; // 13
    public static final byte CYCLIC_RECORD_FILE_FULL_NUMBER = (byte) 0x0E; // 14

    public static final byte[] FILE_ACCESS_RIGHTS_DEFAULT = Utils.hexStringToByteArray("1234"); // RW key 01, CAR key 02, R key 03, W key 04

    /**
     * keys
     */

    public static final byte MASTER_APPLICATION_KEY_NUMBER = (byte) 0x00;
    public static final byte[] MASTER_APPLICATION_KEY_DES_DEFAULT = Utils.hexStringToByteArray("0000000000000000");
    public static final byte[] MASTER_APPLICATION_KEY_DES = Utils.hexStringToByteArray("D000000000000000");
    public static final byte[] MASTER_APPLICATION_KEY_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    public static final byte[] MASTER_APPLICATION_KEY_AES = Utils.hexStringToByteArray("99000000000000000000000000000000");
    public static final byte[] APPLICATION_KEY_MASTER_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    public static final byte[] APPLICATION_KEY_MASTER_AES = Utils.hexStringToByteArray("A0000000000000000000000000000000");
    public static final byte APPLICATION_KEY_MASTER_NUMBER = (byte) 0x00;
    public static final byte[] APPLICATION_KEY_RW_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    public static final byte[] APPLICATION_KEY_RW_AES = Utils.hexStringToByteArray("A1000000000000000000000000000000");
    public static final byte APPLICATION_KEY_RW_NUMBER = (byte) 0x01;
    public static final byte[] APPLICATION_KEY_CAR_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    public static final byte[] APPLICATION_KEY_CAR_AES = Utils.hexStringToByteArray("A2000000000000000000000000000000");
    public static final byte APPLICATION_KEY_CAR_NUMBER = (byte) 0x02;
    public static final byte[] APPLICATION_KEY_R_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    public static final byte[] APPLICATION_KEY_R_AES = Utils.hexStringToByteArray("A3000000000000000000000000000000");
    public static final byte APPLICATION_KEY_R_NUMBER = (byte) 0x03;
    public static final byte[] APPLICATION_KEY_W_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    public static final byte[] APPLICATION_KEY_W_AES = Utils.hexStringToByteArray("A4000000000000000000000000000000");

    public static final byte APPLICATION_KEY_W_NUMBER = (byte) 0x04;
    public static final int APPLICATION_NUMBER_OF_KEYS_DEFAULT = 5; // master, rw, car, r, w

    /**
     * NDEF application and files for SUN/SDM feature
     */

    public static final byte[] NDEF_APPLICATION_DF_NAME = Utils.hexStringToByteArray("D2760000850101");
    public static final byte NDEF_CONTAINER_FILE_NUMBER = (byte) 0x01;
    public static final byte NDEF_DATA_FILE_NUMBER = (byte) 0x02;

    /**
     * Transaction MAC feature
     */
    public static final byte[] TRANSACTION_MAC_KEY_AES = Utils.hexStringToByteArray("F7D23E0C44AFADE542BFDF2DC5C6AE02"); // taken from Mifare DESFire Light Features and Hints AN12343.pdf, pages 83-84
    public static final byte[] TRANSACTION_MAC_KEY_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");

    /**
     * constants for a DESFire Light simulation
     */

    // a DESFire Light tag has one pre defined application with these data
    public static final byte[] LIGHT_APPLICATION_IDENTIFIER_AES = Utils.hexStringToByteArray("E1E2E3"); // AID 'E1 E2 E3'
    public static final byte[] LIGHT_APPLICATION_DF_NAME_DEFAULT = Utils.hexStringToByteArray("A00000039656434103F015400000000B"); // DESFire Light: only one application is  available
    public static final byte[] LIGHT_APPLICATION_ISO_FILE_ID_DEFAULT = Utils.hexStringToByteArray("DF01");
    public static final int LIGHT_APPLICATION_NUMBER_OF_KEYS = 5;
    // there are in total 6 pre-defined files on a DESFire Light tag:
    public static final byte LIGHT_STANDARD_FILE_00_FULL_NUMBER = (byte) 0x00;
    public static final byte[] LIGHT_STANDARD_FILE_00_FULL_ISO_FILE_ID = Utils.hexStringToByteArray("EF00");
    public static final byte[] LIGHT_FILE_ACCESS_RIGHTS_00 = Utils.hexStringToByteArray("301F"); // RW key || CAR key || R key || W key
    public static final byte LIGHT_STANDARD_FILE_04_FULL_NUMBER = (byte) 0x04;
    public static final byte[] LIGHT_STANDARD_FILE_04_FULL_ISO_FILE_ID = Utils.hexStringToByteArray("EF04");
    public static final byte[] LIGHT_FILE_ACCESS_RIGHTS_04 = Utils.hexStringToByteArray("3012"); // RW key || CAR key || R key || W key
    public static final byte LIGHT_STANDARD_FILE_31_PLAIN_NUMBER = (byte) 0x1F;
    public static final byte[] LIGHT_STANDARD_FILE_31_PLAIN_ISO_FILE_ID = Utils.hexStringToByteArray("EF1F");
    public static final byte[] LIGHT_FILE_ACCESS_RIGHTS_31 = Utils.hexStringToByteArray("30EF"); // RW key || CAR key || R key || W key
    public static final byte LIGHT_CYCLIC_RECORD_FILE_01_FULL_NUMBER = (byte) 0x01;
    public static final byte[] LIGHT_CYCLIC_RECORD_FILE_01_FULL_ISO_FILE_ID = Utils.hexStringToByteArray("EF01");
    public static final byte[] LIGHT_FILE_ACCESS_RIGHTS_01 = Utils.hexStringToByteArray("3012"); // RW key || CAR key || R key || W key
    public static final byte LIGHT_VALUE_FILE_03_FULL_NUMBER = (byte) 0x03; // no ISO_FILE_ID given
    public static final byte[] LIGHT_FILE_ACCESS_RIGHTS_03 = Utils.hexStringToByteArray("3012"); // RW key || CAR key || R key || W key
    public static final byte LIGHT_TRANSACTION_MAC_FILE_21_FULL_NUMBER = (byte) 0x15;
    public static final byte[] LIGHT_FILE_ACCESS_RIGHTS_21 = Utils.hexStringToByteArray("101F"); // RW key || CAR key || R key || W key



}
