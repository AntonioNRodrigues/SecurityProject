package utilities;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static utilities.ReadWriteUtil.SERVER;
import static utilities.ReadWriteUtil.USERS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class SecurityUtil2 {

	public SecurityUtil2() {
	}

	/**
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static Cipher getCipher() {
		Cipher c = null;
		try {
			c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}
		return c;
	}

	/*
	 * 
	 */
	public static void appendToFile(Path file, SecretKey secretKey, byte[] text)
			throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
	
		//decode file
		byte[] decodedFile=SecurityUtil2.decipherFile2Memory(file, secretKey);
	
		//concatenate decoded file and the new text 
		ByteArrayOutputStream baos = new ByteArrayOutputStream( );
		baos.write(decodedFile);
		baos.write("\n".getBytes());
		baos.write(text);
		byte fileContent[] = baos.toByteArray( );
	
		Path tempFile = Files.createTempFile("foobar", ".tmp");
				
		//encode again to temp file
		SecurityUtil2.cipherFile(tempFile, secretKey, fileContent);

		//move temp file to file
		CopyOption[] options = new CopyOption[] { REPLACE_EXISTING };
		Files.copy(tempFile, file, options);
		Files.delete(tempFile);
	}

	public static byte[] decipherFile2Memory(Path path, SecretKey secretKey)
			throws IOException, InvalidKeyException, InvalidAlgorithmParameterException {

		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		Cipher c = SecurityUtil2.getCipher();
		secretKey = SecurityUtil.getKeyFromServer();
		c.init(Cipher.DECRYPT_MODE, secretKey, ivspec);

		try (CipherInputStream cis = new CipherInputStream(new FileInputStream(path.toFile()), c);
				ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			byte[] b = new byte[16];
			int length;
			while ((length = cis.read(b)) != -1)
				out.write(b, 0, length);

			System.out.println("decipherFile2Memory: ByteArrayOutputStream out:" + out.toString());
			return out.toByteArray();
		}
	}

	public static void cipherFile(Path file, SecretKey secretKey, byte[] bytes)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, IOException, InvalidAlgorithmParameterException {

		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		Cipher c = SecurityUtil2.getCipher();
		secretKey = SecurityUtil.getKeyFromServer();
		c.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);

		// read file
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

		// write encrypted file
		CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(file.toFile()), c);
		byte[] b = new byte[16];
		int length;
		while ((length = bais.read(b)) != -1)
			cos.write(b, 0, length);

		cos.close();
		bais.close();
	}

	public static String hexString(byte[] bytes) {
		Formatter formatter = new Formatter();
		for (byte b : bytes)
			formatter.format("%02x", b);
		String s = new String(formatter.toString());
		formatter.close();
		return s;
	}

	/*
	 * 
	 */
	public static boolean checkFileIntegrity(Path file, Path hmacFile, SecretKey secretKey)
			throws InvalidKeyException, IOException, NoSuchAlgorithmException, 
			InvalidAlgorithmParameterException {

		// calc file hmac
		byte[] f = SecurityUtil2.decipherFile2Memory(file, secretKey);
		byte[] h = SecurityUtil2.calcHMAC(f, secretKey);
		System.out.println("h hmac: "+toHex(h));
		System.out.println("hmac length: "+toHex(h).length());

		// read hmac file
		byte[] h2 = SecurityUtil2.readHMACFile(hmacFile);
		System.out.println("h2 hmac: "+toHex(h2));
		System.out.println("hmac length: "+toHex(h).length());
		
		// compare
		if (Arrays.equals(h, h2))
			return true;

		return false;
	}

	public static boolean writeHMACFile(Path file, Path hmacFile, SecretKey secretKey)
			throws InvalidKeyException, IOException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {

		if (Files.exists(file)) {
			byte[] f = SecurityUtil2.decipherFile2Memory(file, secretKey);
			byte[] h = SecurityUtil2.calcHMAC(f, secretKey);
			Files.write(hmacFile, h);
		}
		return true;
	}

	public static byte[] calcHMAC(byte[] f, SecretKey sk) throws InvalidKeyException, NoSuchAlgorithmException {

		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(sk);
		mac.update(f);
		byte[] hmac = mac.doFinal();
		//System.out.println("calcHMAC:");
		//System.out.println("hmac: "+toHex(hmac));
		//System.out.println("length: "+toHex(hmac).length());
		return hmac;
	}

	public static byte[] readHMACFile(Path hmac) throws IOException {
		return Files.readAllBytes(hmac);
	}

	public static byte[] getSalt() throws NoSuchAlgorithmException {
		// SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		// byte[] salt = new byte[16];
		// sr.nextBytes(salt);
		// return salt;
		byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea,
				(byte) 0xf2, (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea,
				(byte) 0xf2 };
		return salt;
	}

	/*
	 * RFC 8018: PKCS #5: Password-Based Cryptography Specification Version 2.1
	 * salt and iterations values agree with RFC 8018 for a PBKDF2
	 * password-based key derivation mechanism with the underlying message
	 * authentication scheme "HmacSHA256"
	 */
	public static void createKey(String password)
			throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
		// String password = "Come you spirits that tend on mortal thoughts";
		int iterations = 1000;
		char[] psswd = password.toCharArray();
		byte[] salt = getSalt();

		PBEKeySpec spec = new PBEKeySpec(psswd, salt, iterations, 64 * 8);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		SecretKey sk = skf.generateSecret(spec);
		byte[] hash = sk.getEncoded();
		System.out.println("hash.length: " + hash.length);
		System.out.println(iterations + ":" + toHex(salt) + ":" + toHex(hash));
		System.out.println("pass: " + Arrays.toString(hash));
		System.out.println("pass: " + toHex(hash));

		// write symmetric key to disk
		SecurityUtil.persisteKey(sk, ReadWriteUtil.SERVER + File.separator + SecurityUtil.SERVER_KEY);

		/*
		 * Provider [] providerList = Security.getProviders(); for (Provider
		 * provider : providerList) { System.out.println("Name: " +
		 * provider.getName()); System.out.println("Information:\n" +
		 * provider.getInfo());
		 * 
		 * Set<Service> serviceList = provider.getServices(); for (Service
		 * service : serviceList) { System.out.println("Service Type: " +
		 * service.getType() + " Algorithm " + service.getAlgorithm()); } }
		 */
	}

	public static String toHex(byte[] array) throws NoSuchAlgorithmException {
		BigInteger big = new BigInteger(1, array);
		String hex = big.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0)
			return String.format("%0" + paddingLength + "d", 0) + hex;
		else
			return hex;
	}

}
