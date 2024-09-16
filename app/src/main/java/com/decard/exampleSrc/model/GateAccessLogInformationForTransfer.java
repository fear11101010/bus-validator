package com.decard.exampleSrc.model;

import java.nio.ByteBuffer;
import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    public static GateAccessLogInformationForTransfer generateData(byte[] data){
        Block0 block0 = new Block0();
        block0.setOriginStation(Arrays.copyOfRange(data,0,2));
        block0.setTransferStation1(Arrays.copyOfRange(data,2,4));
        block0.setTransferStation2(Arrays.copyOfRange(data,4,6));
        block0.setTransferStation3(Arrays.copyOfRange(data,6,8));
        block0.setFareAllocationAmountForOwnLine(Arrays.copyOfRange(data,8,11));
        block0.setFareAllocationAmountForOtherLine(Arrays.copyOfRange(data,11,14));
        block0.setReserved(Arrays.copyOfRange(data,14,16));

        Block1 block1 = new Block1();
        block1.setAmountOfTemporaryFare(Arrays.copyOfRange(data,16,19));
        block1.setAmountOfTemporaryFare(Arrays.copyOfRange(data,19,21));
        block1.setAmountOfTemporaryFare(Arrays.copyOfRange(data,21,32));

        return GateAccessLogInformationForTransfer.builder()
                .block0(block0)
                .block1(block1)
                .build();
    }

    public byte[] getData(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(32);
        byteBuffer.put(this.block0.originStation);
        byteBuffer.put(this.block0.transferStation1);
        byteBuffer.put(this.block0.transferStation2);
        byteBuffer.put(this.block0.transferStation3);
        byteBuffer.put(this.block0.fareAllocationAmountForOwnLine);
        byteBuffer.put(this.block0.fareAllocationAmountForOtherLine);
//        byteBuffer.put(this.block0.fareAllocationAmountForOtherLine);
        byteBuffer.put(this.block0.reserved);
        byteBuffer.put(this.block1.amountOfTemporaryFare);
        byteBuffer.put(this.block1.stationForTemporaryFareCalculation);
        byteBuffer.put(this.block1.reserved);
        return byteBuffer.array();
    }
}
