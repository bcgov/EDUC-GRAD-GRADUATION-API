package ca.bc.gov.educ.api.graduation.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class EducGraduationApiUtils {

	private EducGraduationApiUtils() {}

	private static final Logger logger = LoggerFactory.getLogger(EducGraduationApiUtils.class);
	private static final String ERROR_MSG  = "Error {}";

	public static String formatDate(Date date) {
		if (date == null)
			return null;

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
		return simpleDateFormat.format(date);
	}

	public static String formatDate(Date date, String dateFormat) {
		if(date == null) return null;
		return new SimpleDateFormat(dateFormat).format(date);
	}

	public static Date parseDate(String dateString) {
		if (dateString == null || "".compareTo(dateString) == 0)
			return null;

		//fix TRAX date
		if(dateString.contains("/") && dateString.length() < 10) {
			dateString = dateString.replace("/", "-").concat("-01");
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
		Date date = new Date();

		try {
			date = simpleDateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return date;
	}

	public static LocalDate parseDateLocalDate(String dateString) {
		return DateUtils.toLocalDate(parseDate(dateString));
	}

	public static Date parseDate(String dateString, String dateFormat) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
		Date date = new Date();

		//fix TRAX date
		if(dateString.contains("/") && dateString.length() < 10) {
			dateString = dateString.replace("/", "-").concat("-01");
		}
		try {
			date = simpleDateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return date;
	}

	public static String parseTraxDate(String sessionDate) {
		if (StringUtils.isBlank(sessionDate))
			return null;

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
		Date date = new Date();

		try {
			date = simpleDateFormat.parse(sessionDate);
			LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			return localDate.getYear() + "/" + String.format("%02d", localDate.getMonthValue());

		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static HttpHeaders getHeaders(String accessToken) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		httpHeaders.setBearerAuth(accessToken);
		return httpHeaders;
	}

	public static HttpHeaders getHeaders (String username,String password)
	{
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		httpHeaders.setBasicAuth(username, password);
		return httpHeaders;
	}

	public static String formatDateForReport(String updatedTimestamp) {
		SimpleDateFormat fromUser = new SimpleDateFormat(EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyyMMdd");
		try {
			return myFormat.format(fromUser.parse(updatedTimestamp));
		} catch (ParseException e) {
			logger.debug(ERROR_MSG,e.getLocalizedMessage());
		}
		return updatedTimestamp;

	}

	public static String formatDateForReportJasper(String updatedTimestamp) {
		SimpleDateFormat fromUser = new SimpleDateFormat(EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
		SimpleDateFormat myFormat = new SimpleDateFormat(EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
		try {
			return myFormat.format(fromUser.parse(updatedTimestamp));
		} catch (ParseException e) {
			logger.debug(ERROR_MSG,e.getLocalizedMessage());
		}
		return updatedTimestamp;

	}

	public static Date formatIssueDateForReportJasper(String updatedTimestamp) {
		SimpleDateFormat fromUser = new SimpleDateFormat(EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
		SimpleDateFormat myFormat = new SimpleDateFormat(EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
		try {
			return new SimpleDateFormat(EducGraduationApiConstants.DEFAULT_DATE_FORMAT).parse(myFormat.format(fromUser.parse(updatedTimestamp)));
		} catch (ParseException e) {
			logger.debug(ERROR_MSG,e.getLocalizedMessage());
		}
		return null;
	}

	public static LocalDate formatIssueDateForReportJasperLocalDate(String updatedTimestamp) {
		Date date = formatIssueDateForReportJasper(updatedTimestamp);
		if (date != null) {
			return DateUtils.toLocalDate(date);
		}
		return null;
	}

	public static String parsingDateForCertificate(String sessionDate) {
		String actualSessionDate = sessionDate + "/01";
		Date temp = toLastDayOfMonth(parseDate(actualSessionDate, EducGraduationApiConstants.SECONDARY_DATE_FORMAT));
		return formatDate(temp, EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
	}

	public static Date parsingTraxDate(String sessionDate) {
		String actualSessionDate = StringUtils.countMatches(sessionDate, "/") == 2 ? sessionDate : sessionDate + "/01";
		Date temp = EducGraduationApiUtils.parseDate(actualSessionDate, EducGraduationApiConstants.SECONDARY_DATE_FORMAT);
		String sDates = EducGraduationApiUtils.formatDate(temp, EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
		return toLastDayOfMonth(EducGraduationApiUtils.parseDate(sDates, EducGraduationApiConstants.DEFAULT_DATE_FORMAT));
	}

	public static LocalDate parsingTraxDateLocalDate(String sessionDate) {
		return DateUtils.toLocalDate(parsingTraxDate(sessionDate));
	}

	public static String parsingNFormating(String inDate) {
		String actualDate = StringUtils.countMatches(inDate, "/") == 2 ? inDate : inDate + "/01";
		Date temp = toLastDayOfMonth(EducGraduationApiUtils.parseDate(actualDate, EducGraduationApiConstants.SECONDARY_DATE_FORMAT));
		return EducGraduationApiUtils.formatDate(temp, EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
	}

	public static String getSimpleDateFormat(Date date) {
		if(date == null) {
			return null;
		}
		SimpleDateFormat formatter = new SimpleDateFormat(EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
		return formatter.format(date);
	}

	public static String getSimpleDateFormat(LocalDate date) {
		if(date == null) {
			return null;
		}
		SimpleDateFormat formatter = new SimpleDateFormat(EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
		return formatter.format(DateUtils.toDate(date));
	}

	public static int getDifferenceInMonths(String date1, String date2) {
		if(StringUtils.isAnyBlank(date1, date2)) return 0;
		Period diff = Period.between(
				LocalDate.parse(date1).withDayOfMonth(1),
				LocalDate.parse(date2).withDayOfMonth(1));
		int monthsYear = diff.getYears() * 12;
		int months = diff.getMonths();
		return monthsYear + months;
	}

	static Date toLastDayOfMonth(Date date) {
		if(date != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			return cal.getTime();
		}
		return null;
	}
}
