package com.decard.exampleSrc.model;

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
}
