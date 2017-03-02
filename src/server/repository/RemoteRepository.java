package server.repository;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import files.Ficheiro;

public class RemoteRepository {
	private String onwer;
	private Long timestamp;
	private String nameRepo;
	private Map<String, List<File>> mapFiles;

	public RemoteRepository( Long timestamp, String nameRepo) {
		super();
		this.timestamp = timestamp;
		this.nameRepo = nameRepo;
		this.mapFiles = new ConcurrentHashMap<>();
	}
	public RemoteRepository(String onwer, Long timestamp, String nameRepo) {
		super();
		this.onwer = onwer;
		this.timestamp = timestamp;
		this.nameRepo = nameRepo;
		this.mapFiles = new ConcurrentHashMap<>();
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
	
	public void addFilesToRepo(String repoName, List<File> listFiles){
		this.mapFiles.put(repoName, listFiles);
	}
	@Override
	public String toString() {
		return "RemoteRepository [onwer=" + onwer + ", timestamp=" + timestamp + ", nameRepo=" + nameRepo + "]";
	}
}