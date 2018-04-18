package org.lytsiware.clash;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Week from Monday to Sunday(inclusive)
 * Week number {@link #getWeek()} represents the number of weeks after the week zero
 * which is from Monday 2/1/2017
 */
public class Week {
    public static final LocalDate ZERO_WEEK = LocalDate.of(2017, 1, 2);

    private int week;
    private LocalDate startDate;
    private LocalDate endDate;

    public static Week now() {
        return new Week();
    }

    public static Week fromWeek(int week) {
        return new Week(week);
    }

    public static Week fromDate(LocalDate date) {
        return new Week(date);
    }

    public Week previous() {
        return this.minusWeeks(1);
    }

    public Week next() {
        return this.plusWeeks(1);
    }

    public Week plusWeeks(int weeks) {
        return new Week(week + weeks);
    }

    public Week minusWeeks(int weeks) {
        return new Week(week - weeks);
    }

    private Week() {
        this(LocalDate.now(ZoneIdConfiguration.zoneId()));
    }

    private Week(int week) {
        this.week = week;
    }


    private Week(LocalDate date) {
        this(toWeek(date));
    }

    private static int toWeek(LocalDate date) {
        long days = ZERO_WEEK.until(date, ChronoUnit.DAYS);
        return (int) days / 7;
    }


    private static LocalDate toDate(int week) {
        return ZERO_WEEK.plus(week * 7, ChronoUnit.DAYS);
    }


    public int getWeek() {
        return week;
    }

    public LocalDate getStartDate() {
        if (startDate == null) {
            startDate = toDate(week);
        }
        return startDate;
    }

    public LocalDate getEndDate() {
        if (endDate == null) {
            endDate = getStartDate().plusDays(6);
        }
        return endDate;
    }

    @Override
    public String toString() {
        return "" + week;
    }

    public String toStringWithDates() {
        final StringBuilder sb = new StringBuilder();
        sb.append(week);
        sb.append(" (").append(getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        sb.append("-").append(getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        sb.append(")}");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return week;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        return week == ((Week) obj).week;
    }
}
