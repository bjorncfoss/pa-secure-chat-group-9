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
        System.out.println(keyPair.getPublic().getEncoded());
        Certificate certificate = new Certificate(keyPair.getPublic(), "ola");
        System.out.println(certificate.getPublicRSAKey());
        String trollada = certificate.toPEM();
        Certificate certificate2 = new Certificate(null, null);
        certificate2.setValueFromPEM(trollada);
        System.out.println(certificate2.getPublicRSAKey());
        /*
        // Server to Generate Certificate
        Server server = new Server ( 8080 );
        Thread serverThread = new Thread ( server );

        // Initiates Server Thread
        serverThread.start ( );*/
    }
}
