package ca.bc.gov.educ.api.graduation.util;

import java.util.Objects;

public class ThreadLocalStateUtil {
    private static ThreadLocal<String> transaction = new ThreadLocal<>();

    private static ThreadLocal<String> user = new ThreadLocal<>();

    private static ThreadLocal<String> requestSource = new ThreadLocal<>();


    /**
     * Set the requestSource for this thread
     *
     * @param reqSource
     */
    public static void setRequestSource(String reqSource){
        requestSource.set(reqSource);
    }
    /**
     * Get the requestSource for this thread
     *
     * @return the reqSource, or null if it is unknown.
     */
    public static String getRequestSource() {
        return requestSource.get();
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
        requestSource.remove();
    }
}
