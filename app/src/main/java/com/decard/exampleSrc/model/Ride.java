package com.decard.exampleSrc.model;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.decard.exampleSrc.FelicaCard;
import com.decard.exampleSrc.Utils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Setter
@Getter
public class Ride {
    private final String RIDE_AND_DEDUCTION_FROM_SV_NOT_NEGATIVE = "D220";
    private final String RIDE_AND_DEDUCTION_FROM_SV_NEGATIVE = "D320";

    private AttributeInfo attributeInfo;
    private EPurseInfo ePurseInfo;
    private StoredLogInformation storedLogInformation;
    private GateAccessLogInformation gateAccessLogInformation;
    private GateAccessLogInformationForTransfer gateAccessLogInformationForTransfer;
    @Getter(AccessLevel.NONE)
    private ReadWriteInCard readWriteInCard;
    private String startingPlace;
    private String endingPlace;
    private String routeName;
    /*@Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Route route;*/
    private boolean isNegative;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Route.Station startingStation;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Route.Station endingStation;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private boolean rideStatus;

    public Ride(@NonNull ReadWriteInCard readWriteInCard) {
        try {
            this.readWriteInCard = readWriteInCard;
            FelicaCard felicaCard = this.readWriteInCard.readData();
            this.attributeInfo = felicaCard.getFelicaCardDetail().getAttributeInfo();
            this.ePurseInfo = felicaCard.getFelicaCardDetail().getEPurseInfo();
            this.storedLogInformation = felicaCard.getFelicaCardDetail().getStoredLogInformation();
            this.gateAccessLogInformation = felicaCard.getFelicaCardDetail().getGateAccessLogInformation();
            this.gateAccessLogInformationForTransfer = felicaCard.getFelicaCardDetail().getGateAccessLogInformationForTransfer();
            String serviceId = Utils.byteToHex(new byte[]{storedLogInformation.getServiceClassificationCode(), storedLogInformation.getContextCode()});
            this.rideStatus = Utils.convertByteArrayToBit(gateAccessLogInformation.getStatusFlag()).toCharArray()[15] == '1' &&
                    (serviceId.equals(RIDE_AND_DEDUCTION_FROM_SV_NEGATIVE) || serviceId.equals(RIDE_AND_DEDUCTION_FROM_SV_NOT_NEGATIVE)) &&
                    Utils.byteToHex(storedLogInformation.getPlace2()).equals("0000");
        } catch (Exception e) {
            Log.d("RIDE", "can not read data from card");
//           e.printStackTrace();
        }
    }

    public void setStartingPlace(String startingPlace) {
        this.startingPlace = startingPlace;
        this.startingStation = getStationCode(startingPlace);
        this.isNegative = Utils.charArrayToIntLE(ePurseInfo.getBinRemainingSV(), 4) - startingStation.getMaxFare() < 0;

    }

    public void setEndingPlace(String endingPlace) {
        this.endingPlace = endingPlace;
        this.endingStation = getStationCode(endingPlace);
    }

    private void updateData() {
        // update data
        updateAttributeInfo();
        updateEPurseInfo();
        updateStoredValueLog();
        updateAccessLogInformation();
        updateTransferAccessLogInformation();
    }


    private void updateAttributeInfo() {
        if (attributeInfo != null) {
            int transId = Utils.byteToInteger(attributeInfo.getTxnDataId()) + 1;
            attributeInfo.setTxnDataId(Utils.hexToByte(String.format("%04X", transId)));
            if (isNegative) {
                int maxFare = startingStation.getMaxFare();
                int negativeValue = Math.abs(maxFare - Utils.byteToInteger(attributeInfo.getNegativeValue()));
                attributeInfo.setNegativeValue(Utils.hexToByte(String.format("%04X", negativeValue)));
            }
        }
    }

    private void updateEPurseInfo() {
        assert ePurseInfo != null;
        int executionId = Utils.byteToInteger(ePurseInfo.getBinExecutionId()) + 1;
        ePurseInfo.setBinExecutionId(Utils.hexToByte(String.format("%04X", executionId)));

        int maxFare = startingStation.getMaxFare();
        int sv = Utils.charArrayToIntLE(ePurseInfo.getBinRemainingSV(), 4);
        if (isNegative) {
//            ePurseInfo.setBinCashbackData(Utils.hexToByte(String.format("%08X",sv)));
            ePurseInfo.setBinCashbackData(Utils.intToCharArrayLE(sv));
            ePurseInfo.setBinRemainingSV(Utils.intToCharArrayLE(0));
        } else {
//            ePurseInfo.setBinRemainingSV(Utils.hexToByte(String.format("%04X",sv-maxFare)));
            ePurseInfo.setBinRemainingSV(Utils.intToCharArrayLE(sv - maxFare));
//            ePurseInfo.setBinCashbackData(Utils.hexToByte(String.format("%04X",maxFare)));
            ePurseInfo.setBinCashbackData(Utils.intToCharArrayLE(maxFare));
        }
    }

