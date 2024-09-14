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
    byte[] cardFunctionCode; // 1-2
    byte cardControlCode;
    byte discountCode;
    /**
     * length 2 byte
     */
    byte[] expiryDate;
    /**
     * length 2 byte
     */
//3
    byte[] txnDataId;//4
    /**
     * length 8 byte
     */
    byte[] replacedCardId;
    /**
     * length 8 byte
     */
    byte[] merchendizeManagementCode;
    /**
     * length 2 byte
     */
    byte[] negativeValue;
    /**
     * length 1 byte
     */
    byte rechargeType;

    /**
     * length 5 byte
     */
    byte[] reserved;

    public static AttributeInfo generateData(byte[] data){
        return AttributeInfo.builder()
                .cardFunctionCode(Arrays.copyOfRange(data,0,2))
                .cardControlCode(data[2])
                .discountCode(data[3])
                .expiryDate(Arrays.copyOfRange(data,4,6))
                .txnDataId(Arrays.copyOfRange(data,6,8))
                .replacedCardId(Arrays.copyOfRange(data,8,16))
                .merchendizeManagementCode(Arrays.copyOfRange(data,16,24))
                .negativeValue(Arrays.copyOfRange(data,24,26))
                .rechargeType(data[26])
                .reserved(Arrays.copyOfRange(data,27,32))
                .build();
    }

    /**
     * size of byte array 32
     * @return array of byte
     */
    public byte[] getData(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(32);
        byteBuffer.put(getCardFunctionCode());
        byteBuffer.put(getCardControlCode());
        byteBuffer.put(getDiscountCode());
        byteBuffer.put(getExpiryDate());
        byteBuffer.put(getTxnDataId());
        byteBuffer.put(getReplacedCardId());
        byteBuffer.put(getMerchendizeManagementCode());
        byteBuffer.put(getNegativeValue());
        byteBuffer.put(getRechargeType());
        byteBuffer.put(getReserved());
        return byteBuffer.array();
    }
}
