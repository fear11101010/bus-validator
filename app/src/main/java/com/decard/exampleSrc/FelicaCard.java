package com.decard.exampleSrc;

import android.util.Log;

import com.decard.NDKMethod.BasicOper;
import com.decard.exampleSrc.model.AttributeInfo;
import com.decard.exampleSrc.model.EPurseInfo;
import com.decard.exampleSrc.model.FelicaCardDetail;
import com.decard.exampleSrc.model.GateAccessLogInformation;
import com.decard.exampleSrc.model.GateAccessLogInformationForTransfer;
import com.decard.exampleSrc.model.GeneralInfo;
import com.decard.exampleSrc.model.IssuerInfo;
import com.decard.exampleSrc.model.OperatorInfo;
import com.decard.exampleSrc.model.PersonalInfo;
import com.decard.exampleSrc.model.ReadWriteInCard;
import com.decard.exampleSrc.model.StoredLogInformation;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;

public class FelicaCard implements ReadWriteInCard {
    private final Sam sam;
    private byte[] iDm;
    private byte[] pMm;
    private byte[] idt;
    private byte[] idi;
    @Getter
    private byte[] systemCode;
    @Getter
    private FelicaCardDetail felicaCardDetail;

    public FelicaCard(Sam sam) {
        this.sam = sam;
        String[] result = BasicOper.dc_config_card(3).split("\\|", -1);
        if (result[0].equals("0000")) {
            Log.d("FelicaCard", "FelicaCard: Card config successfull--" + result[1]);
        }
    }

    public String detectFelicaCard() {
        String[] result = BasicOper.dc_FeliCaReset().split("\\|", -1);
        if (result[0].equals("0000")) {
            this.iDm = Arrays.copyOfRange(Utils.hexToByte(result[2]), 0, 8);
            this.pMm = Arrays.copyOfRange(Utils.hexToByte(result[2]), 8, 16);
            this.systemCode = Arrays.copyOfRange(Utils.hexToByte(result[2]), 16, 18);
            Log.d("IDm", Utils.byteToHex(iDm));
        }
        return result[2];
    }

    public long pollingCard() throws Exception {
        long ret;

        byte[] felicaCmdParams = new byte[256];
        int felicaCmdParamsLen = 0;

        byte[] felicaCmd = new byte[255];
        byte[] felicaRes = new byte[262];
        byte[] workBuf = new byte[262];
        int[] felicaCmdLen = new int[1], felicaResLen = new int[1], workBufLen = new int[1];

        felicaCmdParams[0] = this.systemCode[0];  // System code
        felicaCmdParams[1] = this.systemCode[1];  // System code
        felicaCmdParams[2] = 0x00;            // Timeslot
        felicaCmdParamsLen = 3;

        ret = sam.askFeliCaCmdToSAM(SAMCommandCodes.SAM_COMMAND_CODE_POLLING, felicaCmdParamsLen, felicaCmdParams,
                felicaCmdLen, felicaCmd);
        if (ret == 0) return 0;

        ret = transmitDataToFeliCaCard(felicaCmdLen[0], felicaCmd, felicaResLen, felicaRes);
        if (ret == 0) return 0;

        return 1;


    }

    public int iWaitForAndAnalyzeFeliCa() throws Exception {
        byte[] readData = new byte[256];
        int[] readLen = new int[1];

        int ret = readFiles(readData, readLen);
        if (ret == 0) {
            return 0;
        }
        populateFelicaCard(Arrays.copyOfRange(readData, 3, readData.length), readLen[0] - 3);
        return 1;
    }

