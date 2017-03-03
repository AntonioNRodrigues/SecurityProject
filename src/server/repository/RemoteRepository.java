package server.repository;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RemoteRepository {
	private String onwer;
	private Long timestamp;
	private String nameRepo;
	private Map<String, List<File>> mapFiles;
	private List<String> sharedUsers; // outros users também com acesso ao
										// repositório

	public RemoteRepository(Long timestamp, String nameRepo) {
		super();
		this.timestamp = timestamp;
		this.nameRepo = nameRepo;
		this.mapFiles = new ConcurrentHashMap<>();
		this.sharedUsers = new CopyOnWriteArrayList<String>(); //Lista concorrente
	}

	public RemoteRepository(String onwer, Long timestamp, String nameRepo) {
		super();
		this.onwer = onwer;
		this.timestamp = timestamp;
		this.nameRepo = nameRepo;
		this.mapFiles = new ConcurrentHashMap<>();
		this.sharedUsers = new CopyOnWriteArrayList<String>();
	}

	public String getOnwer() {
		return onwer;
	}

	public void setOnwer(String onwer) {
		this.onwer = onwer;
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
		return "RemoteRepository [onwer=" + onwer + ", timestamp=" + timestamp
				+ ", nameRepo=" + nameRepo + ", shared with=" + sharedUsers
				+ "]";
	}

}