import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

/**
 * A class responsible for handling communication with a single client.
 */
public class ClientHandler implements Runnable {
    private final Socket client;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<String, ObjectOutputStream> clientsList;

    /**
     * Constructs a ClientHandler object with the specified client socket, input stream, output stream, and client list.
     *
     * @param client The client socket associated with this handler.
     * @param in The ObjectInputStream to read data from the client.
     * @param out The ObjectOutputStream to write data to the client.
     * @param clients The HashMap containing the list of connected clients.
     */
    public ClientHandler(Socket client, ObjectInputStream in, ObjectOutputStream out, HashMap<String, ObjectOutputStream> clients)
    {
        this.client = client;
        this.in=in;
        this.out=out;
        this.clientsList = clients;
    }
    
    @Override
    public void run() {
        try {
            process(in, out);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Continuously reads incoming messages from the client and forwards them to the appropriate recipient.
     *
     * @throws IOException If an I/O error occurs while reading or writing data.
     * @throws ClassNotFoundException If the class of a serialized object cannot be found.
     */
    private void process(ObjectInputStream in,ObjectOutputStream out) throws IOException, ClassNotFoundException {
        while (true) {
            Message messageObj = (Message) in.readObject();
            sendMessage(messageObj);
        }
    }

    /**
     * Sends a message object to the appropriate recipient.
     *
     * @param messageObj The Message object to be sent.
     * @throws IOException If an I/O error occurs while writing the message.
     */
    protected void sendMessage( Message messageObj ) throws IOException {
        String recipient = messageObj.getRecipient();
        ObjectOutputStream recipientOutputStream = clientsList.get(recipient);
        if (recipientOutputStream != null) {
            recipientOutputStream.writeObject(messageObj);
            recipientOutputStream.flush();
        } else {
            System.out.println("Recipient " + recipient + " not found.");
        }
    }

    /**
     * Closes the connection with the client and associated streams.
     *
     * @throws IOException If an I/O error occurs while closing the connection.
     */
    private void closeConnection() throws IOException {
        client.close();
        out.close();
        in.close();
    }
}