import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Client (int port, String nickname) throws Exception {
        client = new Socket ( HOST , port );
        out = new ObjectOutputStream ( client.getOutputStream ( ) );
        in = new ObjectInputStream ( client.getInputStream ( ) );
        this.username = nickname;
        isConnected = true;
        KeyPair keyPair = Encryption.generateKeyPair ( );
        privateKey = keyPair.getPrivate ( );
        publicKey = keyPair.getPublic ( );
    }

    public void execute() throws IOException {
        Scanner usrInput = new Scanner(System.in);
        try {
            if (isConnected) {
                System.out.println("Username:" + username);
                sendMessage(username);
            }
            // Thread for receiving messages
            Thread receiveThread = new Thread(() -> {
                try {
                    while (isConnected) {
                        receiveMessage();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();
            while (isConnected) {
                String message = usrInput.nextLine();
                sendMessage(message);
            }
        } finally {
            closeConnection();
        }
    }
    /*public void execute() throws IOException {
        Scanner usrInput = new Scanner ( System.in );
        try{
            if(isConnected){
                System.out.println("Username:"+ username);
                sendMessage(username);

            }
            while(isConnected){
                //verify messages
                String message = usrInput.nextLine ( );
                sendMessage(message);
                receiveMessage();
            }
            closeConnection();
        } catch (IOException e) {
            closeConnection();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }*/

    public void sendMessage ( String message ) throws IOException {
        //TODO:funcao que passa a string message para o objeto com destinatarios e remetente
        List<String>recipients = extractRecipients ( message );
        String userMessage= extractMessage(message);
        // Creates the message object
        Message messageObj = new Message ( userMessage.getBytes ( ), recipients, username );
        System.out.println ( messageObj.getRecipients() );
        System.out.println ( messageObj.getMessage() );
        // Sends the message
        out.writeObject ( messageObj );
        out.flush();
    }

    public void receiveMessage () throws IOException, ClassNotFoundException {
        Message messageObj = (Message) in.readObject();
        System.out.println(messageObj.getSender()+": "+ new  String(messageObj.getMessage()));
    }

    public static List<String> extractRecipients(String message) {
        List<String> recipients = new ArrayList<>();
        Pattern pattern = Pattern.compile("@(\\w+)");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            recipients.add(matcher.group(1));
        }
        return recipients;
    }

    public static String extractMessage(String message) {
        String messagem = message.replaceAll("@\\w+(,\\s*@\\w+)*", "").trim();
        return messagem;
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