    private void populateFelicaCard(byte[] data, int len) {
        byte[] bytes = Arrays.copyOfRange(data, 0, len - 2);
        IssuerInfo issuerInfo = IssuerInfo.generateData(Arrays.copyOfRange(bytes, 0, 16));
        PersonalInfo personalInfo = PersonalInfo.generateData(Arrays.copyOfRange(bytes, 16, 16 * 5));
        AttributeInfo attributeInfo = AttributeInfo.generateData(Arrays.copyOfRange(bytes, 16 * 5, 16 * 7));
        EPurseInfo ePurseInfo = EPurseInfo.generateData(Arrays.copyOfRange(bytes, 16 * 7, 16 * 8));
        OperatorInfo operatorInfo = OperatorInfo.generateData(Arrays.copyOfRange(bytes, 16 * 8, 16 * 9));
        StoredLogInformation storedLogInformation = StoredLogInformation.generateData(Arrays.copyOfRange(bytes, 16 * 9, 16 * 10));
        GateAccessLogInformation gateAccessLogInformation = GateAccessLogInformation.generateData(Arrays.copyOfRange(bytes, 16 * 10, 16 * 11));
        GateAccessLogInformationForTransfer gateAccessLogInformationForTransfer = GateAccessLogInformationForTransfer.generateData(Arrays.copyOfRange(bytes, 16 * 11, 16 * 13));
        GeneralInfo generalInfo = GeneralInfo.builder()
                .binCardID(idi)
                .binReCycleCounter(issuerInfo.getRecycleCounter())
                .lngRemainingSV(Utils.charArrayToIntLE(ePurseInfo.getBinRemainingSV(), 4))
                .lngCashBackData(Utils.charArrayToIntLE(ePurseInfo.getBinCashbackData(), 4))
                .intNegativeValue((short) Utils.charArrayToIntLE(attributeInfo.getNegativeValue(), 2))
                .build();
        if ((attributeInfo.getCardFunctionCode()[0] & (byte) 0xFE) == (byte) 0xC0) {
            generalInfo.setBinCardType((byte) 0); // RAPIDPASS_SVC_CARD
        }
        else if (((attributeInfo.getCardFunctionCode()[1] & (byte) 0x30) == (byte) 0x10) && ((attributeInfo.getCardFunctionCode()[0] & (byte) 0xFE) == (byte) 0x02)) {
            generalInfo.setBinCardType((byte) 10); // RAPIDPASS_OPERATOR_CARD
        }
        else if (((attributeInfo.getCardFunctionCode()[1] & (byte) 0x30) == (byte) 0x20) && ((attributeInfo.getCardFunctionCode()[0] & (byte) 0xFE) == (byte) 0x02)) {
            generalInfo.setBinCardType((byte) 11); // RAPIDPASS_NEXT_CARD
        }
        else if (((attributeInfo.getCardFunctionCode()[1] & (byte) 0x30) == (byte) 0x30) && ((attributeInfo.getCardFunctionCode()[0] & (byte) 0xFE) == (byte) 0x02)) {
            generalInfo.setBinCardType((byte) 12); // RAPIDPASS_PREV_CARD
        }
        else {
            generalInfo.setBinCardType((byte) -1);
        }

        this.felicaCardDetail = new FelicaCardDetail(generalInfo, issuerInfo, personalInfo,
                attributeInfo, ePurseInfo, operatorInfo, storedLogInformation, gateAccessLogInformation,gateAccessLogInformationForTransfer);

    }

    public byte[] getIDm() {
        return iDm;
    }

    public byte[] getPMm() {
        return pMm;
    }

