package com.decard.exampleSrc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryRecord {
    /**
     * length 1 byte
     */
    private byte binEquipmentClass;
    /**
     * length 1 byte
     */
    private byte binServiceClass;
    /**
     * length 1 byte
     */
    private byte binContextCode;
    /**
     * length 1 byte
     */
    private byte binPaymentMethod;
    /**
     * length 2 byte
     */
    private byte[] binDate;
    /**
     * length 1 byte
     */
    private byte binTime;
    /**
     * length 2 byte
     */
    private byte[] binPlace1;
    /**
     * length 2 byte
     */
    private byte[] binPlace2;
    /**
     * length 3 byte
     */
    private byte[] binCardBalance;
    /**
     * length 2 byte
     */
    private byte[] binSVLogId;
}
