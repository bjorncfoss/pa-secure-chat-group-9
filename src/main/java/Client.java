import java.beans.Encoder;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the sender of the message. It sends the message to the receiver by means of a socket. The use
 * of Object streams enables the sender to send any kind of object.
 */
public class Client {

    private static final String HOST = "0.0.0.0";
    private final Socket client;
    private final ObjectInputStream MSGin;
    private final ObjectOutputStream MSGout;
    private final Socket CA;
    private final ObjectInputStream CAin;
    private final ObjectOutputStream CAout;
    private boolean isConnected;
    private String nickname;
    private PublicKey RSAPublicKey;
    private PrivateKey RSAPrivateKey;
    private String certificate;
    private PublicKey CAPublicKey;
    private ArrayList<User> userList;

    /**
     * Constructs a Client object by specifying the port and the CA port to connect to.
     * The sockets must be created before the client can send a message.
     *
     * @param port the port to connect to
     * @param portCA the port to connect to the Certification Authority (CA)
     *
     * @throws IOException when an I/O error occurs when creating the socket
     * @throws Exception when an error occurs during key pair generation
     */
    public Client (int port, int  portCA) throws Exception {
        client = new Socket ( HOST , port );
        MSGout = new ObjectOutputStream ( client.getOutputStream ( ) );
        MSGin = new ObjectInputStream ( client.getInputStream ( ) );
        CA = new Socket(HOST, portCA);
        CAout = new ObjectOutputStream ( CA.getOutputStream ( ) );
        CAin = new ObjectInputStream ( CA.getInputStream ( ) );
        isConnected = true;
        KeyPair keyPair= Encryption.generateKeyPair();
        this.RSAPrivateKey= keyPair.getPrivate();
        this.RSAPublicKey= keyPair.getPublic();
        this.userList= new ArrayList<>();
    }


