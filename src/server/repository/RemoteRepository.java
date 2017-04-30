package server.repository;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static utilities.ReadWriteUtil.OWNER;
import static utilities.ReadWriteUtil.SERVER;
import static utilities.ReadWriteUtil.SHARED;
import static utilities.ReadWriteUtil.USERS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import utilities.SecurityUtil;
import utilities.SecurityUtil2;

public class RemoteRepository {
	private String owner;
	private Long timestamp;
	private String nameRepo;
	/**
	 * For each file we have its list of versions
	 */
	private Map<String, CopyOnWriteArrayList<Path>> mapVersions;
	private List<String> sharedUsers;
	/**
	 * Map of the users that have shared access to the repository
	 */
	private Map<String, PublicKey> sharedPublicKey;

	public RemoteRepository(String nameRepo) {
		super();
		this.nameRepo = nameRepo;
		this.mapVersions = new ConcurrentHashMap<>();
		this.sharedUsers = new CopyOnWriteArrayList<>();
		this.setSharedPublicKey(new ConcurrentHashMap<>());
		persisteRemRepo();
	}

	public RemoteRepository(String onwer, String nameRepo) {
		super();
		this.owner = onwer;
		this.nameRepo = nameRepo;
		this.mapVersions = new ConcurrentHashMap<>();
		this.sharedUsers = new CopyOnWriteArrayList<>();
		this.setSharedPublicKey(new ConcurrentHashMap<>());
		persisteRemRepo();
	}

	private CopyOnWriteArrayList<Path> getSortedList() {
		CopyOnWriteArrayList<Path> cp = new CopyOnWriteArrayList<>();
		cp.sort(myComparator());
		return cp;
	}

	public CopyOnWriteArrayList<Path> getUniqueList() {
		CopyOnWriteArrayList<Path> uniqueList = new CopyOnWriteArrayList<>();

		for (Map.Entry<String, CopyOnWriteArrayList<Path>> pair : mapVersions.entrySet()) {
			pair.getValue().sort(myComparator());
			uniqueList.add(pair.getValue().get(0));
		}
		return uniqueList;
	}

	private Comparator<Path> myComparator() {
		Comparator<Path> myComparator = new Comparator<Path>() {

			@Override
			public int compare(Path file1, Path file2) {

				int value = 0;
				if (file1.toFile().lastModified() < file2.toFile().lastModified()) {
					value = 1;
				}
				if (file1.toFile().lastModified() > file2.toFile().lastModified()) {
					value = -1;
				}
				if (file1.toFile().lastModified() == file2.toFile().lastModified()) {
					value = 0;
				}
				return value;
			}
		};
		return myComparator;
	}

