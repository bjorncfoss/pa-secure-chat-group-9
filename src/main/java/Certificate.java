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

    // constructor
    public Certificate(PublicKey publicRSAKey, String Subject){
        this.publicRSAKey = publicRSAKey;
        this.serialNumber = new SerialNumberGenerator().generateSerialNumber();
        this.subject=Subject;
    }

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

    public static byte[] decimalStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            data[i] = Byte.parseByte(s.substring(i, i + 1));
        }
        return data;
    }

    // Getters
    public PublicKey getPublicRSAKey() {
        return publicRSAKey;
    }

    public static String getCertificateContent() {
        return certificateContent;
    }

    public static void setCertificateContent(String certificateContent) {
        Certificate.certificateContent = certificateContent;
    }

    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(BigInteger serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public Date getEmissionDate() {
        return emissionDate;
    }

    public void setEmissionDate(Date emissionDate) {
        this.emissionDate = emissionDate;
    }
}