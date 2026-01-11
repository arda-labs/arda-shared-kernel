package vn.io.arda.shared.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * Utility class for Java 21 Date and Time operations.
 * <p>
 * Provides convenience methods for working with LocalDate, LocalDateTime, ZonedDateTime,
 * and Instant, including formatting, parsing, and common date/time calculations.
 * </p>
 *
 * <p>Usage examples:</p>
 * <pre>
 * // Format current time
 * String iso = DateTimeUtils.formatIso(Instant.now());
 *
 * // Parse ISO date
 * Instant instant = DateTimeUtils.parseIso("2024-01-15T10:30:00Z");
 *
 * // Calculate age
 * int age = DateTimeUtils.calculateAge(LocalDate.of(1990, 5, 15));
 *
 * // Get start/end of day
 * Instant startOfDay = DateTimeUtils.startOfDay(Instant.now());
 * Instant endOfDay = DateTimeUtils.endOfDay(Instant.now());
 * </pre>
 *
 * @author Arda Development Team
 */
public final class DateTimeUtils {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DateTimeUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Formats an Instant to ISO-8601 string (e.g., "2024-01-15T10:30:00Z").
     */
    public static String formatIso(Instant instant) {
        return instant != null ? ISO_FORMATTER.format(instant) : null;
    }

    /**
     * Formats a LocalDateTime to ISO-8601 string (e.g., "2024-01-15T10:30:00").
     */
    public static String formatIso(LocalDateTime dateTime) {
        return dateTime != null ? DATETIME_FORMATTER.format(dateTime) : null;
    }

    /**
     * Formats a LocalDate to ISO-8601 string (e.g., "2024-01-15").
     */
    public static String formatIso(LocalDate date) {
        return date != null ? DATE_FORMATTER.format(date) : null;
    }

    /**
     * Parses an ISO-8601 string to Instant.
     */
    public static Instant parseIso(String isoString) {
        return isoString != null ? Instant.parse(isoString) : null;
    }

    /**
     * Parses an ISO-8601 string to LocalDateTime.
     */
    public static LocalDateTime parseLocalDateTime(String isoString) {
        return isoString != null ? LocalDateTime.parse(isoString, DATETIME_FORMATTER) : null;
    }

    /**
     * Parses an ISO-8601 string to LocalDate.
     */
    public static LocalDate parseLocalDate(String isoString) {
        return isoString != null ? LocalDate.parse(isoString, DATE_FORMATTER) : null;
    }

    /**
     * Converts Instant to LocalDateTime using system default timezone.
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, DEFAULT_ZONE) : null;
    }

    /**
     * Converts LocalDateTime to Instant using system default timezone.
     */
    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atZone(DEFAULT_ZONE).toInstant() : null;
    }

    /**
     * Converts LocalDate to Instant (start of day) using system default timezone.
     */
    public static Instant toInstant(LocalDate localDate) {
        return localDate != null ? localDate.atStartOfDay(DEFAULT_ZONE).toInstant() : null;
    }

    /**
     * Gets the start of day (00:00:00) for the given Instant.
     */
    public static Instant startOfDay(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(DEFAULT_ZONE)
                .toLocalDate()
                .atStartOfDay(DEFAULT_ZONE)
                .toInstant();
    }

    /**
     * Gets the end of day (23:59:59.999999999) for the given Instant.
     */
    public static Instant endOfDay(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(DEFAULT_ZONE)
                .toLocalDate()
                .atTime(LocalTime.MAX)
                .atZone(DEFAULT_ZONE)
                .toInstant();
    }

    /**
     * Gets the start of month for the given Instant.
     */
    public static Instant startOfMonth(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(DEFAULT_ZONE)
                .with(TemporalAdjusters.firstDayOfMonth())
                .toLocalDate()
                .atStartOfDay(DEFAULT_ZONE)
                .toInstant();
    }

    /**
     * Gets the end of month for the given Instant.
     */
    public static Instant endOfMonth(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(DEFAULT_ZONE)
                .with(TemporalAdjusters.lastDayOfMonth())
                .toLocalDate()
                .atTime(LocalTime.MAX)
                .atZone(DEFAULT_ZONE)
                .toInstant();
    }

    /**
     * Calculates age in years from a birth date to current date.
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Calculates the number of days between two Instants.
     */
    public static long daysBetween(Instant start, Instant end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculates the number of hours between two Instants.
     */
    public static long hoursBetween(Instant start, Instant end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * Checks if the given Instant is in the past.
     */
    public static boolean isPast(Instant instant) {
        return instant != null && instant.isBefore(Instant.now());
    }

    /**
     * Checks if the given Instant is in the future.
     */
    public static boolean isFuture(Instant instant) {
        return instant != null && instant.isAfter(Instant.now());
    }

    /**
     * Checks if the given date is today.
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }

    /**
     * Adds days to an Instant.
     */
    public static Instant plusDays(Instant instant, long days) {
        return instant != null ? instant.plus(days, ChronoUnit.DAYS) : null;
    }

    /**
     * Adds hours to an Instant.
     */
    public static Instant plusHours(Instant instant, long hours) {
        return instant != null ? instant.plus(hours, ChronoUnit.HOURS) : null;
    }

    /**
     * Subtracts days from an Instant.
     */
    public static Instant minusDays(Instant instant, long days) {
        return instant != null ? instant.minus(days, ChronoUnit.DAYS) : null;
    }

    /**
     * Subtracts hours from an Instant.
     */
    public static Instant minusHours(Instant instant, long hours) {
        return instant != null ? instant.minus(hours, ChronoUnit.HOURS) : null;
    }

    /**
     * Gets the current Instant (UTC).
     */
    public static Instant now() {
        return Instant.now();
    }

    /**
     * Gets the current LocalDate.
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Gets yesterday's LocalDate.
     */
    public static LocalDate yesterday() {
        return LocalDate.now().minusDays(1);
    }

    /**
     * Gets tomorrow's LocalDate.
     */
    public static LocalDate tomorrow() {
        return LocalDate.now().plusDays(1);
    }
}
