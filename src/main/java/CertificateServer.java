import java.math.BigInteger;
import java.security.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class CertificateServer {

    // Server CA - generates key for the CA
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
