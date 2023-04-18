package ca.bc.gov.educ.api.graduation.model.report;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Component
public class SchoolYearDates {

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    final private Date dateFrom;
    final private Date dateTo;

    public SchoolYearDates() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int advance = (cal.get(Calendar.MONTH) >= 9) ? 0 : -1;
        int yearFrom = cal.get(Calendar.YEAR) + advance;
        advance = (cal.get(Calendar.MONTH) < 9) ? 0 : +1;
        int yearTo = cal.get(Calendar.YEAR) + advance;
        Calendar calFrom = Calendar.getInstance();
        calFrom.set(yearFrom, Calendar.SEPTEMBER, 30);
        calFrom.set(Calendar.HOUR_OF_DAY, 23);
        calFrom.set(Calendar.MINUTE, 59);
        calFrom.set(Calendar.SECOND, 59);
        dateFrom = calFrom.getTime();
        Calendar calTo = Calendar.getInstance();
        calTo.set(yearTo, Calendar.OCTOBER, 1);
        calTo.set(Calendar.HOUR_OF_DAY, 0);
        calTo.set(Calendar.MINUTE, 0);
        calTo.set(Calendar.SECOND, 0);
        dateTo = calTo.getTime();
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public String getFiscalDatesRange() {
        return DATE_FORMAT.format(getDateFrom()) + " to " + DATE_FORMAT.format(getDateTo());
    }

    public static void main(String[] args) {
        System.out.println(new SchoolYearDates().getFiscalDatesRange());
    }

}
