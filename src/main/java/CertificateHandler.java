import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class CertificateHandler implements Runnable {

    private final Socket client;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public CertificateHandler(Socket client, ObjectInputStream in, ObjectOutputStream out) {
        this.client = client;
        this.in=in;
        this.out=out;
    }

    @Override
    public void run() {
        try {
            process(in);
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

    private void process(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Message message;
        while ((message = (Message) in.readObject()) != null) {

            switch(message.getMessageType())
            {
                case USER_MESSAGE:
                    break;
                case CERTIFICATE_VALIDATION:
                    break;
            }

            //Message messageObj = (Message) in.readObject();
            System.out.println(new String(message.getMessage()));
            sendMessage(message);
        }
    }

    private void sendMessage( Message messageObj ) throws IOException {

        out.writeObject(messageObj);    //sends the message
    }

    private void closeConnection() throws IOException {
        client.close();
        out.close();
        in.close();
    }
}
