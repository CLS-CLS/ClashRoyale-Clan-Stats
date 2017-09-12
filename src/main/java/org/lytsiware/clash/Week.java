package org.lytsiware.clash;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Week {
    public static final LocalDate ZERO_WEEK = LocalDate.of(2017, 1, 2);

    private int week;
    private LocalDate startDate;
    private LocalDate endDate;

    public Week() {
        this(LocalDate.now());
    }

    public Week(int week) {
        this.week = week;
    }

    public Week(LocalDate date) {
        this(toWeek(date));
    }

    public Week plusWeeks(int weeks) {
        return new Week(week + weeks);
    }

    public Week minusWeeks(int weeks) {
        return new Week(week - weeks);
    }


    private static int toWeek(LocalDate date) {
        long days = ZERO_WEEK.until(date, ChronoUnit.DAYS);
        return (int) days / 7;
    }


    private static LocalDate toDate(int week) {
        LocalDate finalDate = ZERO_WEEK.plus(week * 7, ChronoUnit.DAYS);
        return finalDate;
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
        sb.append(" (").append(startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        sb.append("-").append(endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        sb.append(")}");
        return sb.toString();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + week;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Week other = (Week) obj;
		if (week != other.week)
			return false;
		return true;
	}
    
    

    
    
}
