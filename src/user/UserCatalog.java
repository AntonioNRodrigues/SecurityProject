package user;

import static utilities.ReadWriteUtil.SERVER;
import static utilities.ReadWriteUtil.USERS;

import java.io.BufferedWriter;
import java.io.File;
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

import com.sun.corba.se.spi.ior.Writeable;

import utilities.SecurityUtil;
import utilities.SecurityUtil2;

public class UserCatalog {
	private Map<String, User> mapUsers;

	/*public UserCatalog() {
		this.mapUsers = new ConcurrentHashMap<>();
		if (!buildUsers()) {
			readFile();
		}
		System.out.println(mapUsers);
	}*/
	
	public UserCatalog() {
		this.mapUsers = new ConcurrentHashMap<>();
		try {
			if (!loadUsers()) {
				System.out.println("server process halted");
				System.exit(0);
			}
			
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(mapUsers);
	}


	/**
	 * Read the file users.txt and populates the map with user:password
	 */
	private void readFile() {
		Path usersFile = Paths.get(SERVER + File.separator + USERS);
		// decipher file and read content
		SecretKey sk = SecurityUtil.getKeyFromServer();
		try {
			String content = SecurityUtil.decipherFileToMemory(usersFile.toFile(), sk);
			String[] array = content.split("\n");
			for (String s : array) {
				splitLine(s);
			}
		} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException e) {
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
			u = new User(userPass[0].trim());
		} else {
			u = new User(userPass[0].trim(), userPass[1].trim());
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
				create = temp.createNewFile();
				// GET key an cipher this file with server.key
				SecretKey sk = SecurityUtil.getKeyFromServer();				
				SecurityUtil.cipherFile(temp.toPath(), sk, users.toPath());
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
			// TODO: doesn't check if file exists --> THIS CHECK IS MADE IN THE BUILDUSERS
			SecurityUtil.decipherFile(users, sk, temp);
			
			persisteUser(name, password);
			
			// encript file of users
			SecurityUtil.cipherFile(temp, sk, users);
			

		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | IOException e) {
			e.printStackTrace();
		}

		System.out.println(mapUsers);
		return true;

	}
		
	/*
	 * register user with user's file integrity check
	 */
	public boolean registerUser2(String name, String password) {
		System.out.println("REGISTER USER2");

		Path file = Paths.get(SERVER + File.separator + USERS);
		Path hmacFile = Paths.get(SERVER + File.separator + "." + file.getFileName() + ".hmac");
		SecretKey sk = SecurityUtil.getKeyFromServer();
		byte[] b = (name + ":" + password).getBytes();

		try {
			if (Files.exists(file)) {

				if (Files.exists(hmacFile)) {

					// append new user to current file
					if (SecurityUtil2.checkFileIntegrity(file, hmacFile, sk)) {
						SecurityUtil2.appendToFile(file, sk, b);
						SecurityUtil2.writeHMACFile(file, hmacFile, sk);
					} else {
						System.out.println("incorrect hmac user file");
						return false;
					}
				} else {
					System.out.println("hmac user file doesnt exist");
					return false;
				}
			} else {
				// create new file
				SecurityUtil2.cipherFile(file, sk, b);
				System.out.println("SecurityUtil2.cipherFile(users, sk, b);");
				SecurityUtil2.writeHMACFile(file, hmacFile, sk);
				System.out.println("SecurityUtil2.writeHMACFile(users, sk);");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		mapUsers.put(name, new User(name, password));
		System.out.println("mapusers: " + mapUsers);
		return true;
	}	
	
	/**
	 * Read the users encrypted file and populates the users map with user:password
	 * @throws IOException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public boolean loadUsers() throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException {

		System.out.println("load users from encrypted users file");
		Path usersFile = Paths.get(SERVER + File.separator + USERS);
		Path usersHmac = Paths.get(SERVER + File.separator + "." + usersFile.getFileName() + ".hmac");

		if (Files.exists(usersFile)) {
			
				if (Files.exists(usersHmac)) {
				
					// decipher file and read content
					SecretKey sk = SecurityUtil.getKeyFromServer();

					if (SecurityUtil2.checkFileIntegrity(usersFile, usersHmac, sk)) {				
			
						try {
							
							byte[] b = SecurityUtil2.decipherFile2Memory(usersFile, sk);
							String content = new String(b);
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
					else {
						System.out.println("incorrect hmac users file");
						return false;
					}
				} else {
					System.out.println("hmac users file doesnt exist");
					return false;
				}		
			}
		else
			System.out.println("users file doesnt exist");
		
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
