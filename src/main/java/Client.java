import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.math.BigInteger;

/**
 * This class represents the sender of the message. It sends the message to the receiver by means of a socket. The use
 * of Object streams enables the sender to send any kind of object.
 */
public class Client {

    private static final String HOST = "0.0.0.0";

    private final Socket client;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

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

        //this.privateDHKey = DiffieHellman.generatePrivateKey ( );
        //this.publicDHKey = DiffieHellman.generatePublicKey ( this.privateDHKey );

        //this.certificate = new Certificate();
    }


    /**
     * @throws IOException if an I/O error occurs when opening the socket
     */
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

                // Get the current date and time
                //LocalDateTime now = LocalDateTime.now();
                // Format the date and time
                //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                // Get the formatted date and time
                //String formattedDateTime = now.format(formatter);
                //System.out.print(formattedDateTime + " ");

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
        if (!recipients.isEmpty()) {
            for (String recipient : recipients) {
                Message messageObj = new Message(userMessage.getBytes(), recipient, username, Message.messageType.USER_MESSAGE);
                // Sends the message
                out.writeObject(messageObj);
                out.flush();
            }
        }else{
            Message messageObj = new Message(message.getBytes(),"",username, Message.messageType.USER_MESSAGE);
            // Sends the message
            out.writeObject(messageObj);
            out.flush();
        }
    }

    /**
     * Reads a Message object from the input stream and prints the sender and message content if the message type is USER_MESSAGE.
     *
     * @throws IOException If an I/O error occurs while reading the message.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    public void receiveMessage () throws IOException, ClassNotFoundException {
        Message messageObj = (Message) in.readObject();
        if(messageObj.getMessageType()==Message.messageType.USER_MESSAGE) {
            System.out.println(messageObj.getSender()+": "+ new  String(messageObj.getMessage()));
        }else{
            System.out.println("Invalid message type");
        }
    }

    /**
     * Extracts recipients mentioned in the message string using regular expressions.
     *
     * @param message The message string containing recipient mentions.
     * @return A list of recipients extracted from the message.
     */
    public static List<String> extractRecipients(String message) {
        List<String> recipients = new ArrayList<>();
        Pattern pattern = Pattern.compile("@(\\w+)");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            recipients.add(matcher.group(1));
        }
        return recipients;
    }

    /**
     * Extracts the message content from the message string by removing recipient mentions.
     *
     * @param message The message string containing recipient mentions.
     * @return The message content without recipient mentions.
     */
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
