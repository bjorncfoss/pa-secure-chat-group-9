import java.io.*;
import java.math.BigInteger;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class Certificate implements Serializable {

    // attributes
    private String id;
    private final PublicKey publicRSAKey;
    private final PrivateKey privateRSAKey;
    
    private static String certificateContent;
    
    // Serial Number
    private static final BigInteger MAX_SERIAL_NUMBER = BigInteger.valueOf(Long.MAX_VALUE);
    private static BigInteger lastSerialNumber;

    // constructor
    public Certificate() throws Exception {

        //this.id = id;

        // Encryption RSA
        KeyPair keyPair = Encryption.generateKeyPair();
        this.publicRSAKey = keyPair.getPublic();
        this.privateRSAKey = keyPair.getPrivate();
    }

    public static void generateCertificate(KeyPair keyPair)
    {
        // Create a serial number generator
        SerialNumberGenerator serialNumberGenerator = new SerialNumberGenerator();

        // Generate a serial number for the certificate
        BigInteger serialNumber = serialNumberGenerator.generateSerialNumber();

        String certificateContent =
            "-----BEGIN CERTIFICATE-----\n" +
            Base64.getMimeEncoder().encodeToString(keyPair.getPrivate().getEncoded()) +
            "\n-----END CERTIFICATE-----\n" +
            "-----BEGIN CERTIFICATE-----\n" +
                    "Serial: " + serialNumber.toString() + "\n" +
                    "Public Key: \n" +
            Base64.getMimeEncoder().encodeToString(keyPair.getPublic().getEncoded()) +
            "\n-----END CERTIFICATE-----\n";

        // Folder to store PEM certificates
        String certificatePath = "certificates/";

        // Create the directory if it doesn't exist
        File directory = new File(certificatePath);
        if (!directory.exists()) {
            directory.mkdirs(); // creates parent directories if not exists
        }

        // Generate unique filename using timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "certificate_" + timeStamp + ".cert";

        // File path including directory
        String filePath = certificatePath + fileName;

        // Verifies if directory and file was created successfully
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(certificateContent);
            System.out.println("Certificate file created successfully at: " + filePath);

            // TODO: Create endpoint here to establish CA Communication

        } catch (IOException e) {
            System.out.println("An error occurred while writing the file: " + e.getMessage());
        }
    }

    public static class SerialNumberGenerator
    {
        public SerialNumberGenerator() {
            // Initialize last serial number to a random value
            lastSerialNumber = new BigInteger(MAX_SERIAL_NUMBER.bitLength(), new SecureRandom());
        }

        public synchronized BigInteger generateSerialNumber() {
            // Increment the last serial number
            lastSerialNumber = lastSerialNumber.add(BigInteger.ONE);

            // Check if the new serial number exceeds the maximum value
            if (lastSerialNumber.compareTo(MAX_SERIAL_NUMBER) > 0) {
                // Reset the serial number to a random value if it exceeds the maximum
                lastSerialNumber = new BigInteger(MAX_SERIAL_NUMBER.bitLength(), new SecureRandom());
            }

            return lastSerialNumber;
        }
    }

    // Getters
    public PublicKey getPublicRSAKey()
    {
        return publicRSAKey;
    }
    public PrivateKey getPrivateRSAKey()
    {
        return privateRSAKey;
    }
}