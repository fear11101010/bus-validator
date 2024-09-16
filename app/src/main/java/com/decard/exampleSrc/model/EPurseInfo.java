package com.decard.exampleSrc.model;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EPurseInfo {
    /**
     * length 4 byte
     */
    byte[] binRemainingSV;
    /**
     * length 4 byte
     */
    byte[] binCashbackData;
    /**
     * length 5 byte
     */
    byte[] binCompoundData;
    /**
     * length 1 byte
     */
    byte binPaymentMethod;	//3
    /**
     * length 2 byte
     */
    byte[] binExecutionId;

    /**
     * Generate data from byte
     */

    public static EPurseInfo generateData(byte[] data){
        return EPurseInfo.builder()
                .binRemainingSV(Arrays.copyOfRange(data,0,4))
                .binCashbackData(Arrays.copyOfRange(data,4,8))
                .binCompoundData(Arrays.copyOfRange(data,8,13))
                .binPaymentMethod(data[13])
                .binExecutionId(Arrays.copyOfRange(data,14,16))
                .build();
    }
    /**
     * Get data in byte
     */
    public byte[] getData(){
        byte[] bytes = new byte[16];
        System.arraycopy(getBinRemainingSV(),0,bytes,0,4);
        System.arraycopy(getBinCashbackData(),0,bytes,4,4);
        System.arraycopy(getBinCompoundData(),0,bytes,4+4,5);
        bytes[4+4+5] = binPaymentMethod;
        System.arraycopy(getBinExecutionId(),0,bytes,4+4+5+1,2);
        return bytes;
    }
}
