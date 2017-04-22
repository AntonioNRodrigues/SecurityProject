package utilities;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Provider.Service;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Set;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import static java.nio.file.StandardCopyOption.*;
import static utilities.ReadWriteUtil.SERVER;

public class SecurityUtil2 {

	public SecurityUtil2() {
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * 
	 */
	public static void appendToFile(Path file, SecretKey secretKey, byte[] text) throws IOException, InvalidKeyException  {
	//public static byte[] decipherFile2Memory(Path path, String algorithm, SecretKey secretKey) throws IOException, InvalidKeyException  {
		
		//Cipher c = Cipher.getInstance(algorithm);
		Cipher c = SecurityUtil.getCipher(); 
		c.init(Cipher.DECRYPT_MODE, secretKey);
		//return c.doFinal(Files.readAllBytes(path));

		Cipher c2 = SecurityUtil.getCipher(); 
		c2.init(Cipher.ENCRYPT_MODE, secretKey);
		
		Path tempFile = Paths.get((File.createTempFile(UUID.randomUUID().toString(), ".txt")).getName());
		
		byte[] b;
		try ( 
				FileInputStream fis = new FileInputStream( file.toFile());
				CipherInputStream cis = new CipherInputStream( fis, c);
	
				FileOutputStream fos = new FileOutputStream(tempFile.toFile());
				CipherOutputStream cos2 = new CipherOutputStream(fos, c2);
			) {
			b = new byte[16];
			int length;
			while ((length = cis.read(b)) != -1)
				cos2.write(b, 0, length);
			
			//append the text...it will miss the new line i suppose...
			cos2.write(text);
			
			//replace encrypted file by the new one
	        CopyOption[] options = new CopyOption[] { REPLACE_EXISTING };
	        Files.copy(tempFile, file, options);
	        Files.delete(tempFile);
		}	
	}
	

	public static byte[] decipherFile2Memory(Path path, SecretKey secretKey) throws IOException, InvalidKeyException  {
	//public static byte[] decipherFile2Memory(Path path, String algorithm, SecretKey secretKey) throws IOException, InvalidKeyException  {
		
		//Cipher c = Cipher.getInstance(algorithm);
		Cipher c = SecurityUtil.getCipher(); 
		c.init(Cipher.DECRYPT_MODE, secretKey);
		//return c.doFinal(Files.readAllBytes(path));

		try ( 
				CipherInputStream cis = new CipherInputStream( new FileInputStream(path.toFile()), c);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
		) {
			byte[] b = new byte[16];
			int length;
			while ((length = cis.read(b)) != -1)
				out.write(b, 0, length);
			
			return out.toByteArray();
		}	
	}

	public static String hexString(byte[] bytes) {
		Formatter formatter = new Formatter();	
		for (byte b : bytes)
			formatter.format("%02x", b);
		return formatter.toString();
	}

	public static boolean checkFileIntegrity(Path file, SecretKey secretKey) throws InvalidKeyException, IOException, NoSuchAlgorithmException {

		Path hmac = Paths.get("."+file.getFileName()+".hmac");
		System.out.println("hmac: "+hmac);

		if (Files.exists(file)) {
				byte[] f = SecurityUtil2.decipherFile2Memory(file, secretKey);

				if (Files.exists(hmac)) {
					byte[] h = SecurityUtil2.readHMACFile(hmac);
					byte[] h2= SecurityUtil2.calcHMAC(f, secretKey);
					
					if (!Arrays.equals(h, h2))
						return false;
				}
				else {
					System.out.println("HMAC file not found.");
					return false;
				}
		}
		else {
			System.out.println("File not found.");
			return false;
		}
		
		return true;
	}
	
	
	public static byte[] calcHMAC(byte[] f, SecretKey sk) throws InvalidKeyException, NoSuchAlgorithmException {
		Mac mac = Mac.getInstance("HmacSHA512");
		mac.init(sk);
		mac.update(f);
		byte[] hmac=mac.doFinal();
		//System.out.println("hmac: "+toHex(hmac));
		//System.out.println("length: "+toHex(hmac).length());  		
		return hmac;
	}
	
	
	public static byte[] readHMACFile(Path hmac) throws IOException {
		return Files.readAllBytes(hmac);
	}
	
	
	public static void writeHMACFile(Path file, byte[] hmac) throws IOException {
        Path f = file.getFileName();
        Path d = file.getParent();
		Path hmacFile = Paths.get(d+"."+f+".hmac");
        Files.write(hmacFile, hmac);
	}
	
	
	/*
	 * RFC 8018: PKCS #5: Password-Based Cryptography Specification Version 2.1
	 * salt and iterations values agree with RFC 8018 for a PBKDF2 password-based 
	 * key derivation mechanism with the underlying message authentication scheme 
	 * "HmacSHA512"
	 */
	private static void createKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException
    {
		//String password = "Come you spirits that tend on mortal thoughts";
        int iterations = 1000;
        char[] psswd = password.toCharArray();
        byte[] salt = getSalt();
         
        PBEKeySpec spec = new PBEKeySpec(psswd, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        //SecretKey sk = skf.generateSecret(spec);
        byte[] hash = skf.generateSecret(spec).getEncoded();
        System.out.println("hash.length: "+hash.length);
        System.out.println(iterations + ":" + toHex(salt) + ":" + toHex(hash));
        
        System.out.println(iterations + ":" + toHex(salt) + ":" + toHex(hash));
        System.out.println("pass: "+Arrays.toString(hash));
        System.out.println("pass: "+toHex(hash));
        
        /* Provider [] providerList = Security.getProviders();
        for (Provider provider : providerList)      {
            System.out.println("Name: "  + provider.getName());
            System.out.println("Information:\n" + provider.getInfo());

            Set<Service> serviceList = provider.getServices();
            for (Service service : serviceList)
             {
               System.out.println("Service Type: " + service.getType() + " Algorithm " + service.getAlgorithm());
             }
          }*/      
    }
	
    private static byte[] getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt; 

        //return c3948ae5f5770aaff9ff333f50190529
    }
    
    private static String toHex(byte[] array) throws NoSuchAlgorithmException
    {
        BigInteger big = new BigInteger(1, array);
        String hex = big.toString(16);
        int paddingLength = (array.length*2)-hex.length();
        if(paddingLength > 0)
            return String.format("%0"+paddingLength +"d",0)+hex;
        else
            return hex;
    }
    

}
