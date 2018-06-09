package com.measure.siddhantkushwaha.measure.utitlities;

public class AreaConverter {

    private static double acre_per_square_meter = 0.000247105;
    private static double sqFoot_per_square_meter = 10.76389379999997864;

    public static double toAcres(double sqMeter) {
        return sqMeter * acre_per_square_meter;
    }

    public static double toSqFoot(double sqMeter) {
        return sqMeter * sqFoot_per_square_meter;
    }
}
