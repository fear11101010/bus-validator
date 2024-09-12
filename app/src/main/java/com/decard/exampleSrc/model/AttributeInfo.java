package com.decard.exampleSrc.model;

import java.nio.ByteBuffer;
import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttributeInfo {
    /**
     * length 2 byte
     */
    byte[] binCardFunctionCode; // 1-2
    byte binCardControlCode;
    byte binDiscountCode;
    /**
     * length 2 byte
     */
    byte[] binExpiryDate;
    /**
     * length 2 byte
     */
//3
    byte[] binTxnDataId;//4
    /**
     * length 8 byte
     */
    byte[] binReplacedCardId;
    /**
     * length 8 byte
     */
    byte[] binMerchandizeManagementCode;
    /**
     * length 2 byte
     */
    byte[] binNegativeValue;
    /**
     * length 1 byte
     */
    byte rechargeType;

    /**
     * length 5 byte
     */
    byte[] binReserved;

    public static AttributeInfo generateData(byte[] data){
        return AttributeInfo.builder()
                .binCardFunctionCode(Arrays.copyOfRange(data,0,2))
                .binCardControlCode(data[2])
                .binDiscountCode(data[3])
                .binExpiryDate(Arrays.copyOfRange(data,4,6))
                .binTxnDataId(Arrays.copyOfRange(data,6,8))
                .binReplacedCardId(Arrays.copyOfRange(data,8,16))
                .binMerchandizeManagementCode(Arrays.copyOfRange(data,16,24))
                .binNegativeValue(Arrays.copyOfRange(data,24,26))
                .rechargeType(data[26])
                .binReserved(Arrays.copyOfRange(data,27,32))
                .build();
    }


    public byte[] getData(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(32);
        byteBuffer.put(getBinCardFunctionCode());
        byteBuffer.put(getBinCardControlCode());
        byteBuffer.put(getBinDiscountCode());
        byteBuffer.put(getBinExpiryDate());
        byteBuffer.put(getBinTxnDataId());
        byteBuffer.put(getBinReplacedCardId());
        byteBuffer.put(getBinMerchandizeManagementCode());
        byteBuffer.put(getBinNegativeValue());
        byteBuffer.put(getRechargeType());
        byteBuffer.put(getBinReserved());
        return byteBuffer.array();
    }
}
