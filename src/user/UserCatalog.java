package user;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;

import utilities.SecurityUtil;

import static utilities.ReadWriteUtil.SERVER;
import static utilities.ReadWriteUtil.USERS;

public class UserCatalog {
	private Map<String, User> mapUsers;

	public UserCatalog() {
		super();
		this.mapUsers = new ConcurrentHashMap<>();
		if (!buildUsers()) {
			readFile();
		}
		System.out.println(mapUsers);
	}
	private void readFile() {
		
		Path usersFile = Paths.get(SERVER + File.separator + USERS);
		//decipher file and read content --> to do
		try (BufferedReader b = new BufferedReader(new FileReader(usersFile.toFile()))){
			String str = b.readLine();
			while (str != null) {
				splitLine(str);
				str = b.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void splitLine(String str) {
		String[] userPass = str.split(":");
		User u = null;
		if (userPass.length == 1) {
			u = new User(userPass[0]);
		} else {
			u = new User(userPass[0], userPass[1]);
		}
		mapUsers.put(u.getName(), u);
	}

	private boolean buildUsers() {
		File userFile = new File(SERVER + File.separator + USERS);
		boolean create = false;
		if (!userFile.exists()) {
			try {
				create = userFile.createNewFile();
				//GET key an chiper this file with server.key
				SecretKey sk = SecurityUtil.getKeyFromServer();
				SecurityUtil.encriptFile(userFile, sk);
			} catch (Exception e) {
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

	public boolean registerUser(String name, String password) {
		System.out.println("REGISTER USER");
		mapUsers.put(name, new User(name, password));
		persisteUser(name, password);
		System.out.println(mapUsers);
		return true;

	}

	public void persisteUser(String name, String password) {
		System.out.println("PERSISTING USER");
		//decipher file,  write content, cipher again --> to do
		try (FileWriter fw = new FileWriter(new File(SERVER + File.separator + USERS), true);
				BufferedWriter bf = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bf)) {
			out.println(name + ":" + password);
		} catch (IOException e) {
			System.err.println("PROBLEM PERSISTING THE USER");
		}
		System.out.println(mapUsers);
	}

	public boolean userExists(String user) {
		return mapUsers.containsKey(user);
	}

}
