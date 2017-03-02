package user;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserCatalog {
	private Map<String, User> mapUsers;
	private static final String SERVER = "SERVER";
	private static final String USERS = "users.txt";

	public UserCatalog() {
		super();
		this.mapUsers = new ConcurrentHashMap<>();
		if (!buildUsers()) {
			readFile();
		}
		System.out.println(mapUsers);
	}

	private void readFile() {
		File f = new File(SERVER + "/" + USERS);

		try {
			BufferedReader b = null;
			b = new BufferedReader(new FileReader(f));
			String str = b.readLine();
			while (str != null) {
				splitLine(str);
				str = b.readLine();
			}
			b.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void splitLine(String str) {
		String[] userPass = str.split(":");
		User u = new User(userPass[0], userPass[1]);
		mapUsers.put(u.getName(), u);
	}

	private boolean buildUsers() {
		File userFile = new File(SERVER + "/" + USERS);
		boolean create = false;
		if (!userFile.exists()) {
			try {
				create = userFile.createNewFile();
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
		User u = mapUsers.put(name, new User(name, password));
		persisteUser(name, password);
		System.out.println(mapUsers);
		return true;

	}

	public void persisteUser(String name, String password) {
		System.out.println("PERSISTING USER");
		try (FileWriter fw = new FileWriter(new File(SERVER + "/" + USERS), true);
				BufferedWriter bf = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bf)) {
			out.println(name + ":" + password);
		} catch (IOException e) {
			System.err.println("PROBLEM PERSISTING THE USER");
		}
		System.out.println(mapUsers);
	}

}
