package server.repository;

import static utilities.ReadWriteUtil.OWNER;
import static utilities.ReadWriteUtil.SERVER;
import static utilities.ReadWriteUtil.SHARED;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sun.xml.internal.ws.config.management.policy.ManagementPolicyValidator;

import utilities.ReadWriteUtil;

public class RemoteRepository {
	private String owner;
	private Long timestamp;
	private String nameRepo;
	/**
	 * For each file we have its list of versions
	 */
	private Map<String, CopyOnWriteArrayList<Path>> mapVersions;
	private List<String> sharedUsers;

	public RemoteRepository(String nameRepo) {
		super();
		this.nameRepo = nameRepo;
		this.mapVersions = new ConcurrentHashMap<>();
		this.sharedUsers = new CopyOnWriteArrayList<String>();
		persisteRemRepo();
	}

	public RemoteRepository(String onwer, String nameRepo) {
		super();
		this.owner = onwer;
		this.nameRepo = nameRepo;
		this.mapVersions = new ConcurrentHashMap<>();
		this.sharedUsers = new CopyOnWriteArrayList<String>();
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
	 * method to write the name of the shared user.
	 * 
	 * @param userName
	 * @param remoteRepository
	 */
	private void persisteSharedUser(String userName, RemoteRepository remoteRepository) {
		System.out.println("persisteSharedUser");
		try (BufferedWriter bf = new BufferedWriter(
				new FileWriter(new File(SERVER + File.separator + this.nameRepo + File.separator + SHARED), true));
				PrintWriter out = new PrintWriter(bf)) {
			out.println(userName);
		} catch (IOException e) {
			System.err.println("PROBLEM ADDING THE USER TO THE SHARED FILE");
		}
	}

	public void removeUserFromRepo(String userId) {
		sharedUsers.remove(userId);
		removeUserFromSharedRepo(userId);
		System.out.println("Current List of Shared Users for the " + this.nameRepo + ": " + sharedUsers);
	}

	private void removeUserFromSharedRepo(String userId) {

		UUID uuid = UUID.randomUUID();
		String uuidString = SERVER + File.separator + uuid.toString();
		String fileName = SERVER + File.separator + this.nameRepo + File.separator + SHARED;
		File inputFile = new File(fileName);
		File tempFile = null;
		try {
			tempFile = File.createTempFile(uuidString, ".txt");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));) {

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.equals(userId))
					continue;
				writer.write(line + System.getProperty("line.separator"));
			}

			if (inputFile.delete())
				if (tempFile.renameTo(inputFile))
					System.out.println("O utilizador " + userId + " foi removido do ficheiro " + fileName);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public List<String> getSharedUsers() {
		return sharedUsers;
	}

	@Override
	public String toString() {
		return "RemoteRepository [onwer=" + owner + ", timestamp=" + timestamp + ", nameRepo=" + nameRepo
				+ ", shared with=" + sharedUsers + "]";
	}

	public Map<String, CopyOnWriteArrayList<Path>> getMapVersions() {
		return mapVersions;
	}

	public void setMapVersions(Map<String, CopyOnWriteArrayList<Path>> mapVersions) {
		this.mapVersions = mapVersions;
	}

}