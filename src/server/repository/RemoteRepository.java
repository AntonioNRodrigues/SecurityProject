package server.repository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RemoteRepository {
	private String owner;
	private Long timestamp;
	private String nameRepo;
	private Map<String, List<File>> mapFiles;
	private List<String> sharedUsers; // other users have access to repo

	public RemoteRepository(String nameRepo) {
		super();
		this.nameRepo = nameRepo;
		this.mapFiles = new ConcurrentHashMap<>();
		this.sharedUsers = new CopyOnWriteArrayList<String>();
		persisteRemRepo();
	}

	private void persisteRemRepo() {
		File f = new File("SERVER/" + this.nameRepo);
		if (!f.exists()) {
			f.mkdirs();
			File ff = new File(f.getAbsolutePath() + "/owner.txt");
			try (BufferedWriter fi = new BufferedWriter(new FileWriter(ff))) {
				fi.write(this.owner);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		setTimestamp(f.lastModified());
		System.out.println(this);
	}

	public RemoteRepository(String onwer, String nameRepo) {
		super();
		this.owner = onwer;
		this.nameRepo = nameRepo;
		this.mapFiles = new ConcurrentHashMap<>();
		this.sharedUsers = new CopyOnWriteArrayList<String>();
		persisteRemRepo();
	}

	/**
	 * Get the recent version of a file, this is wrong it has to be sorted by
	 * lastModified param
	 * TO DO ----------------------->
	 * FOR THE MOMENT GETS THE FIRST VALUE OF LIST 
	 * I DONT KNOW HOW THIS IS SORTED
	 * CHECK THE DEFAULT ORDER OF A FILE
	 * @param nameFile
	 * @return
	 */
	public File getMostRecentFile(String nameFile) {
		List<File> temp = getMapFiles().get(nameFile);
		Collections.sort(temp, Collections.reverseOrder());
		return temp.get(0);
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

	public void addUserToRepo(String userName) {
		sharedUsers.add(userName);
	}

	public void removeUserFromRepo(String userId) {
		sharedUsers.remove(userId);
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