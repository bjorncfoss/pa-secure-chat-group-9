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

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

/**
 * This class represents the sender of the message. It sends the message to the receiver by means of a socket. The use
 * of Object streams enables the sender to send any kind of object.
 */
public class Client {

    private static final String HOST = "0.0.0.0";


    private final Socket client;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    private final BigInteger privateDHKey;
    private final BigInteger publicDHKey;

    private final Certificate certificate;

    private String username;
    private boolean isConnected;


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

        this.privateDHKey = DiffieHellman.generatePrivateKey ( );
        this.publicDHKey = DiffieHellman.generatePublicKey ( this.privateDHKey );

        this.certificate = new Certificate(username, publicDHKey, privateDHKey);
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
                    //while (isConnected) {
                    Message message;
                    while((message = (Message) in.readObject()) != null) {
                        receiveMessage(message);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();
            while (isConnected) {

                System.out.print("Message...");

                String message = usrInput.nextLine();
                sendMessage(message);
            }
        } finally {
            closeConnection();
        }
    }

    /**
     * Sends a message to the receiver using the OutputStream of the socket. The message is sent as an object of the
     * {@link Message} class.
     *
     * @param message the message to send
     *
     * @throws IOException when the encryption or the integrity generation fails
     */
    public void sendMessage ( String message ) throws IOException {
        List<String>recipients = extractRecipients ( message );
        String userMessage= extractMessage(message);
        // Creates the message object
        Message messageObj = new Message ( userMessage.getBytes ( ), recipients, username, Message.messageType.USER_MESSAGE, certificate );
        // Sends the message
        out.writeObject ( messageObj );
        out.flush();
    }

    public void receiveMessage (Message messageObj) throws IOException, ClassNotFoundException {
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

    public BigInteger getPublicDHKey() {
        return publicDHKey;
    }
}
