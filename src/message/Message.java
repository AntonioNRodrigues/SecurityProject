package message;

import java.io.Serializable;

import user.User;

public class Message implements Serializable{
	private static final long serialVersionUID = 1L;
	private User localUser;
	private String serverAdress;
	private String password;

	public Message(User localUser, String serverAdress, String password) {
		super();
		this.localUser = localUser;
		this.serverAdress = serverAdress;
		this.password = password;
	}

	public User getLocalUser() {
		return localUser;
	}

	public void setLocalUser(User localUser) {
		this.localUser = localUser;
	}

	public String getServerAdress() {
		return serverAdress;
	}

	public void setServerAdress(String serverAdress) {
		this.serverAdress = serverAdress;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