    /**
     * Executes the client's operations including registration, certificate generation,
     * and login. It starts a thread for receiving messages and handles sending messages
     * from user input until the connection is terminated.
     *
     * @throws Exception if an error occurs during the execution process
     */
    public void execute() throws Exception {
        nickname = registerName();
        certificate = generateCertificate();
        login();

        try {
            // Thread for receiving messages
            Thread receiveThread = new Thread(() -> {
                try {
                    while (isConnected) {
                        receiveMessage();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            receiveThread.start();
            Scanner msgInput = new Scanner(System.in);
            while (isConnected) {
                String msg = msgInput.nextLine();
                sendMessage(msg);
            }

        } finally {
            closeConnection();
        }
    }


    /**
     * Requests the public key from the Certification Authority (CA).
     *
     * @return the public key obtained from the CA
     * @throws IOException if an I/O error occurs while reading from the input stream
     * @throws ClassNotFoundException if the class of the serialized object could not be found
     * @throws RuntimeException if the received message type is not a key message
     */
    private PublicKey askCAPublicKey() throws IOException, ClassNotFoundException {
        Message message = (Message) CAin.readObject();
        if(message.getMessageType().equals(MessageTypes.KEY_MESSAGE)){
            return ((KeyMessage) message).getPublicKey();
        }
        throw new RuntimeException();
    }

    /**
     * Registers a nickname for the user.
     *
     * @return the registered nickname
     * @throws IOException if an I/O error occurs while reading or writing to the input or output streams
     * @throws ClassNotFoundException if the class of the serialized object could not be found
     */
    private String registerName() throws IOException, ClassNotFoundException {
        System.out.println("Choose your nickname:");
        Scanner usrInput = new Scanner(System.in);
        while (true){
            String input= usrInput.nextLine();
            if(input.isEmpty()){
                System.out.println("input is empty try again");
                continue;
            }
            MSGout.writeObject(new Message(input.getBytes(),"Server","new user",MessageTypes.REGISTER));
            Message message = (Message) MSGin.readObject();
            if (message.getMessageType()==MessageTypes.ERROR){
                System.out.println(message.getSender()+": "+new String(message.getMessage()));
            }
            else if(message.getMessageType()==MessageTypes.REGISTER){
                System.out.println("Registered username: "+input);
                return input;
            }else{
                System.out.println("Invalid message received"+message.getMessageType());
            }
        }
    }

    /**
     * Generates a certificate for the user and sends it to the Certification Authority (CA) for signing.
     *
     * @return the response message from the CA after sending the certificate
     * @throws RuntimeException if an I/O error occurs while writing to the file, or if a ClassNotFoundException occurs during object deserialization
     */
    private String generateCertificate(){
        Certificate certificate = new Certificate(RSAPublicKey, nickname);
        try(FileWriter fileWriter= new FileWriter("src/SignCertificates/"+nickname+".pem")){
            CertificateEncoder encoder = new CertificateEncoder();
            fileWriter.write(encoder.encode(certificate));
            fileWriter.close();
            CAout.writeObject(new Message((nickname+".pem").getBytes(),"CA",nickname,MessageTypes.SIGN_CERTIFICATE));
            Message message = (Message) CAin.readObject();
            CAPublicKey = askCAPublicKey();
            return new String(message.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Logs the user into the server. This method sends the user's certificate to the server
     * for authentication and retrieves the list of connected users upon successful login.
     *
     * @throws Exception if an error occurs during the login process, such as I/O errors
     *                   or certificate validation errors
     */
    private void login() throws Exception {
        Message message = new Message(certificate.getBytes(),"Server",nickname,MessageTypes.LOGIN);
        MSGout.writeObject(message);
        Message msg= (Message) MSGin.readObject();
        if(new String(msg.getMessage()).equals("Sucess")){
            msg = (Message) MSGin.readObject();
            CertificateEncoder encoder = new CertificateEncoder();
            String certificateList= new String(msg.getMessage());
            if(!certificateList.isEmpty()){
                String[] certificates =certificateList.substring(0,certificateList.length()-1).split(",");
                for( String certificate : certificates){
                    if (validateCertificate(certificate,encoder)){
                        String username = encoder.decode(certificate).getSubject();
                        userList.add(new User(username,null,certificate));
                        System.out.println(new Date()+" User: "+username+" has connected");
                    }
                }
            }else{
                System.out.println("You are the only user connected");
            }
        }
    }

    /**
     * Validates a PEM encoded certificate by comparing its integrity digest with the decrypted original digest
     * obtained from the CA's public key.
     *
     * @param pemCertificate the PEM encoded certificate to be validated
     * @param encoder        the certificate encoder used for decoding the certificate
     * @return true if the certificate is valid, false otherwise
     * @throws Exception if an error occurs during the certificate validation process, such as decoding errors or RSA decryption errors
     */
    private boolean validateCertificate(String pemCertificate, CertificateEncoder encoder) throws Exception {
        Certificate certificate = encoder.decode(pemCertificate);
        byte[] digest = Integrity.generateDigest(certificate.getCertificateData());
        byte[] originalDigest = Encryption.decryptRSA(certificate.getSignature(),CAPublicKey);
        return Arrays.equals(digest,originalDigest);
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
                Message messageObj = new Message(userMessage.getBytes(), recipient, nickname, MessageTypes.USER_MESSAGE);
                // Sends the message
                MSGout.writeObject(messageObj);
            }
        }else{
            System.out.println("Invalid User");
        }
    }


    /**
     * Reads a Message object from the input stream and prints the sender and message content if the message type is USER_MESSAGE.
     *
     * @throws IOException If an I/O error occurs while reading the message.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    public void receiveMessage () throws Exception {
        Message messageObj = (Message) MSGin.readObject();
        switch (messageObj.getMessageType()) {
            case USER_MESSAGE->{
                System.out.println(new Date() +" "+ messageObj.getSender()+": "+ new String(messageObj.getMessage()));
            }
            case USER_LOGIN -> {
                userLogin(messageObj);
            }
            case USER_LOGOUT -> {
                userLogout(messageObj);
            }
        }
    }

    /**
     * Handles the login of a user based on the received message containing the user's certificate.
     *
     * @param messageObj the message containing the user's certificate
     * @throws Exception if an error occurs during the user login process, such as certificate validation errors
     */
    private void userLogin(Message messageObj) throws Exception {
        String certificate = new String(messageObj.getMessage());
        CertificateEncoder encoder = new CertificateEncoder();
        String username = encoder.decode(certificate).getSubject();
        if(validateCertificate(certificate,encoder)){
            userList.add(new User(username,null,certificate));
            System.out.println(new Date() +" User: "+username+" has logged in");
        }else{
            System.out.println("Invalid certificate");
        }

    }

    /**
     * Handles the logout of a user based on the received message containing the user's username.
     *
     * @param messageObj the message containing the user's username
     * @throws Exception if an error occurs during the user logout process
     */
    private void userLogout(Message messageObj) throws Exception {
        String username = new String(messageObj.getMessage());
        if(userList.remove(username)){
            System.out.println(new Date() +"User: "+username+" has logged out");
        }else{
            System.out.println("invalid username to remove");
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
        MSGout.close ( );
        MSGin.close ( );
    }

}
