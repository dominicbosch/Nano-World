/*
 * Copyright (c) 2011 by Tibor Gyalog, Raoul Schneider, Dino Keller,
 * Christian Wattinger, Martin Guggisberg, Dominic Bosch and The 
 * Regents of the University of Basel. All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice and the following
 * two paragraphs appear in all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF BASEL BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * BASEL HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF BASEL SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF BASEL HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * Authors: Tibor Gyalog, Raoul Schneider, Dino Keller,
 * Christian Wattinger, Martin Guggisberg, Dominic Bosch <vexp@nano-world.net>
 * 
 */ 

package nano.remexp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Used in the future to add some saftey as remote access of an administrator is desirable
 * @author Dominic Bosch
 * @version 1.0 21.10.2011
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
	 * Main method for testing purposes.
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
