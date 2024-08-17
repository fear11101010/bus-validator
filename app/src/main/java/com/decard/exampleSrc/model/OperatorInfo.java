package com.decard.exampleSrc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OperatorInfo {
    /**
     * length 2 byte
     */
    byte[] binOperatorCode;
    /**
     * length 1 byte
     */
    byte binEquipmentClass;
    /**
     * length 2 byte
     */
    byte[] binStationCode;
    /**
     * length 2 byte
     */
    byte[] binEquipmentLocation;	//3
    /**
     * length 2 byte
     */
    byte[] binActivationDate;	//4
    /**
     * length 1 byte
     */
    byte binStatusFlag;
    /**
     * length 1 byte
     */
    byte binPaymentMethod;
    /**
     * length 2 byte
     */
    byte[] binDepositAmount;
    /**
     * length 3 byte
     */
    byte[] binReserved;
}
