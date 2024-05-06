import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a message object that is sent to the server by the client.
 */
public class Message implements Serializable {

    private final byte[] message;
    private List<String> recipients;
    private String sender;

    private messageType messageType;

    /**
     * Constructs a Message object by specifying the message bytes that will be sent to the server.
     *
     * @param message the message that is sent to the server
     */
    public Message ( byte[] message, List<String> recipients, String sender, messageType type  ) {
        this.message = message;
        this.recipients = recipients;
        this.sender=sender;
        this.messageType = type;
    }

    public messageType getMessageType()
    {
        return this.messageType;
    }

    public static enum messageType
    {
        USER_MESSAGE, CERTIFICATE_VALIDATION
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
    public List<String> getRecipients() { return recipients; }
}