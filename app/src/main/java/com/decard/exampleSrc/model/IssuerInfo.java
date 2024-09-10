package com.decard.exampleSrc.model;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssuerInfo {
    /**
     * length 2 byte
     */
    byte[] binCardIssuerID;
    /**
     * length 1 byte
     */
    byte binIssuerEquipmentClass;
    /**
     * length 1 byte
     */
    byte binInitializerId;
    /**
     * length 2 byte
     */
    byte[] binCardIssueDate; // 2
    /**
     * length 1 byte
     */
    byte binCardRevision;
    /**
     * length 1 byte
     */
    byte binRecycleCounter;
    /**
     * length 8 byte
     */
    byte[] binReserved;  // 8

    public static IssuerInfo generateData(byte[] data){
        return IssuerInfo.builder()
                .binCardIssuerID(Arrays.copyOfRange(data,0,2))
                .binIssuerEquipmentClass(data[2])
                .binIssuerEquipmentClass(data[3])
                .binCardIssueDate(Arrays.copyOfRange(data,4,6))
                .binCardRevision(data[6])
                .binRecycleCounter(data[7])
                .binReserved(Arrays.copyOfRange(data,8,16))
                .build();
    }
}


