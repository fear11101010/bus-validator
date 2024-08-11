package com.decard.exampleSrc;

public class SAMCommandCodes {
    public static final byte SAM_COMMAND_CODE_MUTUAL_AUTH_RWSAM = (byte)0xE2;
    public static final byte SAM_COMMAND_CODE_MUTUAL_AUTH_V2_RWSAM = (byte)0xE4;
    public static final byte SAM_SUB_COMMAND_CODE_MUTUAL_AUTH_V2_RWSAM = (byte)0x80;
    public static final byte SAM_COMMAND_CODE_MUTUAL_AUTH_V2 = (byte)0x96;
    public static final byte SAM_COMMAND_CODE_MUTUAL_AUTH = (byte)0x86;
    public static final byte SAM_SUB_COMMAND_CODE_REQUEST_SERVICE_V2 = (byte)0x8A;
    public static final byte SAM_SUB_COMMAND_CODE_REGISTER_AREA_V2 = (byte)0x02;
    public static final byte SAM_SUB_COMMAND_CODE_REQUEST_SERVICE_V2_RWSAM = (byte)0x8A;

    public static final byte SAM_COMMAND_CODE_REGISTER_AREA_V2 = (byte)0xB6;
    public static final byte SAM_COMMAND_CODE_REGISTER_AREA = (byte)0xC2;
    public static final byte SAM_COMMAND_CODE_REQUEST_SERVICE_V2_EX = (byte)0xD2;
    public static final byte SAM_COMMAND_CODE_REQUEST_SERVICE = (byte)0x82;
    public static final byte SAM_COMMAND_CODE_POLLING = (byte)0x80;
    public static final byte SAM_RESPONSE_CODE_POLLING = (byte)0x81;

    public static final byte SAM_RESPONSE_CODE_READ_WO_ENC = (byte)0x99;

    public static final byte SAM_COMMAND_CODE_WRITE = (byte)0x8A;
    public static final byte SAM_COMMAND_CODE_WRITE_WO_ENC = (byte)0x9A;
    public static final byte SAM_COMMAND_CODE_READ = (byte)0x88;
    public static final byte SAM_COMMAND_CODE_READ_WO_ENC = (byte)0x98;

    public static final byte SAM_COMMAND_CODE_REG_ISSUE_IDEX_V2 = (byte)0xB6;
    public static final byte SAM_COMMAND_CODE_REG_ISSUE_IDEX = (byte)0xC6;
    public static final byte SAM_COMMAND_CODE_REG_ISSUE_ID = (byte)0xC0;
    public static final byte SAM_SUB_COMMAND_CODE_REGISTER_SERVICE_V2 = (byte)0x04;
    public static final byte SAM_COMMAND_CODE_REGISTER_SERVICE_V2 = (byte)0xB6;
    public static final byte SAM_COMMAND_CODE_REGISTER_SERVICE = (byte)0xC4;
}
