package com.decard.exampleSrc;

import android.text.TextUtils;
import android.util.Log;

import com.decard.NDKMethod.BasicOper;
import com.decard.driver.utils.HexDump;
import com.decard.exampleSrc.desfire.util.CMAC;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.params.KeyParameter;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Sam {
    private final static int BUFF_SIZE = 1500;
    private final int samSlot;
    private String rwSamNumber;
    private final byte[] rar = new byte[16];
    private byte[] rcr;
    private byte[] rbr;
    private byte[] snr;
    private byte[] kYtr;

    public Sam(int samSlot) {
        this.samSlot = samSlot;
    }

    public String initSam() {
        String[] result = BasicOper.dc_setcpu(samSlot).split("\\|", -1);
        Log.d("sam_init", String.format("code :%s,data :%s",result[0],result[1]));
        return result[0];
    }

    public String resetSam() {
        String[] result = BasicOper.dc_cpureset_hex().split("\\|", -1);
        Log.d("sam_reset", String.format("code :%s,data :%s",result[0],result[1]));
        return result[0];
    }

    public String setToNormalMode() {
        byte[] sendBuf = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xE6, (byte) 0x02, (byte) 0x02};
        Log.d("sam_setToNormalMode_sendBuff", "data: "+HexDump.dumpHexString(sendBuf));
        int responseLength = 0xFF;
        return this.transitDataToSam(sendBuf,responseLength);
    }

    public String sendAttention(){
        byte[] sendBuff = new byte[]{0x00,0x00,0x00,0x00,0x00,0x00};
        Log.d("sam_sendAttention", "sendAttention: "+Utils.byteToHex(sendBuff));
        int responseLength = 0xFF;
        String response = this.transitDataToSam(sendBuff,responseLength);
        assert response != null;
        byte[] bytes = Arrays.copyOfRange(HexDump.hexStringToByteArray(response),4,12);
        rwSamNumber = Utils.byteToHex(bytes);
        return response;
    }

    public String sendAuth1(){
        byte[] sendBuff = new byte[]{0x00,0x00,0x00,0x02,0x00,0x00};
        sendBuff = this.mergeArray(sendBuff,HexDump.hexStringToByteArray(rwSamNumber));
        SecureRandom secureRandom = new SecureRandom();
//        byte[] randomBytes =new byte[16];
        secureRandom.nextBytes(rar);
        sendBuff = this.mergeArray(sendBuff,rar);
        Log.d("sam_sendAuth1", "sendAuth1: "+Utils.byteToHex(sendBuff));
//        int responseLength = (1+2+1+32+4+2)*2;
        int responseLength = 0xFF;
        return this.transitDataToSam(sendBuff,responseLength);
    }

    public String checkAuth1Result(String auth1Response) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        byte[] kab = new byte[16];
        byte[] receivedRar = new byte[16];
        byte[] encryptedM2r = new byte[32];
        byte[] decryptedM2r = new byte[32];
        byte[] iv = new byte[16];

        encryptedM2r = Arrays.copyOfRange(HexDump.hexStringToByteArray(auth1Response),4,4+32);
        this.rcr = Arrays.copyOfRange(HexDump.hexStringToByteArray(auth1Response),4+32,4+32+4);
        for(int i=0;i<4;i++){
            kab[i] = (byte) (Utils.AUTH_KEY[i] ^ this.rcr[i]);
        }
        System.arraycopy(Utils.AUTH_KEY, 4, kab, 4, 12);
        SecretKeySpec secretKey = new SecretKeySpec(kab,"AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        Arrays.fill(iv, (byte) 0x00);
        Arrays.fill(decryptedM2r, (byte) 0x00);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE,secretKey,ivParameterSpec);
        decryptedM2r = cipher.doFinal(encryptedM2r);
        rbr = Arrays.copyOfRange(decryptedM2r,0,16);
        receivedRar = Arrays.copyOfRange(decryptedM2r,16,16+16);
        for(int i=0;i<16;i++){
            if(rar[i]!=receivedRar[i]){
                return null;
            }
        }
        return Utils.byteToHex(decryptedM2r);
    }

    public String sendAuth2() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] buf = new byte[32];
        byte[] kab = new byte[16];
        byte[] iv = new byte[16];
        byte[] m3r = new byte[32];
        byte[] sendBuf = new byte[]{0x00,0x00,0x00,0x04,0x00,0x00};
        buf = mergeArray(this.rar,this.rbr);
        for(int i=0;i<4;i++){
            kab[i] = (byte) (Utils.AUTH_KEY[i] ^ this.rcr[i]);
        }
        System.arraycopy(Utils.AUTH_KEY, 4, kab, 4, 12);
        SecretKeySpec secretKeySpec = new SecretKeySpec(kab,"AES");
        Arrays.fill(iv,(byte)0x00);
        Arrays.fill(m3r,(byte)0x00);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec,ivParameterSpec);
        m3r = cipher.doFinal(buf);
        sendBuf = mergeArray(sendBuf,HexDump.hexStringToByteArray(rwSamNumber));
        sendBuf = mergeArray(sendBuf,m3r);
        return this.transitDataToSam(sendBuf,0xFF);


    }

    public String checkAuth2Result(String auth2Result){
        byte[] bytes = HexDump.hexStringToByteArray(auth2Result);
        if(bytes[0]!=0x00){
            return null;
        }
        snr = new byte[]{0x01,0x00,0x00,0x00};
        kYtr = Arrays.copyOfRange(rbr,0,16);
        return Utils.byteToHex(snr);
    }


    private String transitDataToSam(byte[] sendBuf,int responseLength) {
        byte[] apdu = new byte[]{(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) sendBuf.length};
        Log.d("sam_transitDataToSam_apdu", "apdu: "+HexDump.dumpHexString(apdu));
        byte[] sAPDU = this.mergeArray(apdu, sendBuf);
        String hexAPDU = Utils.byteToHex(sAPDU).concat("00");
        String[] result = BasicOper.dc_TransmitApdu(0xFF,hexAPDU).split("\\|");
//        String[] result = BasicOper.dc_cpuapduInt_hex(hexAPDU).split("\\|");
        if (Objects.equals(result[0], "0000")) {
            byte[] rAPDU = HexDump.hexStringToByteArray(result[1]);
            if (rAPDU[rAPDU.length-2] == (byte)0x90 && rAPDU[rAPDU.length-1] == (byte)0x00) {
                return result[1];
            } else {

                return null;
            }
        } else {
            return null;
        }
    }


    public void calculateMacRaw(byte[] msg, int msgLen, byte[] mac) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
        int encSize;
        byte[] lastBlock = new byte[16];
        byte[] iv = new byte[16];
        Arrays.fill(iv,(byte) 0x00);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        SecretKeySpec keySpec = new SecretKeySpec(kYtr,"ASE");

        Cipher cipher = Cipher.getInstance("ASE/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivParameterSpec);

        if(msgLen==0) return;
        else if (msgLen % 16 == 0) {
            encSize = msgLen - 16;
            lastBlock = Arrays.copyOfRange(msg, encSize,16);
        } else {
            encSize = (msgLen/16) * 16;
            Arrays.fill(lastBlock,(byte) 0x00);
            System.arraycopy(msg,encSize,lastBlock,0,msgLen%16);
        }
        byte[] encryptedMsg = new byte[16];
        for (int i=0;i<encSize;i+=16){
            cipher.update(msg,i,i+16,encryptedMsg);
            ivParameterSpec = new IvParameterSpec(encryptedMsg);
            cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivParameterSpec);
        }
        cipher.doFinal(lastBlock,0,16,mac);
