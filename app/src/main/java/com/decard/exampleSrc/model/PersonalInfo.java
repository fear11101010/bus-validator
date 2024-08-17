package com.decard.exampleSrc.model;

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
    byte[] binPhone;	//3
    /**
     * length 2 byte
     */
    byte[] binBirthday;	//4
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
}
