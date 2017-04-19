package user;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import com.sun.org.apache.bcel.internal.util.ByteSequence;

import utilities.SecurityUtil;
import utilities.SecurityUtil2;

import static utilities.ReadWriteUtil.SERVER;
import static utilities.ReadWriteUtil.USERS;

public class UserCatalog {
	private Map<String, User> mapUsers;

	/**
	 * 
	 */
	public UserCatalog() {
		super();
		this.mapUsers = new ConcurrentHashMap<>();
		if (!buildUsers()) {
			readFile();
		}
		System.out.println(mapUsers);
	}

	/**
	 * Read the file users.txt and populates the map with user:password
	 */
	private void readFile() {
		System.out.println("READFILE");
		Path usersFile = Paths.get(SERVER + File.separator + USERS);
		Path temp = Paths.get(SERVER + File.separator + "temp.txt");
		// decipher file and read content
		SecretKey sk = SecurityUtil.getKeyFromServer();
		try {
			SecurityUtil.decipherFile(usersFile, sk, temp);
			BufferedReader b = new BufferedReader(new FileReader(temp.toFile()));
			String str = b.readLine();
			while (str != null) {
				System.out.println("READFILE" + str);
				splitLine(str);
				str = b.readLine();
			}
			b.close();
			Files.deleteIfExists(temp);
		} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
	}

	/**
	 * split a line by : and separate user from password
	 * 
	 * @param str
	 */
	private void splitLine(String str) {
		String[] userPass = str.split(":");
		User u = null;
		if (userPass.length == 1) {
			u = new User(userPass[0]);
			System.out.println(u);
		} else {
			u = new User(userPass[0], userPass[1]);
			System.out.println(u);
		}
		System.out.println(mapUsers);
		mapUsers.put(u.getName(), u);
	}

	/**
	 * method to build a ciphered users.txt file
	 * 
	 * @return true is the file was created and false if the file already exists
	 *         in the server
	 */
	private boolean buildUsers() {
		System.out.println("BUILDUSERS");
		File users = new File(SERVER + File.separator + USERS);
		File temp = new File(SERVER + File.separator + "temp.txt");
		boolean create = false;
		// if users.txt does not exist create
		if (!users.exists()) {
			try {
				System.out.println("BUILDUSERS::TRY");
				create = temp.createNewFile();
				System.out.println("BUILDUSERS::TRY + CREATE = " + create);
				// GET key an cipher this file with server.key
				SecretKey sk = SecurityUtil.getKeyFromServer();
				SecurityUtil.cipherFile(temp.toPath(), sk, users.toPath());
				System.out.println("BUILDUSERS:: AFTER ENCRIPT");
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("THE FILE CAN NOT BE CREATED:: CHECK PERMISSIONS");
			}
		}
		return create;
	}

	public Map<String, User> getMapUsers() {
		return mapUsers;
	}

	public void setMapUsers(Map<String, User> mapUsers) {
		this.mapUsers = mapUsers;
	}

	/**
	 * method to register a user in the map
	 * 
	 * @param name
	 *            of the user
	 * @param password
	 *            of the user
	 * @return
	 */
	public boolean registerUser(String name, String password) {
		System.out.println("REGISTER USER");
		Path users = Paths.get(SERVER + File.separator + USERS);
		Path temp = Paths.get(SERVER + File.separator + "temp.txt");

		mapUsers.put(name, new User(name, password));
		// get secretKey of the server
		SecretKey sk = SecurityUtil.getKeyFromServer();
		
		//
		try {
			// decript file of users
			// TODO: doesn't check if file exists
			SecurityUtil.decipherFile(users, sk, temp);
			
			
			persisteUser(name, password);
			
			
			// encript file of users
			SecurityUtil.cipherFile(temp, sk, users);
			

		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | IOException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		System.out.println(mapUsers);
		return true;

	}
	
	
	/*
	 * register user with user's file integrity check
	 */
	public boolean registerUser2(String name, String password) {
		System.out.println("REGISTER USER");
				
		Path users = Paths.get(SERVER + File.separator + USERS);
		SecretKey sk = SecurityUtil.getKeyFromServer();
		byte[] b = (name + ":" + password).getBytes();

		try {
			if (SecurityUtil2.checkFileIntegrity(users, sk))				
				SecurityUtil2.appendToFile(users, sk, b);
			else
				return false;
		} catch (InvalidKeyException | NoSuchAlgorithmException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		mapUsers.put(name, new User(name, password));
		System.out.println(mapUsers);
		return true;
	}

	/**
	 * Method to persist the user in a temp.txt file
	 * 
	 * @param name
	 *            of the user
	 * @param password
	 *            of the user
	 */
	public void persisteUser(String name, String password) {
		System.out.println("PERSISTING USER");
		try (FileWriter fw = new FileWriter(new File(SERVER + File.separator + "temp.txt"), true);
				BufferedWriter bf = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bf)) {
			out.println(name + ":" + password);
		} catch (IOException e) {
			System.err.println("PROBLEM PERSISTING THE USER");
		}
		System.out.println(mapUsers);
	}

	/**
	 * method to check if the user already exists
	 * 
	 * @param user
	 * @return
	 */
	public boolean userExists(String user) {
		return mapUsers.containsKey(user);
	}

}
