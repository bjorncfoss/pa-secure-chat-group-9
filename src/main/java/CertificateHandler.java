import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

/**
 * A class responsible for handling client connections and processing messages.
 */
public class CertificateHandler implements Runnable {

    private final Socket client;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    /**
     * Constructs a CertificateHandler object with the specified client socket, input stream, and output stream.
     *
     * @param client The client socket associated with this handler.
     * @param keyPair The key pair used for encryption and decryption.
     */
    public CertificateHandler(Socket client, KeyPair keyPair) {
        this.client = client;
        try {
            this.in = new ObjectInputStream(client.getInputStream());
            this.out = new ObjectOutputStream(client.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.publicKey= keyPair.getPublic();
        this.privateKey= keyPair.getPrivate();
    }

    /**
     * Runs the certificate handler thread, responsible for receiving and processing messages.
     * This method continuously listens for incoming messages and processes them accordingly.
     */
    @Override
    public void run() {
        System.out.println("Certificate handler started");
        // Creates thread to display messages
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
    }

    /**
     * Processes the incoming message based on its type.
     *
     * @param message the message to be processed
     * @throws IOException if an I/O error occurs when opening the socket
     * @throws ClassNotFoundException if the object read from the input stream is null
     */
    private void process(Message message) throws IOException, ClassNotFoundException {
        switch (message.getMessageType()) {
            case USER_MESSAGE:
                sendMessage(message);
                break;
            case CERTIFICATE_VALIDATION:
                validateCertificate(message);
                break;
            case SIGN_CERTIFICATE:
                signCertificate(message);
                break;
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
     * Signs a certificate and sends the signed certificate along with the public key to the appropriate recipient.
     *
     * @param messageObj the message containing the filename of the certificate to be signed
     * @throws IOException if an I/O error occurs when reading or deleting the certificate file
     */
    private void signCertificate(Message messageObj) throws IOException {
            String fileName= new String(messageObj.getMessage());
            Path path = Path.of( "src/SignCertificates/" + fileName );
            String fileContent = Files.readString(  path );
            Files.delete( path);
            CertificateEncoder encoder = new CertificateEncoder();
        try {
            Certificate certificate = encoder.decode(  fileContent );
            Certificate newCertificate = createCertificate(certificate);
            byte[] data = newCertificate.getCertificateData();
            byte[] digest = Integrity.generateDigest(data);
            digest = Encryption.encryptRSA(digest,privateKey);
            newCertificate.setSignature(digest);
            System.out.println(newCertificate.getSerialNumber());
            sendMessage(new Message(encoder.encode(newCertificate).getBytes(), newCertificate.getSubject(),newCertificate.getIssuer(),MessageTypes.SIGN_CERTIFICATE ));
            sendMessage(new KeyMessage(publicKey, newCertificate.getSubject(),newCertificate.getIssuer()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new certificate based on the provided certificate, setting the issuer and emission date.
     *
     * @param certificate the original certificate used as a template for the new certificate
     * @return the new certificate with updated issuer and emission date
     */
    private Certificate createCertificate( Certificate certificate)
    {
        //Create a new certificate for the serialNumber be correct;
        Certificate newCertificate = new Certificate(certificate.getPublicRSAKey(),certificate.getSubject());
        newCertificate.setIssuer( "CertificateAuth" );
        newCertificate.setEmissionDate(new Date());
        return newCertificate;
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
