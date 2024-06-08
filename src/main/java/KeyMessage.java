import java.security.PublicKey;


/**
 * The KeyMessage class represents a message containing a public key, which is used for communication.
 * It extends the Message class.
 */
public class KeyMessage extends Message {

    /**
     * Constructs a KeyMessage object with the specified public key, recipients, and sender.
     *
     * @param publicKey the public key to be included in the message
     * @param recipients the recipients of the message
     * @param sender the sender of the message
     */
    public KeyMessage(PublicKey publicKey, String recipients, String sender) {
        super("publickey".getBytes(), recipients, sender, MessageTypes.KEY_MESSAGE);
        this.publicKey = publicKey;
    }

    private PublicKey publicKey;

    /**
     * Gets the public key included in the KeyMessage.
     *
     * @return the public key
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }
}
