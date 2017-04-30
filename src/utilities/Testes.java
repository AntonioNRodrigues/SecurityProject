package utilities;

import static utilities.ReadWriteUtil.SERVER;
import static utilities.ReadWriteUtil.USERS;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import jdk.jfr.events.FileWriteEvent;
import sun.security.jca.Providers;
import user.User;

public class Testes {

	public static void teste1() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "SunJSSE");
		generator.initialize(2048);
		KeyPair keyPair = generator.generateKeyPair();

		SecretKey sessionKey = new SecretKeySpec(new byte[16], "AES");

		Cipher c = Cipher.getInstance("RSA", "SunJCE");
		c.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
		byte[] result1 = c.doFinal(sessionKey.getEncoded());

		c.init(Cipher.WRAP_MODE, keyPair.getPublic());
		byte[] result2 = c.wrap(sessionKey);

		c.init(Cipher.UNWRAP_MODE, keyPair.getPrivate());
		SecretKey sessionKey1 = (SecretKey) c.unwrap(result1, "AES",
		    Cipher.SECRET_KEY);

		c.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
		SecretKey sessionKey2 = new SecretKeySpec(c.doFinal(result2), "AES");

		System.out.println(Arrays.equals(sessionKey1.getEncoded(),
		    sessionKey2.getEncoded()));
		
		
		//////////////////////////////////////////////////////////////7
		c.init(Cipher.UNWRAP_MODE, keyPair.getPrivate());
		SecretKey sessionKey3 = (SecretKey) c.unwrap(result2, "AES",
		    Cipher.SECRET_KEY);

		c.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
		SecretKey sessionKey4 = new SecretKeySpec(c.doFinal(result1), "AES");
		
		System.out.println(Arrays.equals(sessionKey3.getEncoded(),
			    sessionKey4.getEncoded()));


	}
	
	public static void teste2() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, IOException {
		
		String str = "istoehumapassword";
		byte[] pass = str.getBytes();
		SecretKey sk = new SecretKeySpec(pass, "AES");

		System.out.println(sk);
		
		SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
	
		byte[] salt = {
			    (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
			    (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
			};

		int count = 20;

		String password = null;
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
		PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
		SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

		Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

		Serializable object = null;
		SealedObject sealed = new SealedObject(object, cipher);
	}


	public static void teste3() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		
			// gerar a chave que queremos transmitir
			KeyGenerator kg = KeyGenerator.getInstance("DESede");
			Key sharedKey = kg.generateKey( );
			
			// vamos usar uma chave baseada numa password para a cifrar
			String password = "Come you spirits that tend on mortal thoughts";
			byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52,
			(byte) 0x3e, (byte) 0xea, (byte) 0xf2 };
			PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray( ));
			SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			SecretKey passwordKey = kf.generateSecret(keySpec);
			
			// preparar o algoritmo de cifra
			PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 20);
			Cipher c = Cipher.getInstance("PBEWithMD5AndDES");
			c.init(Cipher.WRAP_MODE, passwordKey, paramSpec);
			
			// cifrar a chave secreta que queremos enviar
			byte[] wrappedKey = c.wrap(sharedKey);
			
			// cifrar alguns dados
			c = Cipher.getInstance("DESede");
			c.init(Cipher.ENCRYPT_MODE, sharedKey);
			byte[] input = "Stand and unfold yourself".getBytes( );
			byte[] encrypted = c.doFinal(input);
			
			
			// agora seria enviada a wrappedKey mais os dados cifrados
			
			// no receptor usaríamos os seguintes passos (re-utilizando algumas estruturas de dados)
			c = Cipher.getInstance("PBEWithMD5AndDES");
			c.init(Cipher.UNWRAP_MODE, passwordKey, paramSpec);
			Key unwrappedKey = c.unwrap(wrappedKey, "DESede", Cipher.SECRET_KEY);
			
			// agora podem-se decifrar os dados
			c = Cipher.getInstance("DESede");
			c.init(Cipher.DECRYPT_MODE, unwrappedKey);
			String newData = new String(c.doFinal(encrypted));
			System.out.println("The string was " + newData);
	}
	
	
	/*
	 * Valores de salt e iterations de acordo com o RFC 8018: PKCS #5: Password-Based Cryptography Specification Version 2.1
	 */
	private static void teste4() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException
    {
		String password = "Come you spirits that tend on mortal thoughts";
        int iterations = 1000;
        char[] psswd = password.toCharArray();
        byte[] salt = getSalt();
         
        PBEKeySpec spec = new PBEKeySpec(psswd, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        SecretKey sk = skf.generateSecret(spec);
        byte[] hash = skf.generateSecret(spec).getEncoded();
        System.out.println("hash.length: "+hash.length);
        
        System.out.println(iterations + ":" + toHex(salt) + ":" + toHex(hash));
        
        Provider [] providerList = Security.getProviders();
        for (Provider provider : providerList)      {
            System.out.println("Name: "  + provider.getName());
            System.out.println("Information:\n" + provider.getInfo());

            Set<Service> serviceList = provider.getServices();
            for (Service service : serviceList)
             {
               System.out.println("Service Type: " + service.getType() + " Algorithm " + service.getAlgorithm());
             }
          }
        
		Mac mac = Mac.getInstance("HmacSHA512");
		mac.init(sk);
		String data = "This have I thought good to deliver thee, ......";
		mac.update(data.getBytes( ));
		byte[] hmac=mac.doFinal();
		
        System.out.println(iterations + ":" + toHex(salt) + ":" + toHex(hash));
        System.out.println("pass: "+Arrays.toString(hash));
        System.out.println("pass: "+toHex(hash));
		System.out.println("hmac: "+toHex(hmac));
		System.out.println("length: "+toHex(hmac).length());


        
    }
	
    private static byte[] getSalt() throws NoSuchAlgorithmException
    {
    	
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt; 

        //1000:c3948ae5f5770aaff9ff333f50190529:4bc6fa20d569502dfeb8b74dc4204d5b6aa0e4e72afa3e2ec3882631df372b23e852481bc91da13c22f88e3dec4626df09f1379d0f4b21bcb964954253d81687

        
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
    
	
	/**
	 * split a line by : and separate user from password
	 * 
	 * @param str
	 */
	public static void splitLine(String str) {
		String[] userPass = str.split(":");
		if (userPass.length == 1) {
			System.out.println(userPass[0].trim());
		} else {
			System.out.println(userPass[0].trim());
			System.out.println(userPass[1].trim());
		}
	}

	
	private static void teste5()
    {
		Path usersFile = Paths.get(SERVER + File.separator + USERS);
		if (Files.exists(usersFile)) {
			System.out.println("load users from users file");

			// decipher file and read content
			SecretKey sk = SecurityUtil.getKeyFromServer();
			try {
				
				byte[] b = SecurityUtil2.decipherFile2Memory(usersFile, sk);
				String content = b.toString();
				String[] array = content.split("\n");
				for (String s : array) {
					splitLine(s);
				}
			} catch (IOException | InvalidKeyException e) {
				e.printStackTrace();
			} catch (InvalidAlgorithmParameterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

    }

	private static void teste6()
    {
		
		Pattern pattern = Pattern.compile("//^/(?!.sig|.key.server)([a-z0-9]+)$");
		
		String[] s = { "a.sig", "a.key.server", "a.txt"};

		Matcher matcher = pattern.matcher(s);

		boolean bl = matcher.find();
		System.out.println(bl);


		//^/(?!ignoreme|ignoreme2|ignoremeN)([a-z0-9]+)$ 

		
	    final Pattern p = Pattern.compile("//^/(?!.sig|.key.server|owner.txt|shared.txt)([a-z0-9]+)$");

	    File folder = new File("your/path");
	    File[] listOfFiles = folder.listFiles();
	    
	    for (int i = 0; i < listOfFiles.length; i++) {
	        if (listOfFiles[i].isFile()) {
	            System.out.println("File " + listOfFiles[i].getName());
	          } else if (listOfFiles[i].isDirectory()) {
	            System.out.println("Directory " + listOfFiles[i].getName());
	          }
	    }

	    
    }
	
	
	public static void main(String[] args)  {
 
			teste6();
	}
		
	
	

}
