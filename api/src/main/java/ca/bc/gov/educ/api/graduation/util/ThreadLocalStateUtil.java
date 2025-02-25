package ca.bc.gov.educ.api.graduation.util;

import java.util.Objects;

public class ThreadLocalStateUtil {
    private static ThreadLocal<String> transaction = new ThreadLocal<>();

    private static ThreadLocal<String> user = new ThreadLocal<>();

    private static ThreadLocal<String> headerUserNameThread = new ThreadLocal<>();

    private static ThreadLocal<String> requestSourceThread = new ThreadLocal<>();


    /**
     * Set the requestSource for this thread
     *
     * @param requestSource
     */
    public static void setRequestSource(String requestSource){
        requestSourceThread.set(requestSource);
    }
    /**
     * Get the requestSource for this thread
     *
     * @return the requestSource, or null if it is unknown.
     */
    public static String getRequestSource() {
        return requestSourceThread.get();
    }

    /**
     * Set the headerUserName for this thread
     *
     * @param headerUserName
     */
    public static void setHeaderUserName(String headerUserName){
        headerUserNameThread.set(headerUserName);
    }

    /**
     * Get the headerUserName for this thread
     *
     * @return the headerUserName, or null if it is unknown.
     */
    public static String getHeaderUserName() {
        return headerUserNameThread.get();
    }

    /**
     * Set the current correlationID for this thread
     *
     * @param correlationID
     */
    public static void setCorrelationID(String correlationID){
        transaction.set(correlationID);
    }

    /**
     * Get the current correlationID for this thread
     *
     * @return the correlationID, or null if it is unknown.
     */
    public static String getCorrelationID() {
        return transaction.get();
    }

    /**
     * Set the current user for this thread
     *
     * @param currentUser
     */
    public static void setCurrentUser(String currentUser){
        user.set(currentUser);
    }

    /**
     * Get the current user for this thread
     *
     * @return the username of the current user, or null if it is unknown.
     */
    public static String getCurrentUser() {
        return Objects.requireNonNullElse(user.get(), "GRAD");
    }

    public static void clear() {
        transaction.remove();
        user.remove();
    }
}
