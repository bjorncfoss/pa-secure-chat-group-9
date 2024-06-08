/**
 * Enumerated type representing different types of messages.
 */
public enum MessageTypes {
    /**
     * Represents a user message.
     */
    USER_MESSAGE,

    /**
     * Represents a certificate validation message.
     */
    CERTIFICATE_VALIDATION,

    /**
     * Represents the certificate signature.
     */
    SIGN_CERTIFICATE,

    /**
     * Represents the register procedure.
     */
    REGISTER,

    /**
     * Represents the login procedure.
     */
    LOGIN,

    /**
     * Represents the logout procedure.
     */
    LOGOUT,

    /**
     * Represents an error message.
     */
    ERROR,

    /**
     * Represents a message containing certificates of logged-in users.
     */
    LOGGED_USERS,

    /**
     * Represents a user login notification.
     */
    USER_LOGIN,

    /**
     * Represents a user logout notification.
     */
    USER_LOGOUT,

    /**
     * Represents a message containing a public key.
     */
    KEY_MESSAGE,
}
