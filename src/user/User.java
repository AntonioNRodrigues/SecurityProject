package user;

import java.io.Serializable;
import java.security.MessageDigest;

import utilities.SecurityUtil;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String password;
	private byte[] b;

	public User(String name) {
		super();
		this.name = name;
		this.password = "";
	}

	public User(String name, String password) {
		super();
		this.name = name;
		this.password = password;
	}

	public User(String name, String password, String nonce) {
		super();
		this.name = name;
		String str = password + nonce;
		this.b = SecurityUtil.calcSintese(str);
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "User [name=" + name + ", password=" + password + "]";
	}

	public byte[] getB() {
		return b;
	}

	public void setB(byte[] b) {
		this.b = b;
	}

}