    public int mutualAuthV2WithFeliCa(byte serviceCodeNum,
                                      byte[] serviceCodeKeyVerList) throws Exception {
        long _ret;

        byte[] felicaCmdParams = new byte[256];
        int felicaCmdParamsLen = 0;

        byte[] samResBuf = new byte[262];
        byte[] felicaCmd = new byte[262];
        byte[] felicaRes = new byte[262];
        int[] samResLen = new int[1];
        int[] felicaResLen = new int[1];
        int[] felicaCmdLen = new int[1];

        // Generate params sent to SAM
        System.arraycopy(iDm, 0, felicaCmdParams, 0, 8); //IDm

        felicaCmdParams[8] = 0x00;                // Reserved
        felicaCmdParams[8 + 1] = 0x03;                // Key Type(Node key, Diversification Code specified)
        felicaCmdParams[8 + 1 + 1] = systemCode[0];        // SystemCode(Big endian)
        felicaCmdParams[8 + 1 + 2] = systemCode[1];        // SystemCode(Big endian)
        felicaCmdParams[8 + 1 + 1 + 2] = 0x00; // Operation Parameter(No Diversification, AES128)
        Arrays.fill(felicaCmdParams, 8 + 1 + 1 + 2 + 1, 8 + 1 + 1 + 2 + 1 + 16, (byte) 0x00);// Diversification code(All Zero)
        felicaCmdParams[8 + 1 + 1 + 2 + 1 + 16] = serviceCodeNum; // Number of Service
        System.arraycopy(serviceCodeKeyVerList, 0, felicaCmdParams, 8 + 1 + 1 + 2 + 1 + 16 + 1, serviceCodeNum * 4);// Service code list
        felicaCmdParamsLen = 8 + 1 + 1 + 2 + 1 + 16 + 1 + serviceCodeNum * 4;
        long felicaCmdRes = sam.askFeliCaCmdToSAMSC(SAMCommandCodes.SAM_COMMAND_CODE_MUTUAL_AUTH_V2_RWSAM,
                SAMCommandCodes.SAM_SUB_COMMAND_CODE_MUTUAL_AUTH_V2_RWSAM, felicaCmdParamsLen,
                felicaCmdParams, felicaCmdLen, felicaCmd);
//        Log.d("TAG", "mutualAuthV2WithFeliCa: ");
        if (felicaCmdRes == 0) {
            return 0;
        }
        int felicaTransRes = transmitDataToFeliCaCard(felicaCmdLen[0], felicaCmd,
                felicaResLen, felicaRes);
        if (felicaTransRes == 0) {
            return 0;
        }
        int auth1V2Res = sam.sendAuth1V2ResultToSAM(felicaResLen[0], felicaRes, felicaCmdLen, felicaCmd);
        if (auth1V2Res == 0) {
            return 0;
        }
        felicaTransRes = transmitDataToFeliCaCard(felicaCmdLen[0], felicaCmd,
                felicaResLen, felicaRes);
        if (felicaTransRes == 0) {
            return 0;
        }

        int samRes = sam.sendCardResultToSAM(felicaResLen[0], felicaRes, samResLen,
                samResBuf);
        if (samRes == 0) {
            return 0;
        }
        if (samResBuf[0] != (byte) 0x00) {
            return 0;
        }

        this.idi = new byte[8];
        System.arraycopy(samResBuf, 1, idi, 0, 8);

        this.idt = new byte[2];
        this.idt[0] = samResBuf[1 + 8 + 8];
        this.idt[1] = samResBuf[1 + 8 + 8 + 1];

        return 1;

    }

    public int transmitDataToFeliCaCard(int felicaCmdLen, byte[] felicaCmdBuf, int[] felicaResLen, byte[] felicaResBuf) {
        int ret = 0;
        byte[] sendBuf = new byte[262];
        byte[] receiveBuf = new byte[262];
        int sendLen, receiveLen;
        byte key;

        sendLen = felicaCmdLen + 1; // Length of FeliCa Command
        sendBuf[0] = (byte) sendLen;
        System.arraycopy(felicaCmdBuf, 0, sendBuf, 1, felicaCmdLen);
        receiveLen = 262;

        // HexDump(sendLen, sendBuf, "C -> Felica");
        Log.d("felicaCmd", Utils.byteToHex(Arrays.copyOfRange(sendBuf, 0, sendLen)));
        String[] result = BasicOper.dc_FeliCaApdu(Utils.byteToHex(Arrays.copyOfRange(sendBuf, 0, sendLen))).split("\\|", -1);
//        ret = ctosFelicaReadWrite(sendBuf, sendLen, receiveBuf, receiveLen, 3000);

        if (!result[0].equals("0000")) {
//            tap_error = 1;
            return 0;
        }

        Log.d("felicaCommandFinal", "felicaCommandFinal: " + result[1]);

        byte[] hexToByte = Utils.hexToByte(result[1]);
        // HexDump(receiveLen, receiveBuf, "C <- Felica");
        if (hexToByte.length < 10) {
            return 0;
        }
        receiveLen = hexToByte.length;
        System.arraycopy(hexToByte, 1, felicaResBuf, 0, receiveLen - 1);
        felicaResLen[0] = receiveLen - 1;

        return 1;
    }

    @Override
    public FelicaCard readData() throws Exception {
        if (iWaitForAndAnalyzeFeliCa() == 0) {
            return null;
        }
        return this;
    }

