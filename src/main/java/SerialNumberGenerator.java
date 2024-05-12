import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * A utility class for generating serial numbers.
 */
public class SerialNumberGenerator
{
    /** The maximum serial number value. */
    private final BigInteger MAX_SERIAL_NUMBER = BigInteger.valueOf(Long.MAX_VALUE);

    /** The last generated serial number. */
    private BigInteger lastSerialNumber;

    /**
     * Constructs a SerialNumberGenerator and initializes the last serial number to a random value.
     */
    public SerialNumberGenerator() {
        // Initialize last serial number to a random value
        lastSerialNumber = new BigInteger(MAX_SERIAL_NUMBER.bitLength(), new SecureRandom());
    }

    /**
     * Generates the next serial number.
     *
     * @return The next serial number.
     */
    public BigInteger generateSerialNumber() {
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