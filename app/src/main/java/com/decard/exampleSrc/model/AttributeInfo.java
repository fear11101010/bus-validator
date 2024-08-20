package com.decard.exampleSrc.model;

import java.nio.ByteBuffer;

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
