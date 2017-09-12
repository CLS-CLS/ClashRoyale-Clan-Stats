package org.lytsiware.clash;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Week {
    public static final LocalDate ZERO_WEEK = LocalDate.of(2017, 1, 2);

    private int week;
    private LocalDate startDate;
    private LocalDate endDate;

    public Week(){
        this(LocalDate.now());
    }
    public Week(int week){
        this.week = week;
        startDate = toDate(week);
        endDate = startDate.plusDays(6);
    }

    public Week(LocalDate date) {
        this(toWeek(date));
    }

    public Week nextWeek(int weeks){
        return new Week(week+ weeks);
    }
    public Week previousWeek(int weeks){
        return new Week(week - weeks);
    }


    private static int toWeek(LocalDate date) {
        long days = ZERO_WEEK.until(date, ChronoUnit.DAYS);
        return (int)days / 7;
    }


    private static LocalDate toDate(int week){
        LocalDate finalDate = ZERO_WEEK.plus(week * 7, ChronoUnit.DAYS);
        return finalDate;
    }


    public int getWeek() {
        return week;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(week);
        sb.append(" (").append(startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        sb.append("-").append(endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        sb.append(")}");
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.print(new Week(50));

    }
}
