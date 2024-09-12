package com.decard.exampleSrc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GateAccessLogInformationForTransfer {

    private Block0 block0;
    private Block1 block1;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Block0{
        /**
         * First ride
         * length 2 byte
         */
        byte[] originStation;
        /**
         * length 2 byte
         */
        private byte[] transferStation1;
        /**
         * length 2 byte
         */
        private byte[] transferStation2;
        /**
         * length 2 byte
         */
        private byte[] transferStation3;
        /**
         * Own line
         * length 3 byte
         */
        private byte[] fareAllocationAmountForOwnLine;
        /**
         * Other line
         * length 3 byte
         */
        private byte[] fareAllocationAmountForOtherLine;
        /**
         * length 2 byte
         */
        private byte[] reserved;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Block1{
        /**
         * length 3 byte
         */
        private byte[] amountOfTemporaryFare;
        /**
         * length 2 byte
         */
        private byte[] stationForTemporaryFareCalculation;
        /**
         * length 11 byte
         */
        private byte[] reserved;

    }
}
