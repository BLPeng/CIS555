package edu.upenn.cis455.storage;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHash {
	
	// use PBKDF2 algorithm to hash password	
	public static String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] salt = new byte[16];
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		random.nextBytes(salt);
        int iterations = 1024;
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
		return hash.toString();
        
    }
    
}
