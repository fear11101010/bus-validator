package com.decard.exampleSrc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralInfo {
    /**
     * length 8 byte
     */
    byte[] binCardID;	//1-8
    /**
     * length 1 byte
     */
    byte binReCycleCounter;	//9
    /**
     * length 1 byte
     */
    byte binCardType;	//10
    /**
     * length probably 2 byte (11-12)
     */
    short intNegativeValue;			//11-12
    /**
     * length probably 8 byte 13-20
     */
    long lngRemainingSV;
    /**
     * length probably 8 byte 21-28
     */
    long lngCashBackData;
}
