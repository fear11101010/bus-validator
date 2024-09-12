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

public class PersonalInfo {
    /**
     * length 48 byte
     */
    byte[] binName;
    /**
     * length 6 byte
     */
    byte[] binPhone;
    /**
     * length 2 byte
     */
    byte[] binBirthday;
    /**
     * length 4 byte
     */
    byte[] binEmployeeNumber;
    /**
     * length 1 byte
     */
    byte binPersonalAttrib;
    /**
     * length 3 byte
     */
    byte[] binReserved;

    public static PersonalInfo generateData(byte[] data){
        return PersonalInfo.builder()
                .binName(Arrays.copyOfRange(data,0,48))
                .binPhone(Arrays.copyOfRange(data,48,48+6))
                .binBirthday(Arrays.copyOfRange(data,48+6,48+6+2))
                .binEmployeeNumber(Arrays.copyOfRange(data,48+6+2,48+6+2+4))
                .binPersonalAttrib(data[48+6+2+4])
                .binEmployeeNumber(Arrays.copyOfRange(data,48+6+2+4+1,48+6+2+4+1+3))
                .build();
    }
}
