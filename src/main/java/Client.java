import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

/**
 * This class represents the sender of the message. It sends the message to the receiver by means of a socket. The use
 * of Object streams enables the sender to send any kind of object.
 */
public class Client {

    private static final String HOST = "0.0.0.0";
    private final Socket client;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private PublicKey publicKey = null;
    private PrivateKey privateKey = null;
    private String username;
    private boolean isConnected;

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
    /**
     * Constructs a Sender object by specifying the port to connect to. The socket must be created before the sender can
     * send a message.
     *
     * @param port the port to connect to
     *
     * @throws IOException when an I/O error occurs when creating the socket
     */
    public Client (int port) throws Exception {
        client = new Socket ( HOST , port );
        out = new ObjectOutputStream ( client.getOutputStream ( ) );
        in = new ObjectInputStream ( client.getInputStream ( ) );
        this.username = "";
        isConnected = true;
        KeyPair keyPair = Encryption.generateKeyPair ( );
        privateKey = keyPair.getPrivate ( );
        publicKey = keyPair.getPublic ( );
    }

    public void execute() throws IOException {
        Scanner usrInput = new Scanner ( System.in );
        try{
            if(isConnected){
                System.out.println ( "Insert username");
                username = usrInput.nextLine();
                System.out.println("Username:"+ username);
            }
            while(isConnected){
                //verify messages
                System.out.println ( "Envie a mensagem");
                String message = usrInput.nextLine ( );
                System.out.println ( "enviada");
                sendMessage ( message );
                receiveMessage();
            }
            closeConnection();
        } catch (IOException e) {
            closeConnection();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage ( String message ) throws IOException {
        // Creates the message object
        Message messageObj = new Message ( message.getBytes ( ) );
        // Sends the message
        out.writeObject ( messageObj );
        out.flush();
    }

    public void receiveMessage () throws IOException, ClassNotFoundException {
        Message messageObj = (Message) in.readObject();
        System.out.println(new String(messageObj.getMessage()));
    }
    /**
     * Closes the connection by closing the socket and the streams.
     *
     * @throws IOException when an I/O error occurs when closing the connection
     */
    private void closeConnection ( ) throws IOException {
        client.close ( );
        out.close ( );
        in.close ( );
    }

}
