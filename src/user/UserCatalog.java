package user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
			// TODO Auto-generated catch block
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

}
