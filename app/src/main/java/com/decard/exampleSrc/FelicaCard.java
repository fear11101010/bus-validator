package com.decard.exampleSrc;

import static com.decard.exampleSrc.SAMCommandCodes.SAM_SUB_COMMAND_CODE_MUTUAL_AUTH_V2_RWSAM;

import android.util.Log;

import com.decard.NDKMethod.BasicOper;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public class FelicaCard {
    private byte[] iDm;
    private byte[] pMm;
    private byte[] idt;
    private byte[] idi;
    private byte[] systemCode;
    private final Sam sam;

    public FelicaCard(Sam sam){
        this.sam = sam;
        String[] result = BasicOper.dc_config_card(3).split("\\|");
        if(result[0].equals("0000")){
            Log.d("FelicaCard", "FelicaCard: Card config successfull--"+result[1]);
        }
    }
    public String detectFelicaCard(){
        String[] result = BasicOper.dc_FeliCaReset().split("\\|");
        if(result[0].equals("0000")){
            this.iDm = Arrays.copyOfRange(Utils.hexToByte(result[1]),0,8);
            this.pMm = Arrays.copyOfRange(Utils.hexToByte(result[1]),8,16);
            this.systemCode = Arrays.copyOfRange(Utils.hexToByte(result[1]),16,18);
        }
        return result[1];
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

    public int MutualAuthV2WithFeliCa(byte serviceCodeNum,
							 byte[] serviceCodeKeyVerList ) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, ShortBufferException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        long			_ret;

        byte[]	felicaCmdParams = new byte[256];
        int	felicaCmdParamsLen=0;

        byte[]	samResBuf = new byte[262];
        byte[] felicaCmd = new byte[262];
        byte[]       felicaRes = new byte[262];
        int[]	samResLen = new int[1];
        int[] felicaResLen = new int[1];
        int[] felicaCmdLen = new int[1];

        // Generate params sent to SAM
        System.arraycopy(iDm,0,felicaCmdParams,0,8);
//        memcpy(&felicaCmdParams[0],				IDm, 8);			// IDm

        felicaCmdParams[8] =						0x00;				// Reserved
        felicaCmdParams[8+1] =					0x03;				// Key Type(Node key, Diversification Code specified)
        felicaCmdParams[8+1+1] = systemCode[0];		// SystemCode(Big endian)
        felicaCmdParams[8+1+2] = systemCode[1];		// SystemCode(Big endian)
//        Rahul	memcpy(&_felica_cmd_params[8+1+1],			systemCode, 2);		// SystemCode(Big endian)
        felicaCmdParams[8+1+1+2] =				0x00;
        byte[] bytes = new byte[16];
        Arrays.fill(bytes,(byte) 0x00);
        System.arraycopy(bytes,0,felicaCmdParams,8+1+1+2+1,16);// Operation Parameter(No Diversification, AES128)
//        memset(&felicaCmdParams[8+1+1+2+1],		0x00, 16);			// Diversification code(All Zero)
        felicaCmdParams[8+1+1+2+1+16] =			serviceCodeNum;
        System.arraycopy(serviceCodeKeyVerList,0,felicaCmdParams,8+1+1+2+1+16+1,serviceCodeNum * 4);// Operation Parameter(No Diversification, AES128)
// Number of Service
//        memcpy(&felicaCmdParams[8+1+1+2+1+16+1], serviceCodeKeyVerList, serviceCodeNum * 4);	// Service Code List
        felicaCmdParamsLen = 8+1+1+2+1+16+1+serviceCodeNum*4;
        long felicaCmdRes = sam.askFeliCaCmdToSAMSC(SAMCommandCodes.SAM_COMMAND_CODE_MUTUAL_AUTH_V2_RWSAM,
                SAMCommandCodes.SAM_SUB_COMMAND_CODE_MUTUAL_AUTH_V2_RWSAM,felicaCmdParamsLen,
                felicaCmdParams,felicaCmdLen,felicaCmd);
        if(felicaCmdRes==0){
            return 0;
        }
        int felicaTransRes = transmitDataToFeliCaCard(felicaCmdLen[0],felicaCmd,
                felicaResLen,felicaRes);
        if(felicaTransRes==0){
            return 0;
        }
        int auth1V2Res = sam.sendAuth1V2ResultToSAM(felicaResLen[0],felicaRes,felicaCmdLen,felicaCmd);
        if(auth1V2Res==0){
            return 0;
        }
        felicaTransRes = transmitDataToFeliCaCard(felicaCmdLen[0],felicaCmd,
                felicaResLen,felicaRes);
        if(felicaTransRes==0){
            return 0;
        }

    }


    public  int transmitDataToFeliCaCard(int felicaCmdLen, byte[] felicaCmdBuf, int[] felicaResLen, byte[] felicaResBuf) {
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
        String[] result = BasicOper.dc_FeliCaApdu(Utils.byteToHex(sendBuf)).split("\\|");
//        ret = ctosFelicaReadWrite(sendBuf, sendLen, receiveBuf, receiveLen, 3000);

        if (!result[0].equals("0000")) {
//            tap_error = 1;
            return 0;
        }

        // HexDump(receiveLen, receiveBuf, "C <- Felica");
        if (result[1].length() < 10) {
            return 0;
        }

        System.arraycopy(Utils.hexToByte(result[1]), 1, felicaResBuf, 0, Utils.hexToByte(result[1]).length - 1);
        felicaResLen[0] = receiveLen - 1;

        return 1;
    }

}
