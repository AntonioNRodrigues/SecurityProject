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

import utilities.ReadWriteUtil;

public class RemoteRepository {
	private String owner;
	private Long timestamp;
	private String nameRepo;
	private List<File> listFiles;
	private List<String> sharedUsers;

	public RemoteRepository(String nameRepo) {
		super();
		this.nameRepo = nameRepo;
		this.listFiles = new CopyOnWriteArrayList<File>();
		this.sharedUsers = new CopyOnWriteArrayList<String>();
		persisteRemRepo();
	}

	public RemoteRepository(String onwer, String nameRepo) {
		super();
		this.owner = onwer;
		this.nameRepo = nameRepo;
		this.listFiles = new CopyOnWriteArrayList<File>();
		this.sharedUsers = new CopyOnWriteArrayList<String>();
		persisteRemRepo();
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

	public File getFile(String nameRepo, String nameFile) {

		List<File> repoFiles = getListFiles();
		for (File f : repoFiles) {
			if (f != null && f.getName().equals(nameFile)) {
				return f;
			}
		}
		return null;
	}

	public Map sortList() {
		Map<String, CopyOnWriteArrayList<File>> map = new ConcurrentHashMap<>();
		Collections.sort(listFiles);
		CopyOnWriteArrayList<File> tempList = null;
		for (File f : getListFiles()) {
			String uniqueName = ReadWriteUtil.getRealFileName(f.getName());
			System.out.println(uniqueName);
			if (map.get(uniqueName) == null) {
				tempList = new CopyOnWriteArrayList<>();
				tempList.add(f);
				map.put(uniqueName, tempList);
			} else if (map.get(uniqueName) != null) {
				map.get(uniqueName).add(f);
			}
		}
		return map;
	}

	public CopyOnWriteArrayList<File> getUniqueListFiles() {
		Map<String, CopyOnWriteArrayList<File>> m = sortList();
		CopyOnWriteArrayList<File> uniqueList = new CopyOnWriteArrayList<>();
		
		for (Map.Entry<String, CopyOnWriteArrayList<File>> pair : m.entrySet()) {
			System.out.println(pair.getKey());
			System.out.println(pair.getValue());

			Collections.sort(pair.getValue(), new Comparator<File>() {

				@Override
				public int compare(File file1, File file2) {
					int value = 0;
					if (file1.lastModified() < file2.lastModified()) {
						value = 1;
					}
					if (file1.lastModified() > file2.lastModified()) {
						value = -1;
					}
					if (file1.lastModified() == file2.lastModified()) {
						value = 0;
					}
					return value;
				}
			});
			System.out.println("sorted list" + pair.getValue());
			uniqueList.add(pair.getValue().get(0));
		}
		return uniqueList;
	}

	public List<File> getFiles(String nameRepo) {
		return getListFiles();

	}

	public boolean fileExists(String repoName, String fileName) {
		for (File f : listFiles) {
			if (f.getName().equals(fileName)) {
				return true;
			}
		}
		return false;
	}

	public void addFile(String repoName, File received) {
		listFiles.add(received);
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

	public List<File> getListFiles() {
		return listFiles;
	}

	public void setListFiles(List<File> listFiles) {
		this.listFiles = listFiles;
	}

	public void addFilesToRepo(String repoName, List<File> listFiles) {
		this.listFiles = listFiles;
	}

	/**
	 * method to get the single name without the version
	 * 
	 * @param nameFile
	 * @return nameFile without version
	 */
	public String getNameWithOutVersion(String nameFile) {
		String namefile = null;
		String[] a = nameFile.split(".");
		if (a[0].length() == 3) {
			namefile = a[0].substring(0, a[0].length() - 3) + a[1];
		}
		if (a[0].length() == 4) {
			namefile = a[0].substring(0, a[0].length() - 4) + a[1];
		}
		if (a[0].length() == 5) {
			namefile = a[0].substring(0, a[0].length() - 5) + a[1];
		}
		return nameFile;

	}

	/**
	 * method to add the user to the shared list to this repository and persist
	 * the user in the file if its not already there.
	 * 
	 * @param userName
	 */
	public void addShareUserToRepo(String userName) {
		sharedUsers.add(userName);
		System.out.println("addShareUserToRepo" + sharedUsers);
		// if its not a shared user persist
		if ((isSharedUser(userName))) {
			persisteSharedUser(userName, this);
		}
		System.out.println("addShareUserToRepo" + sharedUsers);

	}

	/*
	 * method to check is the user is already in the list os shared users
	 */
	private boolean isSharedUser(String userName) {

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
		System.out.println("SHARED USERS LIST::" + sharedUsers);
		removeUserFromSharedRepo(userId);
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

}