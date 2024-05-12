import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket client;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<String, ObjectOutputStream> clientsList;

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

    private void process(ObjectInputStream in,ObjectOutputStream out) throws IOException, ClassNotFoundException {
        while (true) {
            Message messageObj = (Message) in.readObject();
            sendMessage(messageObj);
        }
    }
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

    protected void closeConnection() throws IOException {
        client.close();
        out.close();
        in.close();
    }
}