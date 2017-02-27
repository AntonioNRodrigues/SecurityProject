package server.repository;

import java.util.Map;
import java.util.Set;

import files.Ficheiro;

public class RemoteRepository {
	private String onwer;
	private Long timestamp;
	private String nameRepo;
	private Map<String, Set<Ficheiro>> mapFiles;

	public RemoteRepository( Long timestamp, String nameRepo) {
		super();
		this.timestamp = timestamp;
		this.nameRepo = nameRepo;
	}
	public RemoteRepository(String onwer, Long timestamp, String nameRepo) {
		super();
		this.onwer = onwer;
		this.timestamp = timestamp;
		this.nameRepo = nameRepo;
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

	public Map<String, Set<Ficheiro>> getMapFiles() {
		return mapFiles;
	}

	public void setMapFiles(Map<String, Set<Ficheiro>> mapFiles) {
		this.mapFiles = mapFiles;
	}

	public Set<Ficheiro> getVersionList(String nameFile) {
		return mapFiles.get(nameFile);
	}

	@Override
	public String toString() {
		return "RemoteRepository [onwer=" + onwer + ", timestamp=" + timestamp + ", nameRepo=" + nameRepo + "]";
	}

}