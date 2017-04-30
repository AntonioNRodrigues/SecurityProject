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

import javax.crypto.SecretKey;

import utilities.SecurityUtil;
import utilities.SecurityUtil2;

public class UserCatalog {
	private Map<String, User> mapUsers;
	
	public UserCatalog() {
		this.mapUsers = new ConcurrentHashMap<>();
		try {
			if (!loadUsers()) {
				System.out.println("server process halted");
				System.exit(0);
			}
			
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | IOException e) {
			e.printStackTrace();
		}
		System.out.println(mapUsers);
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


	public Map<String, User> getMapUsers() {
		return mapUsers;
	}

	public void setMapUsers(Map<String, User> mapUsers) {
		this.mapUsers = mapUsers;
	}

		
	/*
	 * register user with user's file integrity check
	 */
	public boolean registerUser(String name, String password) {

		Path file =     Paths.get(SERVER + File.separator + USERS);
		Path hmacFile = Paths.get(SERVER + File.separator + "." + USERS + ".hmac");

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
		Path file =     Paths.get(SERVER + File.separator + USERS);
		Path hmacFile = Paths.get(SERVER + File.separator + "." + USERS + ".hmac");
		
		if (Files.exists(file)) {
			
				if (Files.exists(hmacFile)) {
				
					// decipher file and read content
					SecretKey sk = SecurityUtil.getKeyFromServer();

					if (SecurityUtil2.checkFileIntegrity(file, hmacFile, sk)) {				
			
						try {
							
							byte[] b = SecurityUtil2.decipherFile2Memory(file, sk);
							String content = new String(b);
							String[] array = content.split("\n");
							for (String s : array) {
								splitLine(s);
							}
						} catch (IOException | InvalidKeyException e) {
							e.printStackTrace();
						} catch (InvalidAlgorithmParameterException e) {
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
