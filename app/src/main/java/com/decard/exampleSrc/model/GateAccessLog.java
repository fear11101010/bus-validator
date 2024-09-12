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
public class GateAccessLog {
    /**
     * length 2 byte
     */
    private byte[] binStatus;
    /**
     * length 2 byte
     */
    private byte[] binDate;
    /**
     * length 2 byte
     */
    private byte[] binTime;
    /**
     * length 2 byte
     */
    private byte[] binStationCode;
    /**
     * length 2 byte
     */
    private byte[] binEquipmentLocation;
    /**
     * length 3 byte
     */
    private byte[] binAmountBaseFare;
    /**
     * length 3 byte
     */
    private byte[] binAmountDistanceFare;

    public static GateAccessLog generateData(byte[] data){
        return GateAccessLog.builder()
                .binStatus(Arrays.copyOfRange(data,0,2))
                .binDate(Arrays.copyOfRange(data,2,4))
                .binTime(Arrays.copyOfRange(data,4,6))
                .binStationCode(Arrays.copyOfRange(data,6,8))
                .binEquipmentLocation(Arrays.copyOfRange(data,8,10))
                .binAmountBaseFare(Arrays.copyOfRange(data,10,13))
                .binAmountDistanceFare(Arrays.copyOfRange(data,13,16))
                .build();
    }
}
