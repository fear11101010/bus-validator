package com.decard.exampleSrc.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    private int numberOfRoute;
    private final List<String> routeName = new ArrayList<>();
    private final Map<String,Station[]> stations = new HashMap<>();
    public static final Map<String,String> STATION_CODES = new HashMap<>();

    static {
        STATION_CODES.put("Motijheel", String.format("%04X",Integer.parseInt("129010")));
        STATION_CODES.put("Gulistan", String.format("%04X",Integer.parseInt("129013")));
        STATION_CODES.put("Palton", String.format("%04X",Integer.parseInt("129016")));
        STATION_CODES.put("Press Club", String.format("%04X",Integer.parseInt("129019")));
        STATION_CODES.put("Shahabag", String.format("%04X",Integer.parseInt("129022")));
        STATION_CODES.put("Farmgate", String.format("%04X",Integer.parseInt("129025")));
        STATION_CODES.put("Banani", String.format("%04X",Integer.parseInt("129028")));
        STATION_CODES.put("Khilket", String.format("%04X",Integer.parseInt("129031")));
        STATION_CODES.put("Airport", String.format("%04X",Integer.parseInt("129034")));
        STATION_CODES.put("Azompur", String.format("%04X",Integer.parseInt("129037")));
        STATION_CODES.put("House Building", String.format("%04X",Integer.parseInt("129040")));
        STATION_CODES.put("Abudullahpur", String.format("%04X",Integer.parseInt("129043")));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Station {
        private String name;
        private String code;
        private int[] fare;
        private int position;
        private int minUpStreamFare;
        private int maxUpStreamFare;
        private int minDownStreamFare;
        private int maxDownStreamFare;

    }

}
