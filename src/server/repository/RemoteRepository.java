package server.repository;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import files.Ficheiro;
import user.User;

public class RemoteRepository {
	private User onwer;
	private Timestamp timestamp;
	private String nameRepo;
	private Map<String, Set<Ficheiro>> mapFiles;
	public User getOnwer() {
		return onwer;
	}
	public void setOnwer(User onwer) {
		this.onwer = onwer;
	}
	public Timestamp getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Timestamp timestamp) {
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
	public Set<Ficheiro> getVersionList(String nameFile){
		return mapFiles.get(nameFile);
	}
	
	
	
}