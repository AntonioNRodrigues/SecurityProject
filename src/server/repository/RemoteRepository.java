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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class RemoteRepository {
	private String owner;
	private Long timestamp;
	private String nameRepo;
	private Map<String, List<File>> mapFiles;
	private List<String> sharedUsers;

	public RemoteRepository(String nameRepo) {
		super();
		this.nameRepo = nameRepo;
		this.mapFiles = new ConcurrentHashMap<>();
		this.sharedUsers = new CopyOnWriteArrayList<String>();
		persisteRemRepo();
	}

	public RemoteRepository(String onwer, String nameRepo) {
		super();
		this.owner = onwer;
		this.nameRepo = nameRepo;
		this.mapFiles = new ConcurrentHashMap<>();
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
		System.out.println(this);
	}

	/**
	 * Get the recent version of a file, NOT TESTED-------------------
	 * 
	 * @param nameFile
	 * @return
	 */
	public File getMostRecentFile(String nameFile) {

		System.out.println("nameFile: " + nameFile);

		mapFiles.forEach((key, value) -> {
			System.out.println(" : " + key + " Value : " + value);
		});

		List<File> temp = getMapFiles().get(nameFile);
		System.out.println("BEFORE SORT " + temp);
		Collections.sort(temp, new Comparator<File>() {

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

		System.out.println("AFTER SORT " + temp);
		// Collections.sort(temp, Collections.reverseOrder());
		return temp.get(0);
	}

	public File getFile(String nameRepo, String nameFile) {

		System.out.println("nameFile: " + nameFile);

		mapFiles.forEach((key, value) -> {
			System.out.println("key : " + key + " Value : " + value);
		});

		System.out.println(SERVER + File.separator + nameRepo + File.separator + nameFile);

		List<File> repoFiles = getMapFiles().get(nameRepo);

		System.out.println("repoFiles.size(): " + repoFiles.size());

		for (File f : repoFiles)
			System.out.println(f.getName());

		for (File f : repoFiles) {
			if (f != null && f.getName().equals(nameFile)) {
				return f;
			}
		}
		return null;
	}

	public List<File> getFiles(String nameRepo) {

		if (!(getMapFiles().get(nameRepo).isEmpty()))
			return getMapFiles().get(nameRepo);

		return null;
	}

	/**
	 * method to give a set of the most recent file that are in the map
	 * 
	 * @return set with unique files and each file is the recent version
	 */
	public Set<File> getListMostRecentFiles() {
		Set<File> set = new ConcurrentSkipListSet<>();
		for (String name : mapFiles.keySet()) {
			set.add(getMostRecentFile(name));
		}
		return set;
	}

	/**
	 * method to give the size of the unique values present in the map This
	 * exclude the files that have different versions in the repository.
	 * 
	 * @return size of set
	 */
	public int sizeUniqueFilesInMap() {
		return getListMostRecentFiles().size();
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

	public List<File> getVersionList(String nameFile) {
		return mapFiles.get(nameFile);
	}

	public Map<String, List<File>> getMapFiles() {
		return mapFiles;
	}

	public void setMapFiles(Map<String, List<File>> mapFiles) {
		this.mapFiles = mapFiles;
	}

	public void addFilesToRepo(String repoName, List<File> listFiles) {
		this.mapFiles.put(repoName, listFiles);
	}

	/**
	 * method to split the filename by the . and adds the number of versions in
	 * the file
	 * 
	 * @param nameFile
	 * @return nameFile in version mode
	 */
	public String getNextNameFile(String nameFile) {
		int i = getMapFiles().get(nameFile).size();
		String[] a = nameFile.split(".");
		return a[0] + "(" + i + ")." + a[1];
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
		for (String string : sharedUsers) {
			if (string.equals(userName)) {
				return true;
			}
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
		String str = SERVER + File.separator + this.nameRepo + File.separator + SHARED;
		try (BufferedReader br = new BufferedReader(new FileReader(new File(str)));
				PrintWriter out = new PrintWriter(new File(str))) {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				System.out.println(line);
				if (line.equals(userId)) {
					// out.write("");
					// does not work have to find another way
					break;
				}
			}
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