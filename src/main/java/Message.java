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
    /**
     * Constructs a Message object by specifying the message bytes that will be sent to the server.
     *
     * @param message the message that is sent to the server
     */
    public Message ( byte[] message, List<String> recipients, String sender  ) {
        this.message = message;
        this.recipients = recipients;
        this.sender=sender;
    }
    public Message (byte[] message) {
        this.message = message;
        this.recipients = new ArrayList<String>();
        this.sender = "";
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