import java.math.BigInteger;
import java.net.ServerSocket;
import java.security.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * The CertificateServer class represents a server for handling certificate-related operations.
 * This server listens for incoming connections from clients and spawns a new CertificateHandler thread
 * for each client connection.
 */
public class CertificateServer {
    /**
     * The main method of the CertificateServer class.
     * It initializes the server's key pair, listens for incoming client connections on a specific port,
     * and spawns a CertificateHandler thread for each client connection.
     *
     * @param args command line arguments (not used)
     * @throws Exception if an error occurs during the server operation
     */
    public static void main ( String[] args ) throws Exception {
        KeyPair keyPair = Encryption.generateKeyPair();
        try (ServerSocket socket = new ServerSocket( 8100 ))
        {
            Socket client;
            do
            {
                client = socket.accept();
                Thread thread = new Thread(new CertificateHandler(client,keyPair));
                thread.start();
            }while( true );
        }
        catch (IOException e)
        {
            System.out.println("Socket Closed: " + e.getMessage());
        }
    }
}
