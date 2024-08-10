package com.decard.exampleSrc;

import android.util.Log;

import com.decard.NDKMethod.BasicOper;

import java.util.Arrays;

public class FelicaCard {
    private byte[] iDm;
    private byte[] pMm;
    private byte[] idt;
    private byte[] idi;
    private byte[] systemCode;

    public FelicaCard(){
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
}