    private void updateStoredValueLog() {
        assert storedLogInformation != null;
        storedLogInformation.setEquipmentClassificationCode((byte) 0x42);
        storedLogInformation.setContextCode((byte) 0x20);
        storedLogInformation.setPaymentMethodCode((byte) 0x00);
        Map<String, String> date = Utils.getYearMonthDateHourMinute();
        String day = date.get("year") + date.get("month") + date.get("day");
        day = String.format("%04X", Integer.parseInt(day, 2));
        storedLogInformation.setDate(Utils.hexToByte(day));
        String time = date.get("hour") + date.get("minute") + "00000";
        storedLogInformation.setTime(Utils.hexToByte(String.format("%02X", Integer.parseInt(time, 2)))[0]);
        storedLogInformation.setPlace1(Utils.hexToByte(String.format("%02x", Integer.parseInt(startingStation.getCode().substring(0, 3))) + String.format("%02x", Integer.parseInt(startingStation.getCode().substring(3)))));
        storedLogInformation.setPlace2(Utils.hexToByte("0000"));
        storedLogInformation.setStoredValueLogId(ePurseInfo.getBinExecutionId());

        if (isNegative) {
            storedLogInformation.setServiceClassificationCode((byte) 0xD3);
            storedLogInformation.setCardBalance(Utils.convertToTwosComplement(-Integer.parseInt(Utils.byteToHex(attributeInfo.getNegativeValue()), 16), 3));
        } else {
            storedLogInformation.setServiceClassificationCode((byte) 0xD2);
            storedLogInformation.setCardBalance(Arrays.copyOfRange(ePurseInfo.getBinRemainingSV(), 0, 3));
        }
    }

    private void updateAccessLogInformation() {
        assert gateAccessLogInformation != null;
        String statusFlagInBit = "110010" + (attributeInfo.getDiscountCode() == 0x00 ? "0" : "1") + "000000000";
        gateAccessLogInformation.setStatusFlag(Utils.hexToByte(String.format("%04X", Integer.parseInt(statusFlagInBit, 2))));
        Map<String, String> date = Utils.getYearMonthDateHourMinute();
        String day = date.get("year") + date.get("month") + date.get("day");
        gateAccessLogInformation.setDate(Utils.hexToByte(String.format("%04X", Integer.parseInt(day, 2))));
        String time = "000" + date.get("hour") + "00" + date.get("minute");
        gateAccessLogInformation.setTime(Utils.hexToByte(String.format("%04X", Integer.parseInt(time, 2))));
        gateAccessLogInformation.setCurrentStationCode(Utils.hexToByte(String.format("%02x", Integer.parseInt(startingStation.getCode().substring(0, 3))) + String.format("%02x", Integer.parseInt(startingStation.getCode().substring(3)))));
        gateAccessLogInformation.setCurrentEquipmentLocationNumber(Utils.hexToByte("0101"));
        gateAccessLogInformation.setAmountOfBasicFare(Utils.hexToByte("000000"));

        int maxFare = startingStation.getMaxFare();
        gateAccessLogInformation.setAmountOfDistanceFare(Utils.hexToByte(String.format("%06X", maxFare)));
    }

    private void updateTransferAccessLogInformation() {
        assert gateAccessLogInformationForTransfer != null;
        int maxFare = startingStation.getMaxFare();
        GateAccessLogInformationForTransfer.Block0 block0 = new GateAccessLogInformationForTransfer.Block0();
        block0.setReserved(new byte[]{0x00, 0x00});
        block0.setOriginStation(Utils.hexToByte(String.format("%02x", Integer.parseInt(startingStation.getCode().substring(0, 3))) + String.format("%02x", Integer.parseInt(startingStation.getCode().substring(3)))));
        block0.setTransferStation1(new byte[]{0x00, 0x00});
        block0.setTransferStation2(new byte[]{0x00, 0x00});
        block0.setTransferStation3(new byte[]{0x00, 0x00});
        block0.setFareAllocationAmountForOwnLine(Utils.hexToByte(String.format("%06X", maxFare)));
        block0.setFareAllocationAmountForOtherLine(new byte[]{0x00, 0x00, 0x00});

        GateAccessLogInformationForTransfer.Block1 block1 = new GateAccessLogInformationForTransfer.Block1();
        block1.setReserved(Utils.hexToByte(String.format("%022X", 0)));
        block1.setAmountOfTemporaryFare(Utils.hexToByte(String.format("%06X", 0)));
        block1.setStationForTemporaryFareCalculation(Utils.hexToByte(String.format("%04X", 0)));
        gateAccessLogInformationForTransfer.setBlock0(block0);
        gateAccessLogInformationForTransfer.setBlock1(block1);
    }

