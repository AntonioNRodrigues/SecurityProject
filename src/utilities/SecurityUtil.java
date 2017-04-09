package utilities;

import static utilities.ReadWriteUtil.SERVER;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtil {
	public static final String AES = "AES";
	public static final String RSA = "RSA";
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
			c = Cipher.getInstance("AES/ECB/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}
		return c;
	}

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

	public static void encriptFile(File f, SecretKey sk) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		Cipher c = getCipher();
		c.init(Cipher.ENCRYPT_MODE, sk);
		// read file
		FileInputStream fis = new FileInputStream(f);
		// write file to c.cif
		CipherOutputStream cos = new CipherOutputStream(
				new FileOutputStream(new File(SERVER + File.separator + "users.cif")), c);
		byte[] b = new byte[16];
		int i = fis.read(b);
		while (i != -1) {
			cos.write(b, 0, i);
			i = fis.read(b);
		}
		Files.deleteIfExists(f.toPath());
		cos.close();
		fis.close();
	}

	public static void decriptFile(File f, SecretKey sk) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		Cipher c = getCipher();
		c.init(Cipher.DECRYPT_MODE, sk);
		// get ciphered file
		CipherInputStream cis = new CipherInputStream(new FileInputStream("a.cif"), c);
		FileOutputStream fis = new FileOutputStream(new File("new.txt"));
		byte[] b = new byte[16];
		int i = cis.read(b);
		while (i != -1) {
			fis.write(b, 0, i);
			i = cis.read(b);
		}
		cis.close();
		fis.close();
	}

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

}
