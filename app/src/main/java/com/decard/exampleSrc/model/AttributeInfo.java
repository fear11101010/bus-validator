package com.decard.exampleSrc.model;

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
     * length 6 byte
     */
    byte[] binReserved;
}