    private Route.Station getStationCode(String stationName) {
        assert routeName != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            for (int i = 0; i < Utils.route.getNumberOfRoute(); i++) {
                if (!TextUtils.equals(this.routeName, Utils.route.getRouteName().get(i))) continue;
                ;
                for (Route.Station station : Objects.requireNonNull(Utils.route.getStations().get(Utils.route.getRouteName().get(i)))) {
                    if (TextUtils.equals(station.getName(), stationName)) {
                        return station;
                    }
                }
            }
            return null;
        }
        return null;
    }
//    private void update

    public int writeData() {
        // update data
        updateData();

        byte[] attributeInfoData = attributeInfo.getData();
        byte[] ePurseData = ePurseInfo.getData();
        byte[] storageInfoData = storedLogInformation.getData();
        byte[] gateAccessLogData = gateAccessLogInformation.getData();
        byte[] gateAccessLogTransferData = gateAccessLogInformationForTransfer.getData();

        // make data

        ByteBuffer byteBuffer = ByteBuffer.allocate(attributeInfoData.length + ePurseData.length + storageInfoData.length + gateAccessLogData.length + gateAccessLogTransferData.length);
        byteBuffer.put(attributeInfoData);
        byteBuffer.put(ePurseData);
        byteBuffer.put(storageInfoData);
        byteBuffer.put(gateAccessLogData);
        byteBuffer.put(gateAccessLogTransferData);

        // write data

        int serviceNumber = 5;
        byte[] serviceCodeList = new byte[serviceNumber * 4];
        serviceCodeList[0] = (byte) 0x08; // Attribute information file
        serviceCodeList[1] = (byte) 0x13; // Attribute information file
        serviceCodeList[2] = (byte) 0x01; // Version key
        serviceCodeList[3] = (byte) 0x00; // Version key
        serviceCodeList[4] = (byte) 0x10; // e-Purse  file
        serviceCodeList[5] = (byte) 0x14; // e-Purse  file
        serviceCodeList[6] = (byte) 0x01; // Version key
        serviceCodeList[7] = (byte) 0x00; // Version key
        serviceCodeList[8] = (byte) 0x0C; // Stored value log information file
        serviceCodeList[9] = (byte) 0x22; // Stored value log information file
        serviceCodeList[10] = (byte) 0x01; // Version key
        serviceCodeList[11] = (byte) 0x00; // Version key
        serviceCodeList[12] = (byte) 0x0C; // Gate access log file
        serviceCodeList[13] = (byte) 0x25; // Gate access log file
        serviceCodeList[14] = (byte) 0x01; // Version key
        serviceCodeList[15] = (byte) 0x00; // Version key
        serviceCodeList[16] = (byte) 0x08;  // Gate access log file (for transfer)
        serviceCodeList[17] = (byte) 0x26; // Gate access log file (for transfer)
        serviceCodeList[18] = (byte) 0x01; // Version key
        serviceCodeList[19] = (byte) 0x00; // Version key

        // block number

        int blockNumber = 7;
        byte[] blockNumberList = new byte[blockNumber * 2];
        blockNumberList[0] = (byte) 0x80;
        blockNumberList[1] = (byte) 0x00;
        blockNumberList[2] = (byte) 0x80;
        blockNumberList[3] = (byte) 0x01;
        blockNumberList[4] = (byte) 0x81;
        blockNumberList[5] = (byte) 0x00;
        blockNumberList[6] = (byte) 0x82;
        blockNumberList[7] = (byte) 0x00;
        blockNumberList[8] = (byte) 0x83;
        blockNumberList[9] = (byte) 0x00;
        blockNumberList[10] = (byte) 0x84;
        blockNumberList[11] = (byte) 0x00;
        blockNumberList[12] = (byte) 0x84;
        blockNumberList[13] = (byte) 0x01;

        try {
            return readWriteInCard.writeInCard(serviceNumber, serviceCodeList, blockNumber, blockNumberList, byteBuffer.array());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("writeData: ", e.getMessage());
            return -1;
        }
    }
}
