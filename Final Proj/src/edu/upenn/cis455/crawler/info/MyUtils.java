package edu.upenn.cis455.crawler.info;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyUtils {
	//assume num of workers less than 65536
	public static int getWorkerIndex(String hash, int size) {
		String head = hash.substring(0, 4);
		int val = Integer.parseInt(head, 16); 
		if (size <= 0) {
			size = 1;
		}
		int range = 65536 / size;
		int idx = val / range;
		return idx;
	}
	
	public static String sha1(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
        byte[] result = mDigest.digest(input.getBytes("UTF-8"));
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(String.format("%02X", result[i]));
        }         
        return sb.toString();
    }
}
