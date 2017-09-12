package org.lytsiware.clash.utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateWeekConverter {

    public static LocalDate zeroWeek = LocalDate.of(2017, 1, 2);

    public static int toWeek(LocalDate date) {
        long days = zeroWeek.until(date, ChronoUnit.DAYS);
        return (int)days / 7;
    }


    public static LocalDate toDate(int week){
        LocalDate finalDate = zeroWeek.plus(week * 7, ChronoUnit.DAYS);
        return finalDate;
    }



}
