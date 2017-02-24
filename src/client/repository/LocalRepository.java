package client.repository;

import java.sql.Timestamp;
import java.util.List;

import files.Ficheiro;
import user.User;

public class LocalRepository {
	private User onwer;
	private Timestamp timestamp;
	private String nameRepo;
	private List<Ficheiro> listFiles;
	
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

	public List<Ficheiro> getListFiles() {
		return listFiles;
	}

	public void setListFiles(List<Ficheiro> listFiles) {
		this.listFiles = listFiles;
	}

}
