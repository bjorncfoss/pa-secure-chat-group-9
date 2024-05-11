import java.math.BigInteger;
import java.security.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * A class representing the Certificate Authority (CA) server.
 */
public class CertificateServer {

    /**
     * The main method to start the CA server and generate certificates.
     *
     * @param args The command-line arguments (not used).
     * @throws Exception If an error occurs while starting the server or generating certificates.
     */
    public static void main ( String[] args ) throws Exception {

        // Generates Certificate
        KeyPair keyPair = Encryption.generateKeyPair();
        Certificate.generateCertificate(keyPair);

        // Server to Generate Certificate
        Server server = new Server ( 8080 );
        Thread serverThread = new Thread ( server );

        // Initiates Server Thread
        serverThread.start ( );
    }

}