    @Override
    public int writeInCard(int serviceNum, byte[] serviceList, int blockNum, byte[] blockList, byte[] blockData) throws Exception {
        int ret = mutualAuthV2WithFeliCa((byte) serviceNum, serviceList);
        if (ret == 0) {
            return 0;
        }

        ret = writeBlockData(blockNum, blockNum * 2, blockList, blockData);
        if (ret == 0) {
            return 0;
        }
        return 1;
    }


    public int readDataBlock(byte blockNum, byte[] blockList, int[] readLen, byte[] readData) throws Exception {
        byte[] felicaCmdParams = new byte[256], felicaCmd = new byte[262], felicaRes = new byte[262];
        int felicaCmdParamsLen = 0;
        int[] felicaCmdLen = new int[1], felicaResLen = new int[1];

        // Generate params sent to sam
        felicaCmdParams[0] = idt[0];
        felicaCmdParams[1] = idt[1];
        felicaCmdParams[2] = blockNum;
        System.arraycopy(blockList, 0, felicaCmdParams, 2 + 1, blockNum * 2);
        felicaCmdParamsLen = 2 + 1 + blockNum * 2;
        int ret = (int) sam.askFeliCaCmdToSAM(SAMCommandCodes.SAM_COMMAND_CODE_READ, felicaCmdParamsLen, felicaCmdParams, felicaCmdLen, felicaCmd);
        if (ret == 0) {
            return 0;
        }
        ret = transmitDataToFeliCaCard(felicaCmdLen[0] - 2, felicaCmd, felicaResLen, felicaRes);
        if (ret == 0) {
            return 0;
        }
        ret = sam.sendCardResultToSAM(felicaResLen[0], felicaRes, readLen, readData);
        if (ret == 0) {
            return 0;
        }
        return 1;

    }


    public int readFiles(byte[] pbinReadData, int[] plngReadLen) throws Exception {
        byte serviceNum, blockNum;
        byte[] serviceList = new byte[64], blockList = new byte[128];
        int[] readLen = new int[1];
        serviceNum = 8;
        serviceList[0] = 0x0A; //Issuer info file
        serviceList[1] = 0x11; //Issuer info file
        serviceList[2] = 0x01; //Service key ver
        serviceList[3] = 0x00; //Service key ver
        serviceList[4] = 0x0A; //Personal info file
        serviceList[5] = 0x12; //Personal info file
        serviceList[6] = 0x01; //Service key ver
        serviceList[7] = 0x00; //Service key ver
        serviceList[8] = 0x0A; //Card attribute info file
        serviceList[9] = 0x13; //Card attribute info file
        serviceList[10] = 0x01; //Service key ver
        serviceList[11] = 0x00; //Service key ver
        serviceList[12] = 0x10; //EPurse info file
        serviceList[13] = 0x14; //EPurse info file
        serviceList[14] = 0x01; //Service key ver
        serviceList[15] = 0x00; //Service key ver
        serviceList[16] = 0x0A; //Operator info file
        serviceList[17] = 0x21; //Operator info file
        serviceList[18] = 0x01; //Service key ver
        serviceList[19] = 0x00; //Service key ver
        serviceList[20] = 0x0C; //Stored value log file
        serviceList[21] = 0x22; //Stored value log file
        serviceList[22] = 0x01; //Service key ver
        serviceList[23] = 0x00; //Service key ver
        serviceList[24] = 0x0C; //Gate access log file
        serviceList[25] = 0x25; //gate access log file
        serviceList[26] = 0x01; //Service key ver
        serviceList[27] = 0x00; //Service key ver
        serviceList[28] = 0x08; //Gate access log for transfer file
        serviceList[29] = 0x26; //gate access log for transfer file
        serviceList[30] = 0x01; //Service key ver
        serviceList[31] = 0x00; //Service key ver


        int ret = mutualAuthV2WithFeliCa(serviceNum, serviceList);
        if (ret == 0) {
            return 0;
        }

        blockNum = 13;
        blockList[0] = (byte) 0x80;    //file 1, issuer file
        blockList[1] = 0x00;    //0th block
        blockList[2] = (byte) 0x81;    //file 2, personal info
        blockList[3] = 0x00;    //0th block
        blockList[4] = (byte) 0x81;    //file 2, personal info
        blockList[5] = 0x01;    //1th block
        blockList[6] = (byte) 0x81;    //file 2, personal info
        blockList[7] = 0x02;    //2nd block
        blockList[8] = (byte) 0x81;    //file 2, personal info
        blockList[9] = 0x03;    //3rd block
        blockList[10] = (byte) 0x82;    //file 3, card attrib file
        blockList[11] = 0x00;    //0th block
        blockList[12] = (byte) 0x82;    //file 3, card attrib file
        blockList[13] = 0x01;    //1th block
        blockList[14] = (byte) 0x83;    //file 4, ePurse
        blockList[15] = 0x00;    //0th block
        blockList[16] = (byte) 0x84;    //file 5, Operator info file
        blockList[17] = 0x00;    //0th block
        blockList[18] = (byte) 0x85;    //file 6, History file
        blockList[19] = 0x00;    //0th block
        blockList[20] = (byte) 0x86;    //file 7, Gate Access Log file
        blockList[21] = 0x00;    //0th block
        blockList[22] = (byte) 0x87;    //file 7, Gate Access Log for transfer file
        blockList[23] = 0x00;    //0th block
        blockList[24] = (byte) 0x87;    //file 7, Gate Access Log for transfer file
        blockList[25] = 0x01;    //1th block

        ret = readDataBlock(blockNum, blockList, plngReadLen, pbinReadData);
        if (ret == 0) {
            return 0;
        }
        return 1;
    }


