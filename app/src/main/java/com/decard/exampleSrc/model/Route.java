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
        STATION_CODES.put("Motijheel", "129010");
        STATION_CODES.put("Gulistan", "129013");
        STATION_CODES.put("Palton", "129016");
        STATION_CODES.put("Press Club", "129019");
        STATION_CODES.put("Shahabag", "129022");
        STATION_CODES.put("Farmgate", "129025");
        STATION_CODES.put("Banani", "129028");
        STATION_CODES.put("Khilket", "129031");
        STATION_CODES.put("Airport", "129034");
        STATION_CODES.put("Azompur", "129037");
        STATION_CODES.put("House Building", "129040");
        STATION_CODES.put("Abudullahpur", "129043");
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Station {
        private String name;
        private String code;
        private int[] fare;
        private int position;
        private int maxFare;

    }

}
