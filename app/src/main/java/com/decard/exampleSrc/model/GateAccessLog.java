package com.decard.exampleSrc.model;

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
}