//        System.arraycopy(encryptedMsg,0,mac,0,16);

    }
    public void calculateMacUsingCMac(byte[] msg, int msgLen, byte[] mac) {
        BlockCipher aseEngine = AESEngine.newInstance();
        Mac cMac = new CMac(aseEngine);
        CipherParameters cipherParameters = new KeyParameter(kYtr);
        cMac.init(cipherParameters);
        cMac.update(msg,0,msgLen);
        cMac.doFinal(mac,0);
    }

    public void encryptData(byte command,byte subCommand,int felicaCommandLength,
                              byte[] felicaCommandParams,byte[] payload,byte[] mac) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, ShortBufferException, BadPaddingException, InvalidAlgorithmParameterException {
        byte[] b0 = new byte[16];
        byte[] b1 = new byte[16];
        byte[] rawMac = new byte[16];
        byte[] ctrBlock = new byte[16];
        byte[] counter = new byte[16];
        byte[] workBuf = new byte[256];
        byte[] tempPayload = new byte[256];
        // block 0
        b0[0] = 0x59;
        System.arraycopy(snr,0,b0,1,4);
        System.arraycopy(rcr,0,b0,1+4,4);
        System.arraycopy(rar,0,b0,1+4+4,5);
        b0[14] = (byte)(felicaCommandLength/0x0100);
        b0[15] = (byte)(felicaCommandLength%0x0100);

        // block 1
        b1[0] = 0x00;
        b1[1] = 0x09;
        b1[2] = command;
        b1[3] = subCommand;
        b1[4] = 0x00;
        b1[5] = 0x00;
        b1[6] = 0x00;
        System.arraycopy(snr,0,b1,7,4);
        System.arraycopy(new byte[]{0x00,0x00,0x00,0x00,0x00},0,b1,11,5);

        // Generate ctrl
        // Encrypt data
        Arrays.fill(counter,(byte) 0x00);
        Arrays.fill(payload,(byte) 0x00);

        SecretKeySpec keySpec = new SecretKeySpec(kYtr,"AES");
        Cipher cipher = Cipher.getInstance("AES/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE,keySpec);
        int i=0;
        for(i=0;i+15<felicaCommandLength;i+=16){
            ctrBlock[0] = 0x01;
            System.arraycopy(snr,1,ctrBlock,0,4);
            System.arraycopy(rcr,1+4,ctrBlock,0,4);
            System.arraycopy(rar,1+4+4,ctrBlock,0,5);
            ctrBlock[14] = 0x00;
//            ctrBlock[15] = 0x01;
            ctrBlock[15] = (byte)((i/16)+1);
            cipher.doFinal(ctrBlock,i,16,tempPayload,i);
            for(int j=i;j<i+16;j++){
                tempPayload[j] = (byte) (tempPayload[j]^felicaCommandParams[j]);
            }
        }
        if (i<felicaCommandLength){
            ctrBlock[0] = 0x01;
            System.arraycopy(snr,1,ctrBlock,0,4);
            System.arraycopy(rcr,1+4,ctrBlock,0,4);
            System.arraycopy(rar,1+4+4,ctrBlock,0,5);
            ctrBlock[14] = 0x00;
//            ctrBlock[15] = 0x01;
            ctrBlock[15] = (byte)((i/16)+1);
            cipher.doFinal(ctrBlock,i,16,tempPayload,16);
            for(int j=i;j<felicaCommandLength;j++){
                tempPayload[j] = (byte) (tempPayload[j]^felicaCommandParams[j]);
            }
        }

        // Calculate CBC-MAC

        Arrays.fill(workBuf,(byte)0x00);
        System.arraycopy(workBuf,0,b0,0,16);
        System.arraycopy(workBuf,16,b1,0,16);
        System.arraycopy(workBuf,16+16,felicaCommandParams,0,felicaCommandLength);
        this.calculateMacRaw(workBuf,16+16+felicaCommandLength,rawMac);
        byte[] jCMac = new byte[16];
        this.calculateMacUsingCMac(workBuf,16+16+felicaCommandLength,jCMac);

        // Encrypt mac

        ctrBlock[0] = 0x01;
        System.arraycopy(snr,1,ctrBlock,0,4);
        System.arraycopy(rcr,1+4,ctrBlock,0,4);
        System.arraycopy(rar,1+4+4,ctrBlock,0,5);
        ctrBlock[14] = 0x00;
        ctrBlock[15] = 0x00;
        Arrays.fill(counter,(byte) 0x00); // no need in java

        cipher = Cipher.getInstance("AES/CTR/NoPadding");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ctrBlock);
        cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivParameterSpec);
        byte[] jMac = new byte[8];
        cipher.doFinal(rawMac,0,8,mac);
        cipher.doFinal(rawMac,0,8,jMac);
        System.arraycopy(tempPayload,0,payload,0,256);

    }

    private byte[] mergeArray(byte[] arr1, byte[] arr2) {
        byte[] mergedArray = new byte[arr1.length+arr2.length];
        System.arraycopy(arr1, 0, mergedArray, 0, arr1.length);
        System.arraycopy(arr2, 0, mergedArray, arr1.length, arr2.length);
        return mergedArray;
    }


    public long askFeliCaCmdToSAM(byte commandCode,
                                         int felicaCmdParamsLen,
                                         byte[] felicaCmdParams,
                                         int[] felicaCommandLen,
                                         byte[] felicaCommand) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, ShortBufferException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        // Call the method equivalent to AskFeliCaCmdToSAMSC in Java
        return askFeliCaCmdToSAMSC(commandCode,
                (byte) 0x00,  // SubCommandCode is 0x00
                felicaCmdParamsLen,
                felicaCmdParams,
                felicaCommandLen,
                felicaCommand);
    }

    public long askFeliCaCmdToSAMSC(byte commandCode,
                                    byte subCommandCode,
                                    int felicaCmdParamsLen,
                                    byte[] felicaCmdParams,
                                    int[] felicaCommandLen,
                                    byte[] felicaCommand) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, ShortBufferException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        long ret;
        byte[] payload = new byte[262];
        byte[] mac = new byte[8];
        byte[] sendBuf = new byte[262];
        byte[] samRes = new byte[262];
        int sendLen;
        int samResLen = 0xFF;

        // Encrypt command payload data
        this.encryptData(commandCode, subCommandCode, felicaCmdParamsLen, felicaCmdParams, payload, mac);

        // Construct command packet sent to SAM
        sendBuf[0] = 0x00;            // Dispatcher
        sendBuf[1] = 0x00;            // Reserved
        sendBuf[2] = 0x00;            // Reserved
        sendBuf[3] = commandCode;     // Command Code
        sendBuf[4] = subCommandCode;  // Sub Command Code
        sendBuf[5] = 0x00;            // Reserved
        sendBuf[6] = 0x00;            // Reserved
        sendBuf[7] = 0x00;            // Reserved
        System.arraycopy(snr, 0, sendBuf, 8, 4);  // Snr
        System.arraycopy(payload, 0, sendBuf, 12, felicaCmdParamsLen);  // Encrypted data
        System.arraycopy(mac, 0, sendBuf, 12 + felicaCmdParamsLen, 8);  // Encrypted MAC
        sendLen = 8 + 4 + felicaCmdParamsLen + 8;

        // Send packets to SAM
        String res = this.transitDataToSam(sendBuf, samResLen);
        if (res != null) {
            return 0;
        }

        // Extract FeliCa command packets from SAM response
        if (samRes[3] != (byte) 0x7f) {
            felicaCommandLen[0] = samResLen - 3;
            System.arraycopy(samRes, 3, felicaCommand, 0, felicaCommandLen[0]);
            // HexDump(samResLen, samRes, "SAM response");
            return 1;
        } else {
            felicaCommandLen[0] = 1;
            // TextDump("SAM decline", 0);
            return 0;
        }
    }

    public String SendAuth1V2ResultToSAM(int[] felicaResLen,
                                byte[] felicaResponse,
                                int[] auth2V2CommandLen,
                                byte[] auth2V2Command)
    {
        byte[]	sendBuf = new byte[262];
        byte[]	samRes = new byte[262];
//        int	sendLen, samResLen;

        //Send back the response from FeliCa Card to RW-SAM(RC-S500)
        sendBuf[0] =			0x01;	// Dispatcher
        sendBuf[1] =			0x00;	// Reserved
        sendBuf[2] =			0x00;	// Reserved
        System.arraycopy(felicaResponse,0,sendBuf,3,felicaResLen[0]); //response of FeliCa Card
//        samResLen = 0xFF;

        // Send packets to SAM
        String result = this.transitDataToSam(sendBuf, 0xFF);
        if(TextUtils.isEmpty(result))
        {
            //PrintText("Card result Error\n");
            return null;
        }

	    auth2V2CommandLen[0] = result.length() - 3;
        System.arraycopy(samRes,3,auth2V2Command,0,auth2V2CommandLen[0]);

        return result;
    }
    public  int sendAuth1V2ResultToSAM(int felicaResLen, byte[] felicaResponse, int[] auth2V2CommandLen, byte[] auth2V2Command) {
        long ret = 0;
        byte[] sendBuf = new byte[262];
        byte[] samRes = new byte[262];
        int sendLen, samResLen;

        // Prepare the buffer to send back the response from FeliCa Card to RW-SAM(RC-S500)
        sendBuf[0] = 0x01;  // Dispatcher
        sendBuf[1] = 0x00;  // Reserved
        sendBuf[2] = 0x00;  // Reserved
        System.arraycopy(felicaResponse, 0, sendBuf, 3, felicaResLen); // response of FeliCa Card
        sendLen = 3 + felicaResLen;
        samResLen = 0xFF;

        // Send packets to SAM
        String result = transitDataToSam(sendBuf, 0xFF);
        if (!TextUtils.isEmpty(result)) {
            // PrintText("Card result Error\n");
            return 0;
        }
        System.arraycopy(Utils.hexToByte(result),0,samRes,0,result.length()/2);

        auth2V2CommandLen[0] = samResLen - 3;
        System.arraycopy(samRes, 3, auth2V2Command, 0, auth2V2CommandLen[0]);

        return 1;
    }

}
