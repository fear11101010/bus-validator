package com.decard.exampleSrc;

import android.util.Log;

import com.decard.NDKMethod.BasicOper;
import com.decard.exampleSrc.model.AttributeInfo;
import com.decard.exampleSrc.model.EPurseInfo;
import com.decard.exampleSrc.model.FelicaCardDetail;
import com.decard.exampleSrc.model.GateAccessLog;
import com.decard.exampleSrc.model.GeneralInfo;
import com.decard.exampleSrc.model.HistoryRecord;
import com.decard.exampleSrc.model.IssuerInfo;
import com.decard.exampleSrc.model.OperatorInfo;
import com.decard.exampleSrc.model.PersonalInfo;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import lombok.Getter;

public class FelicaCard {
    private final Sam sam;
    private byte[] iDm;
    private byte[] pMm;
    private byte[] idt;
    private byte[] idi;
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
        }
        return result[2];
    }

    public int iWaitForAndAnalyzeFeliCa() throws Exception {
        byte[] readData = new byte[256];
        int[] readLen = new int[1];

        int ret = readFiles(readData, readLen);
        if (ret == 0) {
            return 0;
        }
        populateFelicaCard(Arrays.copyOfRange(readData,3,readData.length),readLen[0]-3);
        return 1;
    }

    private void populateFelicaCard(byte[] data, int len){
       byte[] bytes = Arrays.copyOfRange(data,0,len-2);
        IssuerInfo issuerInfo = IssuerInfo.builder().binCardIssuerID(Arrays.copyOfRange(bytes,0,2))
                .binIssuerEquipmentClass(bytes[2]).binInitializerId(bytes[3])
                .binCardIssueDate(Arrays.copyOfRange(bytes,4,6))
                .binCardRevision(bytes[6]).binRecycleCounter(bytes[7])
                .binReserved(Arrays.copyOfRange(bytes,8,8+8)).build();
        PersonalInfo personalInfo = PersonalInfo.builder().binName(Arrays.copyOfRange(bytes,16,16+48))
                .binPhone(Arrays.copyOfRange(bytes,16+48,16+48+6))
                .binBirthday(Arrays.copyOfRange(bytes,16+48+6,16+48+6+2))
                .binEmployeeNumber(Arrays.copyOfRange(bytes,16+48+6+2,16+48+6+2+4))
                .binPersonalAttrib(bytes[16+48+6+2+4])
                .binReserved(Arrays.copyOfRange(bytes,16+48+6+2+4+1,16+48+6+2+4+1+3))
                .build();
        AttributeInfo attributeInfo = AttributeInfo.builder().binCardFunctionCode(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3,16+48+6+2+4+1+3+2))
                .binCardControlCode(bytes[16+48+6+2+4+1+3+2])
                .binDiscountCode(bytes[16+48+6+2+4+1+3+2+1])
                .binExpiryDate(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1,16+48+6+2+4+1+3+2+1+1+2))
                .binTxnDataId(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2,16+48+6+2+4+1+3+2+1+1+2+2))
                .binReplacedCardId(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2,16+48+6+2+4+1+3+2+1+1+2+2+8))
                .binMerchandizeManagementCode(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8,16+48+6+2+4+1+3+2+1+1+2+2+8+8))
                .binNegativeValue(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2))
                .rechargeType(bytes[16+48+6+2+4+1+3+2+1+1+2+2+8+8+2])
                .binReserved(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+1,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+1+5))
                .build();
        EPurseInfo ePurseInfo =  EPurseInfo.builder()
                .binRemainingSV(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4))
                .binCashbackData(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4))
                .binCompoundData(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5))
                .binPaymentMethod(bytes[16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5])
                .binExecutionId(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2))
                .build();
        OperatorInfo operatorInfo = OperatorInfo.builder()
                .binOperatorCode(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2))
                .binEquipmentClass(bytes[16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2])
                .binStationCode(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2))
                .binEquipmentLocation(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2))
                .binActivationDate(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2))
                .binStatusFlag(bytes[16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2])
                .binPaymentMethod(bytes[16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1])
                .binDepositAmount(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+1+1,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2))
                .binReserved(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+1+1+2,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3))
                .build();
        HistoryRecord historyRecord = HistoryRecord.builder()
                .binEquipmentClass(bytes[16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3])
                .binServiceClass(bytes[16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1])
                .binContextCode(bytes[16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1])
                .binPaymentMethod(bytes[16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1])
                .binDate(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2))
                .binTime(bytes[16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2])
                .binPlace1(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2))
                .binPlace2(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2))
                .binCardBalance(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3))
                .binSVLogId(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3+2))
                .build();
        GateAccessLog gateAccessLog = GateAccessLog.builder()
                .binStatus(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3+2,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3+2+2))
                .binDate(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3+2+2,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3+2+2+2))
                .binTime(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3+2+2+2,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3+2+2+2+2))
                .binStationCode(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3+2+2+2+2,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3+2+2+2+2+2))
                .binEquipmentLocation(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3+2+2+2+2+2,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3+2+2+2+2+2+2))
                .binAmountBaseFare(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3+2+2+2+2+2+2,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3+2+2+2+2+2+2+3))
                .binAmountDistanceFare(Arrays.copyOfRange(bytes,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3+2+2+2+2+2+2+3,16+48+6+2+4+1+3+2+1+1+2+2+8+8+2+6+4+4+5+1+2+2+1+2+2+2+1+1+2+3+1+1+1+1+2+1+2+2+3+2+2+2+2+2+2+3+3))
                .build();

        GeneralInfo generalInfo = GeneralInfo.builder()
                .binCardID(idi)
                .binReCycleCounter(issuerInfo.getBinRecycleCounter())
                .lngRemainingSV(Utils.charArrayToIntLE(ePurseInfo.getBinRemainingSV(),4))
                .lngCashBackData(Utils.charArrayToIntLE(ePurseInfo.getBinCashbackData(),4))
                .intNegativeValue((short) Utils.charArrayToIntLE(attributeInfo.getBinNegativeValue(),2))
                .build();
        if((attributeInfo.getBinCardFunctionCode()[0] & (byte)0xFE)==(byte)0xC0){
            generalInfo.setBinCardType((byte)0); // RAPIDPASS_SVC_CARD
        }
        else if(((attributeInfo.getBinCardFunctionCode()[1] & (byte)0x30)==(byte)0x10) &&((attributeInfo.getBinCardFunctionCode()[0] & (byte)0xFE)==(byte)0x02)){
            generalInfo.setBinCardType((byte)10); // RAPIDPASS_OPERATOR_CARD
        }
        else if(((attributeInfo.getBinCardFunctionCode()[1] & (byte)0x30)==(byte)0x20) &&((attributeInfo.getBinCardFunctionCode()[0] & (byte)0xFE)==(byte)0x02)){
            generalInfo.setBinCardType((byte)11); // RAPIDPASS_NEXT_CARD
        }
        else if(((attributeInfo.getBinCardFunctionCode()[1] & (byte)0x30)==(byte)0x30) &&((attributeInfo.getBinCardFunctionCode()[0] & (byte)0xFE)==(byte)0x02)){
            generalInfo.setBinCardType((byte)12); // RAPIDPASS_PREV_CARD
        }
        else{
            generalInfo.setBinCardType((byte)-1);
        }

        this.felicaCardDetail = new FelicaCardDetail(generalInfo,issuerInfo,personalInfo,
                attributeInfo,ePurseInfo,operatorInfo,historyRecord,gateAccessLog);
        return;

    }

    public byte[] getIDm() {
        return iDm;
    }

    public byte[] getPMm() {
        return pMm;
    }

    public byte[] getSystemCode() {
        return systemCode;
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
        String[] result = BasicOper.dc_FeliCaApdu(Utils.byteToHex(Arrays.copyOfRange(sendBuf, 0, sendLen))).split("\\|", -1);
//        ret = ctosFelicaReadWrite(sendBuf, sendLen, receiveBuf, receiveLen, 3000);

        if (!result[0].equals("0000")) {
//            tap_error = 1;
            return 0;
        }

        byte[] hexToByte = Utils.hexToByte(result[1]);
        // HexDump(receiveLen, receiveBuf, "C <- Felica");
        if (hexToByte.length < 10) {
            return 0;
        }
        receiveLen = hexToByte.length;
        System.arraycopy(hexToByte, 1, felicaResBuf, 0, receiveLen-1);
        felicaResLen[0] = receiveLen - 1;

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
        System.arraycopy(blockList, 0, felicaCmdParams, 2+1, blockNum * 2);
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
        serviceNum = 7;
        serviceList[0] = 0x0A; //Issuer info file
        serviceList[1] = 0x11; //Issuer info file
        serviceList[2] = 0x01; //Service key ver
        serviceList[3] = 0x00; //Service key ver
        serviceList[4] = 0x0A; //Personal info file
        serviceList[5] = 0x12; //Personal info file
        serviceList[6] = 0x01; //Service key ver
        serviceList[7] = 0x00; //Service key ver
        serviceList[8] = 0x0A; //Card attrib info file
        serviceList[9] = 0x13; //Card attrib info file
        serviceList[10] = 0x01; //Service key ver
        serviceList[11] = 0x00; //Service key ver
        serviceList[12] = 0x10; //Direct Access of purse
        serviceList[13] = 0x14; //Direct Access of purse
        serviceList[14] = 0x01; //Service key ver
        serviceList[15] = 0x00; //Service key ver
        serviceList[16] = 0x0A; //Operator info file
        serviceList[17] = 0x21; //Operator info file
        serviceList[18] = 0x01; //Service key ver
        serviceList[19] = 0x00; //Service key ver
        serviceList[20] = 0x0C; //History file
        serviceList[21] = 0x22; //History file
        serviceList[22] = 0x01; //Service key ver
        serviceList[23] = 0x00; //Service key ver
        serviceList[24] = 0x0C; //Gate access History file
        serviceList[25] = 0x25; //gate access History file
        serviceList[26] = 0x01; //Service key ver
        serviceList[27] = 0x00; //Service key ver

        int ret = mutualAuthV2WithFeliCa(serviceNum, serviceList);
        if (ret == 0) {
            return 0;
        }

        blockNum = 11;
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

        ret = readDataBlock(blockNum, blockList, plngReadLen, pbinReadData);
        if (ret == 0) {
            return 0;
        }
        return 1;
    }


    public int updateBalance() throws Exception {
        byte serviceNum, blockNum;
        byte[] serviceList = new byte[64], blockList = new byte[128],blockData = new byte[256];
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

        blockNum = 3;
        blockList[0] = (byte) 0x80;    // file 1, attribute
        blockList[1] = 0x00;    //0th block

        blockList[4] = (byte) 0x81;    //file 2, ePurse
        blockList[5] = 0x00;    //0th block

        blockList[6] = (byte) 0x82;    //file 3, stored value log
        blockList[7] = 0x00;    //0th block


        EPurseInfo ePurseInfo = this.felicaCardDetail.getEPurseInfo();
        ePurseInfo.setBinRemainingSV(new byte[]{0x64,0x00, 0x00, 0x00});

        System.arraycopy(ePurseInfo.getData(),0,blockData,0, 16);
        ret = writeBlockData(blockNum,blockNum*2,blockList,blockData);
        if(ret==0){
            return 0;
        }
        return 1;
    }

    private int writeBlockData(int blockNum,int blockLen,byte[] blockList,byte[] blockData) throws Exception {
        long _ret;

        byte[] felicaCmdParams = new byte[256];
        int felicaCmdParamsLen = 0;

        byte[] samResBuf = new byte[262];
        byte[] felicaCmd = new byte[262];
        byte[] felicaRes = new byte[262];
        int[] samResLen = new int[1];
        int[] felicaResLen = new int[1];
        int[] felicaCmdLen = new int[1];
        felicaCmdParams[0] =						this.idt[0];						// this.idt
        felicaCmdParams[1] =						this.idt[1];						// IDt
        felicaCmdParams[2] =						(byte)blockNum;					// Number of Blocks
        System.arraycopy(blockList,0,			felicaCmdParams,3, blockLen);		// block list
        System.arraycopy(blockData,0,			felicaCmdParams,3+blockLen, blockNum*16);		// block list
        felicaCmdParamsLen = 3 + blockLen + blockNum*16;

        _ret = sam.askFeliCaCmdToSAM(SAMCommandCodes.SAM_COMMAND_CODE_WRITE,felicaCmdParamsLen,felicaCmdParams,felicaCmdLen,felicaCmd);
        if(_ret==0){
            return 0;
        }

        _ret = transmitDataToFeliCaCard(felicaCmdLen[0]-2,felicaCmd,felicaResLen,felicaRes);
        if(_ret==0){
            return 0;
        }
        _ret = sam.sendCardResultToSAM(felicaResLen[0],felicaRes,samResLen,samResBuf);
        if(_ret==0){
            return 0;
        }

        return 1;
    }

}
