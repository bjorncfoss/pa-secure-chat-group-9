import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;

/**
 * A class responsible for handling client connections and processing messages.
 */
public class CertificateHandler implements Runnable {

    private final Socket client;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    /**
     * Constructs a CertificateHandler object with the specified client socket, input stream, and output stream.
     *
     * @param client The client socket associated with this handler.
     * @param in The ObjectInputStream to read data from the client.
     * @param out The ObjectOutputStream to write data to the client.
     */
    public CertificateHandler(Socket client, ObjectInputStream in, ObjectOutputStream out) {
        this.client = client;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {

        // Creates thread to display messages
        new Thread(() -> {
            try {
                Message message;
                while((message = (Message) in.readObject()) != null)
                {
                    process(message);
                }
                //process(in, out);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    closeConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * @param message if message is not delivered
     * @throws IOException if an I/O error occurs when opening the socket
     * @throws ClassNotFoundException in case readObject is null
     */
    private void process(Message message) throws IOException, ClassNotFoundException {
        while ((message = (Message) in.readObject()) != null) {
            switch (message.getMessageType()) {
                case USER_MESSAGE:
                    sendMessage(message);
                    break;
                case CERTIFICATE_VALIDATION:
                    validateCertificate(message);
                    break;
            }
        }
    }

    /**
     * Sends a message object to the client.
     *
     * @param messageObj The Message object to be sent.
     * @throws IOException If an I/O error occurs while writing the message.
     */
    private void sendMessage(Message messageObj) throws IOException {
        out.writeObject(messageObj);
        out.flush();
    }

    /**
     * @param messageObj
     * @throws IOException if an I/O error occurs when opening the socket
     */
    private void validateCertificate(Message messageObj) throws IOException {

        String username = messageObj.getSender();
        //Certificate certificate = messageObj.getCertificate();
    }

    /**
     * Checks if a given certificate is valid.
     *
     * @param certificate The Certificate object to be validated.
     * @return true if the certificate is valid; otherwise, false.
     */
    private boolean isCertificateValid(Certificate certificate) {
        // TODO: Implement certificate validation here
        return true;
    }

    /**
     * @throws Exception if the connection is not closing.
     */
    private void closeConnection() throws Exception {
        client.close();
        out.close();
        in.close();
    }

}