	private void persisteRemRepo() {
		File f = new File(SERVER + File.separator + this.nameRepo);
		if (!f.exists()) {
			f.mkdirs();
			try (BufferedWriter fi = new BufferedWriter(
					new FileWriter(new File(f.getAbsolutePath() + File.separator + OWNER)))) {
				// write in the file the owner
				fi.write(this.owner);
				// create a file shared.txt
				File shared = new File(SHARED);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		setTimestamp(f.lastModified());
	}

	public File getFile(String nameFile) {
		if (mapVersions.get(nameFile) != null) {
			mapVersions.get(nameFile).sort(myComparator());
			return mapVersions.get(nameFile).get(0).toFile();
		}
		return null;
	}

	public boolean fileExists(String fileName) {
		return (this.getMapVersions().get(fileName) == null) ? false : true;
	}

	public void addFile(String nameFile, File received) {
		CopyOnWriteArrayList<Path> l = getMapVersions().get(nameFile);
		if (l == null) {
			CopyOnWriteArrayList<Path> cp = getSortedList();
			cp.add(received.toPath());
			this.getMapVersions().put(nameFile, cp);
			System.out.println(this.getMapVersions());
		} else {
			this.getMapVersions().get(nameFile).add(received.toPath());
			System.out.println(this.getMapVersions());
		}

	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getNameRepo() {
		return nameRepo;
	}

	public void setNameRepo(String nameRepo) {
		this.nameRepo = nameRepo;
	}

	public List<String> getSharedUsers() {
		return sharedUsers;
	}

	public Map<String, CopyOnWriteArrayList<Path>> getMapVersions() {
		return mapVersions;
	}

	public void setMapVersions(Map<String, CopyOnWriteArrayList<Path>> mapVersions) {
		this.mapVersions = mapVersions;
	}

	public Map<String, PublicKey> getSharedPublicKey() {
		return sharedPublicKey;
	}

	public void setSharedPublicKey(Map<String, PublicKey> sharedPublicKey) {
		this.sharedPublicKey = sharedPublicKey;
	}

	/**
	 * method to add the user to the shared list to this repository and persist
	 * the user in the file if its not already there.
	 * 
	 * @param userName
	 */
	public void addShareUserToRepo(String userName) {
		// if the does not have access to the repo add it
		if (!(existsInSharedList(userName))) {
			sharedUsers.add(userName);
			sharedPublicKey.put(userName, null);
			persisteSharedUser(userName, this);
		}
		System.out.println("Current List of Shared Users for the " + this.nameRepo + ": " + sharedUsers);
	}

	/**
	 * method to add the user to the shared list to this repository and persist
	 * the user in the file if its not already there.
	 * 
	 * @param userName
	 */
	public void addPublicKeytoShareUserToRepo(String userName) {
		// if the does not have access to the repo add it
		if (existsInSharedMap(userName)) {
			sharedPublicKey.put(userName, null);
			persisteSharedUser(userName, this);
		}
		System.out.println("Current List of Shared Users for the " + this.nameRepo + ": " + sharedUsers);
	}

	/*
	 * method to check is the user is already in the list of shared users
	 */
	private boolean existsInSharedList(String userName) {
		return sharedUsers.contains(userName) ? true : false;
	}

	/**
	 * method to check if the user exists in the map of shared users
	 * 
	 * @param userName
	 * @return
	 */
	private boolean existsInSharedMap(String userName) {
		return sharedPublicKey.containsKey(userName) ? true : false;
	}

	public boolean addPublicKeySharedUser(String userName, PublicKey pk) {
		// if the entry for the username exists and its value is null
		if (existsInSharedMap(userName) && (sharedPublicKey.get(userName) == null)) {
			sharedPublicKey.putIfAbsent(userName, pk);
			return true;
		}
		return false;
	}

	/**
	 * method to write the name of the shared user.
	 * 
	 * @param userName
	 * @param remoteRepository
	 */
	private void persisteSharedUser(String userName, RemoteRepository remoteRepository) {
		
		System.out.println("persisteSharedUser");

		Path file = Paths.get(SERVER + File.separator + this.nameRepo + File.separator + SHARED);
		Path hmacFile = Paths.get(SERVER + File.separator + "." + file.getFileName() + ".hmac");
		SecretKey sk = SecurityUtil.getKeyFromServer();
		byte[] b = userName.getBytes();

		try {
			if (Files.exists(file)) {

				if (Files.exists(hmacFile)) {

					// append new user to current file
					if (SecurityUtil2.checkFileIntegrity(file, hmacFile, sk)) {
						SecurityUtil2.appendToFile(file, sk, b);
						SecurityUtil2.writeHMACFile(file, hmacFile, sk);
					} else {
						System.out.println("incorrect repo shared with info hmac file");
					}
				} else {
					System.out.println("repo shared with info hmac file doesnt exist");
				}
			} else {
				// create new file
				SecurityUtil2.cipherFile(file, sk, b);
				System.out.println("SecurityUtil2.cipherFile(users, sk, b);");
				SecurityUtil2.writeHMACFile(file, hmacFile, sk);
				System.out.println("SecurityUtil2.writeHMACFile(users, sk);");
			}
		} catch (Exception e) {
			System.err.println("Existiu um problema a adicinar o user aos ficheiro");
			e.printStackTrace();
		}
	}

	public void removeSharedUserFromRepo(String userId) {
		sharedUsers.remove(userId);
		sharedPublicKey.remove(userId);
		removeUserFromSharedRepo(userId);
		System.out.println("Current List of Shared Users for the " + this.nameRepo + ": " + sharedUsers);
	}

	
	private void removeUserFromSharedRepo(String userId) {

		Path file = Paths.get(SERVER + File.separator + this.nameRepo + File.separator + SHARED);
		Path hmacFile = Paths.get(SERVER + File.separator + "." + file.getFileName() + ".hmac");
		SecretKey sk = SecurityUtil.getKeyFromServer();
		byte[] b = userId.getBytes();

		try {
			if (Files.exists(file)) {

				if (Files.exists(hmacFile)) {

					if (SecurityUtil2.checkFileIntegrity(file, hmacFile, sk)) {
						
						updateFile(file, sk, b);
						
						SecurityUtil2.writeHMACFile(file, hmacFile, sk);
						
						System.out.println("O utilizador " + userId + " foi removido do ficheiro " + file.getFileName());

					} else {
						System.out.println("incorrect repo shared with info hmac file");
					}
				} else {
					System.out.println("repo shared with info hmac file doesnt exist");
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
		}
	}
	
	
	public void updateFile(Path file, SecretKey secretKey, byte[] text)
			throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream( );

		for (int i= 0; i<this.sharedUsers.size(); i++) {
			baos.write(this.sharedUsers.get(i).getBytes());
			if (i<this.sharedUsers.size()-1) 
				baos.write("\n".getBytes());
		}
		byte fileContent[] = baos.toByteArray( );
		
		Path tempFile = Files.createTempFile("foobar", ".tmp");		
		//encode again to temp file
		SecurityUtil2.cipherFile(tempFile, secretKey, fileContent);

		//move temp file to file
		CopyOption[] options = new CopyOption[] { REPLACE_EXISTING };
		Files.copy(tempFile, file, options);
		Files.delete(tempFile);
	}
	

	@Override
	public String toString() {
		return "RemoteRepository [owner=" + owner + ", timestamp=" + timestamp + ", nameRepo=" + nameRepo
				+ ", mapVersions=" + mapVersions + ", sharedUsers=" + sharedUsers + ", sharedPublicKey="
				+ sharedPublicKey + "]";
	}
}