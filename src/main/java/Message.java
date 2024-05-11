import java.io.Serializable;
import java.util.List;

/**
 * This class represents a message object that is sent to the server by the client.
 */
public class Message implements Serializable {

    private final byte[] message;
    private String recipient;
    private String sender;
    private messageType messageType;

    /**
     * Constructs a Message object by specifying the message bytes that will be sent to the server.
     *
     * @param message the message that is sent to the server
     */
    public Message ( byte[] message, String recipients, String sender, messageType type) {
        this.message = message;
        this.recipient = recipients;
        this.sender=sender;
        this.messageType = type;
    }

    /**
     * Returns the type of the message.
     *
     * @return The type of the message as a messageType enum value.
     */
    public messageType getMessageType()
    {
        return this.messageType;
    }

    /**
     * Enumerated type representing different types of messages.
     */
    public static enum messageType
    {
        /**
         * Represents a user message.
         */
        USER_MESSAGE,

        /**
         * Represents a certificate validation message.
         */
        CERTIFICATE_VALIDATION
    }

    /**
     * Gets the message string.
     *
     * @return the message string
     */
    public byte[] getMessage ( ) {
        return message;
    }
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
}