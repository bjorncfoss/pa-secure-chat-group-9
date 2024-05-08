import java.io.*;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class Certificate implements Serializable {

    // attributes
    private final String id;
    private final BigInteger publicRSAKey;
    private final BigInteger privateRSAKey;

    private String certificateContent;

    // constructor
    public Certificate(String id, BigInteger publicKey, BigInteger privateKey)
    {
        this.id = id;
        this.publicRSAKey = publicKey;
        this.privateRSAKey = privateKey;
        setContent();
    }

    // shows the certificate for public and private key
    public void setContent ()
    {
        StringBuilder sb = new StringBuilder("-----BEGIN CERTIFICATE-----\n");
        sb.append(privateRSAKey + "\n");
        sb.append("-----END CERTIFICATE-----\n");
        this.certificateContent = sb.toString();

        StringBuilder sb2 = new StringBuilder("-----BEGIN CERTIFICATE-----\n");
        sb.append(publicRSAKey + "\n");
        sb.append("-----END CERTIFICATE-----\n");
        this.certificateContent += sb2.toString();
    }

    // Generates 'certificates' folder on project folder
    // This folder will store the .cert files
    public static String certFolder()
    {
        // Name of the folder to be generated
        String path = "/certificates";
        File file = new File(path);

        if (!file.exists())
        {
            file.mkdir();
        }

        return file.getPath();
    }

    public static void generateCertificate(String folderPath, String fileName, String content)
    {
        File file = new File(folderPath + File.separator + fileName + ".cert");

        try {
            FileWriter writer = new FileWriter(file);

            writer.write(content);
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void generateSampleCert() throws Exception
    {
        String folderPath = certFolder();
        generateCertificate(folderPath, "certificate", "This is a sample .cert file.");
    }
}