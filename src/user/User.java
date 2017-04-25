package user;

import java.io.Serializable;
import java.nio.file.Paths;
import java.security.KeyPair;

import utilities.SecurityUtil;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String password;
	private byte[] b;
	private byte[] pubKey;

	public User(String name) {
		super();
		this.name = name;
		this.password = "";
		this.b = null;
		this.pubKey = null;
	}

	public User(String name, String password) {
		super();
		this.name = name;
		this.password = password;
		this.b = null;
		this.pubKey = null;

	}

	/**
	 * Constructor to be used in client side
	 * 
	 * @param name
	 * @param password
	 * @param nonce
	 */
	public User(String name, String password, String nonce) {
		super();
		this.name = name;
		String str = password + nonce;
		this.b = SecurityUtil.calcSintese(str);
		this.password = password;
		KeyPair kpair = SecurityUtil.getKeyPairFromKS(Paths.get(".myGitClientKeyStore"), "mygitclient", "badpassword2");
		// persistKeyPair into the client folder
		this.pubKey = kpair.getPublic().getEncoded();

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

	public byte[] getPubKey() {
		return pubKey;
	}

	public void setPubKey(byte[] pubKey) {
		this.pubKey = pubKey;
	}

}
