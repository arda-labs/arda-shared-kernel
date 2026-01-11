package vn.io.arda.shared.util;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for common validation operations.
 * <p>
 * Provides convenience methods for validating strings, collections, emails, URLs,
 * and other common data types without throwing exceptions.
 * </p>
 *
 * <p>Usage examples:</p>
 * <pre>
 * // String validation
 * if (ValidationUtils.isBlank(username)) {
 *     throw new IllegalArgumentException("Username is required");
 * }
 *
 * // Email validation
 * if (!ValidationUtils.isValidEmail(email)) {
 *     throw new IllegalArgumentException("Invalid email format");
 * }
 *
 * // Collection validation
 * if (ValidationUtils.isEmpty(users)) {
 *     return Collections.emptyList();
 * }
 * </pre>
 *
 * @author Arda Development Team
 */
public final class ValidationUtils {

    // Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$"
    );

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    private ValidationUtils() {
        // Utility class - prevent instantiation
    }

    // ========== String Validation ==========

    /**
     * Checks if a string is null or empty.
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Checks if a string is null, empty, or contains only whitespace.
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    /**
     * Checks if a string is not null and not empty.
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Checks if a string is not null, not empty, and contains non-whitespace characters.
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Checks if a string has a minimum length.
     */
    public static boolean hasMinLength(String str, int minLength) {
        return str != null && str.length() >= minLength;
    }

    /**
     * Checks if a string has a maximum length.
     */
    public static boolean hasMaxLength(String str, int maxLength) {
        return str == null || str.length() <= maxLength;
    }

    /**
     * Checks if a string length is within the specified range.
     */
    public static boolean isLengthBetween(String str, int minLength, int maxLength) {
        return hasMinLength(str, minLength) && hasMaxLength(str, maxLength);
    }

    // ========== Collection Validation ==========

    /**
     * Checks if a collection is null or empty.
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if a collection is not null and not empty.
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Checks if a map is null or empty.
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Checks if a map is not null and not empty.
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * Checks if an array is null or empty.
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if an array is not null and not empty.
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    // ========== Pattern Validation ==========

    /**
     * Checks if a string is a valid email address.
     */
    public static boolean isValidEmail(String email) {
        return isNotBlank(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Checks if a string is a valid URL.
     */
    public static boolean isValidUrl(String url) {
        return isNotBlank(url) && URL_PATTERN.matcher(url).matches();
    }

    /**
     * Checks if a string is a valid phone number.
     */
    public static boolean isValidPhone(String phone) {
        return isNotBlank(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Checks if a string contains only alphanumeric characters.
     */
    public static boolean isAlphanumeric(String str) {
        return isNotBlank(str) && ALPHANUMERIC_PATTERN.matcher(str).matches();
    }

    /**
     * Checks if a string matches a custom regex pattern.
     */
    public static boolean matches(String str, String regex) {
        return isNotBlank(str) && Pattern.matches(regex, str);
    }

    // ========== Numeric Validation ==========

    /**
     * Checks if a number is positive (> 0).
     */
    public static boolean isPositive(Number number) {
        return number != null && number.doubleValue() > 0;
    }

    /**
     * Checks if a number is negative (< 0).
     */
    public static boolean isNegative(Number number) {
        return number != null && number.doubleValue() < 0;
    }

    /**
     * Checks if a number is zero.
     */
    public static boolean isZero(Number number) {
        return number != null && number.doubleValue() == 0;
    }

    /**
     * Checks if a number is within a range (inclusive).
     */
    public static boolean isInRange(Number number, Number min, Number max) {
        if (number == null || min == null || max == null) {
            return false;
        }
        double value = number.doubleValue();
        return value >= min.doubleValue() && value <= max.doubleValue();
    }

    // ========== Object Validation ==========

    /**
     * Checks if an object is null.
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * Checks if an object is not null.
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    /**
     * Checks if all objects are not null.
     */
    public static boolean allNotNull(Object... objects) {
        if (objects == null) {
            return false;
        }
        for (Object obj : objects) {
            if (obj == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if any object is null.
     */
    public static boolean anyNull(Object... objects) {
        if (objects == null) {
            return true;
        }
        for (Object obj : objects) {
            if (obj == null) {
                return true;
            }
        }
        return false;
    }

    // ========== Utility Methods ==========

    /**
     * Returns the value if not blank, otherwise returns the default value.
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isNotBlank(str) ? str : defaultValue;
    }

    /**
     * Returns the value if not null, otherwise returns the default value.
     */
    public static <T> T defaultIfNull(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Requires that a string is not blank, throws IllegalArgumentException otherwise.
     */
    public static String requireNotBlank(String str, String message) {
        if (isBlank(str)) {
            throw new IllegalArgumentException(message);
        }
        return str;
    }

    /**
     * Requires that an object is not null, throws IllegalArgumentException otherwise.
     */
    public static <T> T requireNotNull(T obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }
}