    public int updateBalance() throws Exception {
        byte serviceNum, blockNum;
        byte[] serviceList = new byte[64], blockList = new byte[128], blockData = new byte[256];
        int[] readLen = new int[1];
        serviceNum = 3;
        serviceList[0] = 0x08; //Card Attribute Information
        serviceList[1] = 0x13; //Card Attribute Information
        serviceList[2] = 0x01; //Service key ver
        serviceList[3] = 0x00; //Service key ver
        serviceList[4] = 0x10; //Direct Access of purse
        serviceList[5] = 0x14; //Direct Access of purse
        serviceList[6] = 0x01; //Service key ver
        serviceList[7] = 0x00; //Service key ver
        serviceList[8] = 0x0C; //Stored Value Log Information
        serviceList[9] = 0x22; //Stored Value Log Information
        serviceList[10] = 0x01; //Service key ver
        serviceList[11] = 0x00; //Service key ver

        int ret = mutualAuthV2WithFeliCa(serviceNum, serviceList);
        if (ret == 0) {
            return 0;
        }

        blockNum = 4;
        blockList[0] = (byte) 0x80;    // file 1, attribute
        blockList[1] = 0x00;    //0th block

        blockList[2] = (byte) 0x80;    // file 1, attribute
        blockList[3] = 0x01;    //1th block

        blockList[4] = (byte) 0x81;    //file 2, ePurse
        blockList[5] = 0x00;    //0th block

        blockList[6] = (byte) 0x82;    //file 3, stored value log
        blockList[7] = 0x00;    //0th block


//        EPurseInfo ePurseInfo = this.felicaCardDetail.getEPurseInfo();
        writeEPurseInfoForRecharge(this.felicaCardDetail.getEPurseInfo());
        writeAttributeInformationBlock(this.felicaCardDetail.getAttributeInfo());
        StoredLogInformation storedLogInformation = new StoredLogInformation();
        writeStoredLogInformationBlockForRecharge(storedLogInformation);

        System.arraycopy(this.getFelicaCardDetail().getAttributeInfo().getData(), 0, blockData, 0, 2 * 16);
        System.arraycopy(this.getFelicaCardDetail().getEPurseInfo().getData(), 0, blockData, 32, 16);
        System.arraycopy(storedLogInformation.getData(), 0, blockData, 32 + 16, 16);

        Log.d("blockdata", Arrays.toString(blockData));

        ret = writeBlockData(blockNum, blockNum * 2, blockList, blockData);
        if (ret == 0) {
            return 0;
        }
        return 1;
    }

