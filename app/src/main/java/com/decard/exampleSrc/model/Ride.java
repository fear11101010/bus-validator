package com.decard.exampleSrc.model;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.decard.NDKMethod.BasicOper;
import com.decard.exampleSrc.Utils;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
public class Ride {
    private final byte RIDE_AND_DEDUCTION_FROM_SV_NOT_NEGATIVE = (byte) 0xD220;
    private final byte RIDE_AND_DEDUCTION_FROM_SV_NEGATIVE = (byte) 0xD320;

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
    private Route route;
    private boolean isNegative;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Route.Station startingStation;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Route.Station endingStation;

    public Ride() {
       try {
           assert readWriteInCard != null;
           readWriteInCard.readData();
       }catch (Exception e){
           Log.d("RIDE", "can not read data from card");
//           e.printStackTrace();
       }
    }

    private void updateAttributeInfo() {
        if (attributeInfo != null) {
            int transId =Utils.byteToInteger(attributeInfo.getBinTxnDataId())+1;
            attributeInfo.setBinTxnDataId(Utils.hexToByte(String.format("%04X", transId)));
            if(isNegative){
                int maxFare = endingStation.getPosition()> startingStation.getPosition()?startingStation.getMaxUpStreamFare():startingStation.getMaxDownStreamFare();
                int negativeValue = Math.abs(maxFare - Utils.byteToInteger(attributeInfo.getBinNegativeValue()));
                attributeInfo.setBinNegativeValue(Utils.hexToByte(String.format("%04X",negativeValue)));
            }
        }
    }
    private void updateEPurseInfo(){
        assert ePurseInfo != null;
        int executionId = Utils.byteToInteger(ePurseInfo.getBinExecutionId()) + 1;
        ePurseInfo.setBinExecutionId(Utils.hexToByte(String.format("%04X",executionId)));

        int maxFare = endingStation.getPosition()> startingStation.getPosition()?startingStation.getMaxUpStreamFare():startingStation.getMaxDownStreamFare();
        int sv = Utils.charArrayToIntLE(ePurseInfo.getBinRemainingSV(),4);
        if(isNegative){
//            ePurseInfo.setBinCashbackData(Utils.hexToByte(String.format("%08X",sv)));
            ePurseInfo.setBinCashbackData(Utils.intToCharArrayLE(sv));
            ePurseInfo.setBinRemainingSV(Utils.intToCharArrayLE(0));
        } else{
//            ePurseInfo.setBinRemainingSV(Utils.hexToByte(String.format("%04X",sv-maxFare)));
            ePurseInfo.setBinRemainingSV(Utils.intToCharArrayLE(sv-maxFare));
//            ePurseInfo.setBinCashbackData(Utils.hexToByte(String.format("%04X",maxFare)));
            ePurseInfo.setBinCashbackData(Utils.intToCharArrayLE(maxFare));
        }
    }
    private void updateStoredValueLog(){
        assert storedLogInformation != null;
        storedLogInformation.setEquipmentClassificationCode((byte) 0x42);
        storedLogInformation.setContextCode((byte) 0x20);
        storedLogInformation.setPaymentMethodCode((byte) 0x00);
        Map<String,String> date = Utils.getYearMonthDateHourMinute();
        String day = date.get("year")+date.get("month")+date.get("day");
        day = String.format("%04X",Integer.parseInt(day,2));
        storedLogInformation.setDate(Utils.hexToByte(day));
        String time = date.get("hour")+date.get("minute")+"00000";
        storedLogInformation.setTime(Utils.hexToByte(String.format("%02X",Integer.parseInt(day,2)))[0]);
        storedLogInformation.setPlace1(Utils.hexToByte(startingStation.getCode()));
        storedLogInformation.setPlace2(Utils.hexToByte("0000"));
        storedLogInformation.setStoredValueLogId(ePurseInfo.getBinExecutionId());

        if(isNegative){
            storedLogInformation.setServiceClassificationCode((byte) 0xD3);
            storedLogInformation.setCardBalance(Utils.convertToTwosComplement(-Integer.parseInt(Utils.byteToHex(attributeInfo.getBinNegativeValue()), 16),3));
        } else{
            storedLogInformation.setServiceClassificationCode((byte) 0xD2);
            storedLogInformation.setCardBalance(Arrays.copyOfRange(ePurseInfo.getBinRemainingSV(),0,3));
        }
    }
    private void updateAccessLogInformation(){
        assert gateAccessLogInformation !=null;
        String statusFlagInBit = "110010"+(attributeInfo.getBinDiscountCode()==0x00?"0":"1")+"000000000";
        gateAccessLogInformation.setStatusFlag(Utils.hexToByte(String.format("%04X",Integer.parseInt(statusFlagInBit,2))));
        Map<String,String> date = Utils.getYearMonthDateHourMinute();
        String day = date.get("year")+date.get("month")+date.get("day");
        gateAccessLogInformation.setDate(Utils.hexToByte(String.format("%04X",Integer.parseInt(day,2))));
        String time = "000"+date.get("hour")+"00"+date.get("minute");
        gateAccessLogInformation.setTime(Utils.hexToByte(String.format("%04X",Integer.parseInt(time,2))));
        gateAccessLogInformation.setCurrentStationCode(Utils.hexToByte(startingStation.getCode()));
        gateAccessLogInformation.setCurrentEquipmentLocationNumber(Utils.hexToByte("0101"));
        gateAccessLogInformation.setAmountOfBasicFare(Utils.hexToByte("000000"));

        int maxFare = endingStation.getPosition()> startingStation.getPosition()?startingStation.getMaxUpStreamFare():startingStation.getMaxDownStreamFare();
        gateAccessLogInformation.setAmountOfDistanceFare(Utils.hexToByte(String.format("%06X",maxFare)));
    }
    private void updateTransferAccessLogInformation(){
        assert gateAccessLogInformationForTransfer !=null;
        int maxFare = endingStation.getPosition()> startingStation.getPosition()?startingStation.getMaxUpStreamFare():startingStation.getMaxDownStreamFare();
        GateAccessLogInformationForTransfer.Block0 block0 = new GateAccessLogInformationForTransfer.Block0();
        block0.setReserved(new byte[]{0x00,0x00});
        block0.setOriginStation(Utils.hexToByte(startingStation.getCode()));
        block0.setTransferStation1(new byte[]{0x00,0x00});
        block0.setTransferStation2(new byte[]{0x00,0x00});
        block0.setTransferStation3(new byte[]{0x00,0x00});
        block0.setFareAllocationAmountForOwnLine(Utils.hexToByte(String.format("%06X",maxFare)));
        block0.setFareAllocationAmountForOtherLine(new byte[]{0x00,0x00,0x00});

        GateAccessLogInformationForTransfer.Block1 block1 = new GateAccessLogInformationForTransfer.Block1();
        block1.setReserved(Utils.hexToByte(String.format("%022X",0)));
        block1.setAmountOfTemporaryFare(Utils.hexToByte(String.format("%06X",0)));
        block1.setStationForTemporaryFareCalculation(Utils.hexToByte(String.format("%04X",0)));
        gateAccessLogInformationForTransfer.setBlock0(block0);
        gateAccessLogInformationForTransfer.setBlock1(block1);
    }
    private Route.Station getStationCode(String stationName){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            for (int i = 0; i < route.getNumberOfRoute(); i++) {
                for(Route.Station station: Objects.requireNonNull(route.getStations().get(route.getRouteName().get(i)))){
                    if(TextUtils.equals(station.getName(),stationName)){
                        return station;
                    }
                }
            }
            return null;
        }
        return null;
    }
//    private void update
}
