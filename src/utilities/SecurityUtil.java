package utilities;

import static utilities.ReadWriteUtil.SERVER;
import static utilities.ReadWriteUtil.USERS;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Formatter;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.security.provider.SHA;

public class SecurityUtil {
	public static final String AES = "AES";
	public static final String RSA = "RSA";
	public static final String SHA_256 = "SHA-256";
	public static final int bits_RSA = 2048;
	public static final int bits_AES = 128;
	public static final String SERVER_KEY = "Server.key";

	/**
	 * 
	 * @param sk
	 * @param data
	 * @return
	 */
	public static String decriptSimetric(SecretKey sk, String data) {

		Cipher c = null;
		byte[] str = null;
		try {
			c = getCipher();
			c.init(Cipher.DECRYPT_MODE, sk);
			str = c.doFinal(data.getBytes());

		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		String decriptValue = new String(str);
		System.out.println(decriptValue);
		return decriptValue;

	}

	/**
	 * 
	 * @param sk
	 * @param data
	 * @return
	 */
	public static byte[] encriptSimetric(SecretKey sk, String data) {
		byte[] strBytes = null;
		Cipher c = null;
		try {
			c = getCipher();
			c.init(Cipher.ENCRYPT_MODE, sk);
			strBytes = c.doFinal(data.getBytes());
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}

		System.out.println(strBytes.toString());
		return strBytes;

	}

	/**
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static SecretKey getKey() {
		KeyGenerator k = null;
		try {
			k = KeyGenerator.getInstance(AES);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		k.init(bits_AES);
		SecretKey sk = k.generateKey();
		return sk;
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
			//c = Cipher.getInstance("AES/ECB/PKCS5Padding");
			c = Cipher.getInstance("AES/CBC/PKCS5Padding");

		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}
		return c;
	}

	/**
	 * 
	 * @param sk
	 * @param path
	 */
	public static void persisteKey(SecretKey sk, String path) {
		byte[] keyEncoded = sk.getEncoded();
		if (!(new File("SERVER").exists())) {
			new File("SERVER").mkdirs();
		}
		try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(new File(path)))) {
			oo.write(keyEncoded);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * method to get a pair of asymmetric keys
	 * 
	 * @return
	 */
	public static KeyPair getKeyPair() {
		KeyPair kp = null;
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA);
			kpg.initialize(bits_RSA);
			kp = kpg.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return kp;
	}

	/**
	 * method to generate a symmetric key through a string
	 * 
	 * @param str
	 */
	public static void generateKeyFromPass(String str) {
		byte[] pass = str.getBytes();
		SecretKey sk = new SecretKeySpec(pass, AES);
		persisteKey(sk, ReadWriteUtil.SERVER + File.separator + SERVER_KEY);
	}

	/**
	 * 
	 * @param temp
	 * @param sk
	 * @param encriptedFile
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public static void cipherFile(Path temp, SecretKey sk, Path encriptedFile) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		Cipher c = getCipher();
		c.init(Cipher.ENCRYPT_MODE, sk);
		// read file
		FileInputStream fis = new FileInputStream(temp.toFile());
		// write encrypted file
		CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(encriptedFile.toFile()), c);
		byte[] b = new byte[16];
		int i = fis.read(b);
		while (i != -1) {
			cos.write(b, 0, i);
			i = fis.read(b);
		}
		cos.close();
		fis.close();
		Files.deleteIfExists(temp);
	}
	
	/**
	* method to decipher a file
	*
	* @param fileToDecript
	* @param sk
	* @param temp
	* @throws NoSuchAlgorithmException
	* @throws NoSuchPaddingException
	* @throws InvalidKeyException
	* @throws IllegalBlockSizeException
	* @throws BadPaddingException
	* @throws IOException
	 * @throws InvalidAlgorithmParameterException 
	*/
	public static void decipherFile(Path fileToDecript, SecretKey sk, Path temp) throws NoSuchAlgorithmException,
	NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
	Cipher c = getCipher();
	//c.init(Cipher.DECRYPT_MODE, sk);
	// Change for CBC mode

    IvParameterSpec ivParameterSpec = new IvParameterSpec(sk.getEncoded());
    c.init(Cipher.DECRYPT_MODE, sk, ivParameterSpec);

	// get ciphered file
	CipherInputStream cis = new CipherInputStream(new FileInputStream(fileToDecript.toFile()), c);
	FileOutputStream fos = new FileOutputStream(temp.toFile());

	byte[] b = new byte[16];
	int length;
	while ((length = cis.read(b)) != -1)
		fos.write(b, 0, length);
	
	cis.close();
	fos.close();
	}

	/**
	 * 
	 * @param f
	 * @param sk
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public static String decipherFileToMemory(File f, SecretKey sk) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		Cipher c = getCipher();
		c.init(Cipher.DECRYPT_MODE, sk);

		byte[] fileInBytes = new byte[(int) f.length()];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		// get ciphered file
		CipherInputStream cis = new CipherInputStream(new FileInputStream(f), c);
		
		byte[] b = new byte[16];
		int i = cis.read(b);
		while (i != -1) {
			bos.write(b, 0, i);
			i = cis.read(b);
		}
		fileInBytes = bos.toByteArray();
		cis.close();
		bos.close();

		return new String(fileInBytes, "UTF-8");
	}

	/**
	 * 
	 * @param f
	 * @param sk
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public static void cipherFileToMemory(File f, SecretKey sk) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		Cipher c = getCipher();
		c.init(Cipher.DECRYPT_MODE, sk);
		// get ciphered file
		CipherInputStream cis = new CipherInputStream(new FileInputStream(f), c);
		BufferedOutputStream bf = new BufferedOutputStream(System.out);
		byte[] b = new byte[16];
		int i = cis.read(b);
		while (i != -1) {
			bf.write(b, 0, i);
			i = cis.read(b);
		}
		cis.close();
		bf.close();
	}
	/**
	 * method to get the key of the server
	 * 
	 * @return the secretKey
	 */
	public static SecretKey getKeyFromServer() {
		byte[] secretKeyBytes = new byte[16];
		try (ObjectInputStream out = new ObjectInputStream(
				new FileInputStream(new File(SERVER + File.separator + SERVER_KEY)))) {
			out.read(secretKeyBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		SecretKey sk = new SecretKeySpec(secretKeyBytes, AES);
		return sk;

	}

	/**
	 * method to generate a NONCE
	 * 
	 * @return
	 */
	public static String generateNonce() {
		byte[] binaryData = UUID.randomUUID().toString().getBytes();
		return Base64.getEncoder().encodeToString(binaryData);
	}
	/**
	 * method to calculate a sintes
	 * @param passNonce
	 * @return
	 */
	public static byte [] calcSintese(String passNonce) {
		byte [] message = null;
		try {
			byte[] auxMessage = passNonce.getBytes("UTF-8");
			MessageDigest messageDigest = MessageDigest.getInstance(SHA_256);
			message = messageDigest.digest(auxMessage);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return message;
	}
	
	

	
}