    public void writeEPurseInfoForRecharge(EPurseInfo ePurseInfo) {
        int balance = Utils.charArrayToIntLE(ePurseInfo.getBinRemainingSV(), 4) + Utils.charArrayToIntLE(new byte[]{0x64, 0x00, 0x00, 0x00}, 4)
                - Utils.charArrayToIntLE(this.felicaCardDetail.getAttributeInfo().getNegativeValue(), 2);
//        byte[] newBalance = new byte[4];
        Log.d("new balance", balance + "");
        ePurseInfo.setBinRemainingSV(Utils.intToCharArrayLE(balance));
        Log.d("new balance in byte:", Arrays.toString(ePurseInfo.getBinRemainingSV()));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddhhmm", Locale.ENGLISH);
        String date = dateFormat.format(new Date());
        Map<String, String> map = Utils.getYearMonthDateHourMinute();

        // Generate year, month and data
        String year = String.format("%7s", Integer.toBinaryString(Integer.parseInt(date.substring(0, 2)))).replace(' ', '0');
        String month = String.format("%4s", Integer.toBinaryString(Integer.parseInt(date.substring(2, 4)))).replace(' ', '0');
        String day = String.format("%5s", Integer.toBinaryString(Integer.parseInt(date.substring(4, 6)))).replace(' ', '0');

        String dateInHex = String.format("%04X", Integer.parseInt(map.get("year") + map.get("month") + map.get("day"), 2));
        Log.d("dateInHex", dateInHex);

        // Generate hour , minute and region code
        String hour = String.format("%5s", Integer.toBinaryString(Integer.parseInt(date.substring(6, 8)))).replace(' ', '0');
        String minute = String.format("%6s", Integer.toBinaryString(Integer.parseInt(date.substring(8, 10)))).replace(' ', '0');
        String regionCode = String.format("%5s", Integer.toBinaryString(10)).replace(' ', '0');
        String regionAndHourAndMinuteInHex = String.format("%04X", Integer.parseInt(map.get("hour") + map.get("minute") + regionCode, 2));

        // Generate compound data 2+2+1
        byte[] compoundData = new byte[5];
        System.arraycopy(Utils.hexToByte(dateInHex), 0, compoundData, 0, 2);
        System.arraycopy(Utils.hexToByte(regionAndHourAndMinuteInHex), 0, compoundData, 2, 2);
        compoundData[4] = (byte) 0x01;

        // Set payment method
        ePurseInfo.setBinPaymentMethod((byte) 0x01);
        ePurseInfo.setBinCompoundData(compoundData);

        // Increment execution id by 1
        Log.d("executionId", Arrays.toString(ePurseInfo.getBinExecutionId()));
        int executionId = Integer.parseInt(Utils.byteToHex(ePurseInfo.getBinExecutionId()), 16) + 1;
        Log.d("executionId", String.format("%04x", executionId));
        ePurseInfo.setBinExecutionId(Utils.hexToByte(String.format("%04x", executionId)));
    }

    public void writeStoredLogInformationBlockForRecharge(StoredLogInformation storedLogInformation) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddhh", Locale.ENGLISH);
//        String date = dateFormat.format(new Date());

        // Generate year, month and data
//        String year = String.format("%7s",Integer.toBinaryString(Integer.parseInt(date.substring(0,2)))).replace(' ','0');
//        String month = String.format("%4s",Integer.toBinaryString(Integer.parseInt(date.substring(2,4)))).replace(' ','0');
//        String day = String.format("%5s",Integer.toBinaryString(Integer.parseInt(date.substring(4,6)))).replace(' ','0');
//        String hour = String.format("%5s",Integer.toBinaryString(Integer.parseInt(date.substring(6,8)))).replace(' ','0');
//
//        String dateInHex = Integer.toHexString(Integer.parseInt(year+month+day,2)).toUpperCase();
//        Log.d("dateInHex", dateInHex);

        Map<String, String> map = Utils.getYearMonthDateHourMinute();

        storedLogInformation.setEquipmentClassificationCode((byte) 0x25);
        storedLogInformation.setServiceClassificationCode((byte) 0x60);
        storedLogInformation.setContextCode((byte) 0x02);
        storedLogInformation.setPaymentMethodCode((byte) 0x01);
        Log.d("storedlogdate :", String.format("%04X", Integer.parseInt(map.get("year") + map.get("month") + map.get("day"), 2)));
        Log.d("storedlogdate in decimal:", Integer.parseInt(String.format("%04X", Integer.parseInt(map.get("year") + map.get("month") + map.get("day"), 2)), 16) + "");
        int i = Integer.parseInt(String.format("%04X", Integer.parseInt(map.get("year") + map.get("month") + map.get("day"), 2)), 16);

