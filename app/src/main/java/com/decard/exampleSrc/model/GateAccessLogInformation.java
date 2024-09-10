package com.decard.exampleSrc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GateAccessLogInformation {
    /**
     * length 2 byte
     */
    private byte[] statusFlag;
    /**
     * length 2 byte
     */
    private byte[] date;
    /**
     * length 2 byte
     */
    private byte[] time;
    /**
     * length 2 byte
     */
    private byte[] currentStationCode;
    /**
     * length 2 byte
     */
    private byte[] currentEquipmentLocationNumber;
    /**
     * length 3 byte
     */
    private byte[] amountOfBasicFare;
    /**
     * length 3 byte
     */
    private byte[] amountOfDistanceFare;
}
