package edu.upenn.cis455.storage;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHash {
	
	// use PBKDF2 algorithm to hash password	
	public static String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] salt = "thisthisthisthis".getBytes();
//		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
//		random.nextBytes(salt);
        int iterations = 1024;
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
		return toHex(hash);
        
    }
	private static String toHex(byte[] array) throws NoSuchAlgorithmException
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }
    
}
