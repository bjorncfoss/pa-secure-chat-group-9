import javax.xml.crypto.Data;
import java.io.Serializable;
import java.util.List;

/**
 * This class represents a message object that is sent to the server by the client.
 */
public class Message implements Serializable {

    private final byte[] message;
    private String recipient;
    private String sender;
    private MessageTypes messageType;

    /**
     * Constructs a Message object by specifying the message bytes that will be sent to the server.
     *
     * @param message the message that is sent to the server
     */
    public Message ( byte[] message, String recipients, String sender, MessageTypes type) {
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
    public MessageTypes getMessageType()
    {
        return this.messageType;
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