        storedLogInformation.setDate(Utils.hexToByte(String.format("%04X", Integer.parseInt(map.get("year") + map.get("month") + map.get("day"), 2))));
        storedLogInformation.setTime(Utils.hexToByte(String.format("%02X", Integer.parseInt(map.get("hour") + "000", 2)).toUpperCase())[0]);
        storedLogInformation.setPlace1(Utils.hexToByte("0105"));
        storedLogInformation.setPlace2(new byte[]{0x00, 0x00});
        storedLogInformation.setCardBalance(Arrays.copyOfRange(this.felicaCardDetail.getEPurseInfo().getBinRemainingSV(), 0, 3));
        storedLogInformation.setStoredValueLogId(this.felicaCardDetail.getEPurseInfo().getBinExecutionId());
    }

    public void writeAttributeInformationBlock(AttributeInfo attributeInfo) {
        String hex = Utils.byteToHex(attributeInfo.getTxnDataId());
        int i = Integer.parseInt(hex, 16);
        String newTxnId = String.format("%04X", i + 1);
        attributeInfo.setTxnDataId(Utils.hexToByte(newTxnId));
    }

    private int writeBlockData(int blockNum, int blockLen, byte[] blockList, byte[] blockData) throws Exception {
        long _ret;

        byte[] felicaCmdParams = new byte[256];
        int felicaCmdParamsLen = 0;

        byte[] samResBuf = new byte[262];
        byte[] felicaCmd = new byte[262];
        byte[] felicaRes = new byte[262];
        int[] samResLen = new int[1];
        int[] felicaResLen = new int[1];
        int[] felicaCmdLen = new int[1];
        felicaCmdParams[0] = this.idt[0];                        // this.idt
        felicaCmdParams[1] = this.idt[1];                        // IDt
        felicaCmdParams[2] = (byte) blockNum;                    // Number of Blocks
        System.arraycopy(blockList, 0, felicaCmdParams, 3, blockLen);        // block list
        System.arraycopy(blockData, 0, felicaCmdParams, 3 + blockLen, blockNum * 16);        // block list
        felicaCmdParamsLen = 3 + blockLen + blockNum * 16;

        _ret = sam.askFeliCaCmdToSAM(SAMCommandCodes.SAM_COMMAND_CODE_WRITE, felicaCmdParamsLen, felicaCmdParams, felicaCmdLen, felicaCmd);
        if (_ret == 0) {
            return 0;
        }

        _ret = transmitDataToFeliCaCard(felicaCmdLen[0] - 2, felicaCmd, felicaResLen, felicaRes);
        if (_ret == 0) {
            return 0;
        }
        _ret = sam.sendCardResultToSAM(felicaResLen[0], felicaRes, samResLen, samResBuf);
        if (_ret == 0) {
            return 0;
        }

        return 1;
    }

    public String readOpenBlock() {
        /*String[] cmd = new String[]{
            "10","06",Utils.byteToHex(this.iDm),"01","0F22","01"
        };
        String[] blockList = new String[]{"8000","8001","8002","8003","8004","8005","8006","8007","8008","8009","8010","8011",
                "8012","8013","8014","8015","8017","8018","8019"};*/

        String c = "40012E30D4B6153B6100070A110A120A1310140A210C220C251CC7E1731891F537EB9994E5929DF4F39000";
        byte[] b = new byte[255];
        byte[] b1 = Utils.hexToByte(c);
        b[0] = (byte) (b1.length + 1);
        System.arraycopy(b1, 0, b, 1, b1.length);
        String[] r = BasicOper.dc_FeliCaApdu(Utils.byteToHex(Arrays.copyOfRange(b, 0, b1.length + 1))).split("\\|", -1);
        if (Objects.equals(r[0], "0000")) {
            Log.d("jewel_vai", r[r.length - 1]);
            return r[r.length - 1];
        }
        /*StringBuilder builder = new StringBuilder();
        for(int i=0;i<20;i++) {
            String apdu = String.join("",cmd)+blockList[i];
            //
            //1006012e30d4b6153b61010F22018000
            String[] result = BasicOper.dc_FeliCaApdu(apdu).split("\\|", -1);
            if (result[0].equals("0000")) {
                builder.append(result[1]);
            }
        }*/
        return null;
    }

}
