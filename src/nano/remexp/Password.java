package nano.remexp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Used in the future to add some saftey as remote access of an administrator is desirable
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class Password {
	/**
	 * Encrypts a string and converts it into the hex representation.
	 * 
	 * @param password The string to be encrypted
	 * @return The encrypted string, represented in Hex values
	 */
	public static String computeHashHex(String password) {
		MessageDigest d = null;
		try {
			d = MessageDigest.getInstance("SHA-1");
			d.reset();
			d.update(password.getBytes());
			return toHexString(d.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Converts a byte array into the representative Hex string.
	 * 
	 * @param buf The byte array to be converted.
	 * @return The hex string representation of the byte array.
	 */
	private static String toHexString(byte[] buf) {
		char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F'};
		
		StringBuffer strBuf = new StringBuffer(buf.length * 2);
		for (int i = 0; i < buf.length; i++) {
			strBuf.append(hexChar[(buf[i] & 0xf0) >>> 4]); // fill left with zero bits
			strBuf.append(':');
			strBuf.append(hexChar[buf[i] & 0x0f]);
		}
		return strBuf.toString();
	}

	/**
	 * Main method for testing purposes. It will ask on the system line to enter a string
	 * that will be converted to a hex string
	 * 
	 * @param args no args needed.
	 */
	public static void main(String[] args){
		String curLine = "";
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		do {
			System.out.println("Enter string you want to hash ('q' to exit): ");
			try {
				curLine = in.readLine();
			} catch (IOException e) {
				System.err.println("Failed reading line!");
			}
			if (!(curLine.equals("q"))){
				System.out.println("Hash for '" + curLine +"' = " + computeHashHex(curLine));
			} else System.out.println("Bye!");
		} while (!(curLine.equals("q")));
	}
}
