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
    byte[] operatorCode;
    /**
     * length 1 byte
     */
    byte equipmentClass;
    /**
     * length 2 byte
     */
    byte[] stationCode;
    /**
     * length 2 byte
     */
    byte[] equipmentLocation;	//3
    /**
     * length 2 byte
     */
    byte[] activationDate;	//4
    /**
     * length 1 byte
     */
    byte statusFlag;
    /**
     * length 1 byte
     */
    byte paymentMethod;
    /**
     * length 2 byte
     */
    byte[] depositAmount;
    /**
     * length 3 byte
     */
    byte[] reserved;

    public static OperatorInfo generateData(byte[] data){
        return OperatorInfo.builder()
                .operatorCode(Arrays.copyOfRange(data,0,2))
                .equipmentClass(data[2])
                .stationCode(Arrays.copyOfRange(data,3,5))
                .equipmentLocation(Arrays.copyOfRange(data,5,7))
                .activationDate(Arrays.copyOfRange(data,7,9))
                .statusFlag(data[9])
                .paymentMethod(data[10])
                .depositAmount(Arrays.copyOfRange(data,11,13))
                .reserved(Arrays.copyOfRange(data,13,16))
                .build();
    }
}
