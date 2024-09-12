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

    public static OperatorInfo generateData(byte[] data){
        return OperatorInfo.builder()
                .binOperatorCode(Arrays.copyOfRange(data,0,2))
                .binEquipmentClass(data[2])
                .binStationCode(Arrays.copyOfRange(data,3,5))
                .binEquipmentLocation(Arrays.copyOfRange(data,5,7))
                .binActivationDate(Arrays.copyOfRange(data,7,9))
                .binStatusFlag(data[9])
                .binPaymentMethod(data[10])
                .binDepositAmount(Arrays.copyOfRange(data,11,13))
                .binReserved(Arrays.copyOfRange(data,13,16))
                .build();
    }
}
