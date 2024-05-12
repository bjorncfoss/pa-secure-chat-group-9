import java.io.*;
import java.math.BigInteger;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.security.spec.RSAPublicKeySpec;

/**
 * Represents a certificate.
 */
public class Certificate implements Serializable {

    // attributes
    private PublicKey publicRSAKey;
    private static String certificateContent;

    // Serial Number
    private BigInteger serialNumber;
    private String subject;
    private String issuer;
    private byte[] signature;
    private Date emissionDate;

    /**
     * Constructs a Certificate object.
     *
     * @param publicRSAKey The public RSA key.
     * @param Subject      The subject of the certificate.
     */
    public Certificate(PublicKey publicRSAKey, String Subject){
        this.publicRSAKey = publicRSAKey;
        this.serialNumber = new SerialNumberGenerator().generateSerialNumber();
        this.subject=Subject;
    }

    /**
     * Converts the certificate to PEM format.
     *
     * @return The certificate in PEM format.
     */
    public String toPEM(){
        return
                "-----BEGIN CERTIFICATE-----\n" +
                        "Serial:" + serialNumber.toString() + "\n" +
                        "Date of Emission:" + (emissionDate==null?"":emissionDate) + "\n" +
                        "Public Key:" + (publicRSAKey==null?"":publicRSAKey.getEncoded())+ ",\n" +
                        "Subject:" + (subject==null?"":subject)+ "\n" +
                        "Issuer:"+ (issuer==null?"":subject)+ "\n" +
                        "Signature:" + new String(getSignature()==null?new byte[0]:getSignature())+ "\n" +
                        "\n-----END CERTIFICATE-----\n";
    }

    /**
     * Sets the values of the certificate from a PEM formatted string.
     *
     * @param PEM The PEM formatted string representing the certificate.
     */
    public void setValueFromPEM(String PEM){
        int indexOfFieldName=PEM.indexOf("Serial:")+7;
        serialNumber = new BigInteger(PEM.substring(indexOfFieldName,PEM.indexOf("\n",indexOfFieldName)));

        indexOfFieldName=PEM.indexOf("Date of Emission:")+17;
        String date = PEM.substring(indexOfFieldName,PEM.indexOf("\n",indexOfFieldName));
        emissionDate = date.isEmpty()?null:new Date(date);

        indexOfFieldName=PEM.indexOf("Public Key:")+11;
        String key= PEM.substring(indexOfFieldName,PEM.indexOf(",\n",indexOfFieldName));
        publicRSAKey = key.isEmpty()?null:getPublicKeyFromString(key);

        indexOfFieldName=PEM.indexOf("Subject:")+8;
        String subjectPEM= PEM.substring(indexOfFieldName,PEM.indexOf("\n",indexOfFieldName));
        subject = subjectPEM.isEmpty()?null:new String(subjectPEM);

        indexOfFieldName=PEM.indexOf("Issuer:")+7;
        String issuerPEM= PEM.substring(indexOfFieldName,PEM.indexOf("\n",indexOfFieldName));
        issuer = issuerPEM.isEmpty()?null:new String(issuerPEM);

        indexOfFieldName=PEM.indexOf("Signature:")+10;
        signature = (PEM.substring(indexOfFieldName,PEM.indexOf("\n",indexOfFieldName))).getBytes();
    }

    /**
     * Extracts the public key from a string representation.
     *
     * @param publicKeyString The string representation of the public key.
     * @return The public key extracted from the string.
     */
    public static PublicKey getPublicKeyFromString(String publicKeyString) {
        try {
            String[] lines = publicKeyString.split("\n");

            String modulusString = lines[2].substring(lines[2].indexOf(":")+2).trim();
            String exponentString = lines[3].substring(lines[3].indexOf(":")+2).trim();

            byte[] modulusBytes = decimalStringToByteArray(modulusString);
            byte[] exponentBytes = decimalStringToByteArray(exponentString);

            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(1, modulusBytes), new BigInteger(1, exponentBytes));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a decimal string to a byte array.
     *
     * @param s The decimal string to convert.
     * @return The byte array representation of the decimal string.
     */
    public static byte[] decimalStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            data[i] = Byte.parseByte(s.substring(i, i + 1));
        }
        return data;
    }

    /**
     * Gets the public RSA key.
     *
     * @return The public RSA key.
     */
    public PublicKey getPublicRSAKey() {
        return publicRSAKey;
    }

    /**
     * Gets the certificate content.
     *
     * @return The certificate content.
     */
    public static String getCertificateContent() {
        return certificateContent;
    }

    /**
     * Sets the certificate content.
     *
     * @param certificateContent The certificate content to set.
     */
    public static void setCertificateContent(String certificateContent) {
        Certificate.certificateContent = certificateContent;
    }

    /**
     * Gets the serial number.
     *
     * @return The serial number.
     */
    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    /**
     * Sets the serial number.
     *
     * @param serialNumber The serial number to set.
     */
    public void setSerialNumber(BigInteger serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * Gets the subject of the certificate.
     *
     * @return The subject of the certificate.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the subject of the certificate.
     *
     * @param subject The subject to set.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Gets the issuer of the certificate.
     *
     * @return The issuer of the certificate.
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * Sets the issuer of the certificate.
     *
     * @param issuer The issuer to set.
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * Gets the signature of the certificate.
     *
     * @return The signature of the certificate.
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * Sets the signature of the certificate.
     *
     * @param signature The signature to set.
     */
    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    /**
     * Gets the emission date of the certificate.
     *
     * @return The emission date of the certificate.
     */
    public Date getEmissionDate() {
        return emissionDate;
    }

    /**
     * Sets the emission date of the certificate.
     *
     * @param emissionDate The emission date to set.
     */
    public void setEmissionDate(Date emissionDate) {
        this.emissionDate = emissionDate;
    }
}