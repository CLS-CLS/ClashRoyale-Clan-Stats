package org.lytsiware.clash.utils;

public class AvgCalc {

    public static double avg(int[] stats) {
        int sum = 0;
        for (int i = 0; i < stats.length; i++) {
            sum += stats[i];
        }
        return sum / stats.length;
    }


}
