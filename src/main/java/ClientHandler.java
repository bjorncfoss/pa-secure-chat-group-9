import java.beans.Encoder;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class responsible for handling communication with a single client.
 */
public class ClientHandler implements Runnable {
    private final Socket client;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ConcurrentHashMap<String, User> clientsList;
    private ArrayList<String> registeredClients;
    private ReentrantLock registeredClientsLock;
    private boolean connected = false;

    /**
     * Constructs a ClientHandler object with the specified client socket, input stream, output stream, and client list.
     *
     * @param client The client socket associated with this handler.
     * @param clients The HashMap containing the list of connected clients.
     */
    public ClientHandler(Socket client, ConcurrentHashMap<String, User> clients, ArrayList<String> registeredClients, ReentrantLock clientsLock) throws IOException {
        this.client = client;
        this.in=new ObjectInputStream(client.getInputStream());
        this.out=new ObjectOutputStream(client.getOutputStream());
        this.clientsList = clients;
        this.registeredClients = registeredClients;
        this.registeredClientsLock= clientsLock;
        connected = true;
    }

    /**
     * Runs the thread responsible for handling client requests.
     * This method continuously listens for incoming messages from the client,
     * processes them, and responds accordingly.
     */
    @Override
    public void run() {
        try {
            //process(in, out);
            while (connected) {
                Message message = (Message) in.readObject();
                handleRequest(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    /**
     * Handles the incoming message based on its type.
     *
     * @param message the message to be handled
     * @throws IOException if an I/O error occurs during message handling
     */
    private void handleRequest(Message message) throws IOException {
        switch (message.getMessageType()){
            case LOGIN -> {
                login(message);
            }
            case LOGOUT -> {
                logout(message);
            }
            case REGISTER -> {
                register(message);
            }
            case USER_MESSAGE -> {
                sendMessage(message);
            }
        }
    }

    /**
     * Sends a message to the specified recipients.
     *
     * @param message the message to be sent
     * @throws IOException if an I/O error occurs while sending the message
     */
    private void sendMessage(Message message) throws IOException {
        String[] recipients= message.getRecipient().split(", ");
        for(String recipient : recipients){
            User user= clientsList.get(recipient);
            if(user.getOut()!=null){
                user.getOut().writeObject(message);
            }
        }
    }

    /**
     * Sends a broadcast message of the specified type to all connected clients except the sender.
     *
     * @param message the message to be broadcasted
     * @param type the type of the broadcast message
     * @throws IOException if an I/O error occurs while sending the broadcast message
     */
    private void sendBroadcast(Message message, MessageTypes type) throws IOException {
        for(Map.Entry <String, User> entry : clientsList.entrySet()){
            if(!entry.getKey().equals(message.getSender())){
                User user= entry.getValue();
                user.getOut().writeObject(new Message(message.getMessage(), message.getSender(), user.getName(),type));
            }
        }
    }

    /**
     * Registers a new client with the server.
     *
     * @param message the message containing the new client's name
     * @throws IOException if an I/O error occurs during the registration process
     */
    private void register(Message message) throws IOException {
        String newName= new String(message.getMessage());
        registeredClientsLock.lock();
        if(!registeredClients.contains(newName)){
            registeredClients.add(newName);
            registeredClientsLock.unlock();
            out.writeObject(new Message("Sucess".getBytes(), message.getSender(), "Server",MessageTypes.REGISTER));
        }else{
            registeredClientsLock.unlock();
            out.writeObject(new Message("Failed".getBytes(), message.getSender(), "Server",MessageTypes.ERROR));
        }
    }

    /**
     * Logs in a client with the server using the provided certificate.
     *
     * @param message the message containing the encoded certificate for login
     * @throws IOException if an I/O error occurs during the login process
     */
    private void login(Message message) throws IOException {
        String encodedCertificate= new String(message.getMessage());
        try {
            Certificate certificate= new CertificateEncoder().decode(encodedCertificate);

            registeredClientsLock.lock();
            if(registeredClients.contains(certificate.getSubject()) && !clientsList.containsKey(certificate.getSubject())){
                clientsList.put(certificate.getSubject(),new User(certificate.getSubject(),out,encodedCertificate));
                registeredClientsLock.unlock();
                out.writeObject(new Message("Sucess".getBytes(), message.getSender(), "Server",MessageTypes.LOGIN));
                StringBuilder certificates= new StringBuilder();
                for(User user : clientsList.values()){
                    if (user.getName().equals(certificate.getSubject())){
                     continue;
                    }
                    certificates.append(user.getCertificate()).append(",");
                }
                out.writeObject(new Message(certificates.toString().getBytes(), message.getSender(), "Server",MessageTypes.LOGGED_USERS));

                sendBroadcast(message,MessageTypes.USER_LOGIN);
            }else{
                registeredClientsLock.unlock();
                out.writeObject(new Message("Failed".getBytes(), message.getSender(), "Server",MessageTypes.ERROR));
            }

        } catch (ClassNotFoundException e) {
            System.out.println("Invalid certificate");
            out.writeObject(new Message("Invalid Certificate".getBytes(), message.getSender(), "Server",MessageTypes.ERROR));
            return;
        }
    }

    /**
     * Logs out a client from the server.
     *
     * @param message the message containing the client's name for logout
     * @throws IOException if an I/O error occurs during the logout process
     */
    private void logout(Message message) throws IOException {
        String content = new String(message.getMessage());
        registeredClientsLock.lock();
        registeredClients.remove(content);
        registeredClientsLock.unlock();
        clientsList.remove(content);
        sendBroadcast(message,MessageTypes.USER_LOGOUT);
        closeConnection();
    }

    /**
     * Closes the connection with the client and associated streams.
     *
     * @throws IOException If an I/O error occurs while closing the connection.
     */
    private void closeConnection() {
        try {
            client.close();
            out.close();
            in.close();
            connected=false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}