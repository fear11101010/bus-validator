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
    byte[] name;
    /**
     * length 6 byte
     */
    byte[] phone;
    /**
     * length 2 byte
     */
    byte[] birthday;
    /**
     * length 4 byte
     */
    byte[] employeeNumber;
    /**
     * length 1 byte
     */
    byte personalAttribution;
    /**
     * length 3 byte
     */
    byte[] reserved;

    public static PersonalInfo generateData(byte[] data){
        return PersonalInfo.builder()
                .name(Arrays.copyOfRange(data,0,48))
                .phone(Arrays.copyOfRange(data,48,48+6))
                .birthday(Arrays.copyOfRange(data,48+6,48+6+2))
                .employeeNumber(Arrays.copyOfRange(data,48+6+2,48+6+2+4))
                .personalAttribution(data[48+6+2+4])
                .employeeNumber(Arrays.copyOfRange(data,48+6+2+4+1,48+6+2+4+1+3))
                .build();
    }
}
