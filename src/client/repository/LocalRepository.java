package client.repository;

import java.io.File;
import java.util.List;

import user.User;

public class LocalRepository {
	private User onwer;
	private Long timestamp;
	private String nameRepo;
	private List<File> listFiles;
	private List<User> sharedUsers;
	
	public User getOnwer() {
		return onwer;
	}

	public void setOnwer(User onwer) {
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

	public List<File> getListFiles() {
		return listFiles;
	}

	public void setListFiles(List<File> listFiles) {
		this.listFiles = listFiles;
	}
	
	public List<User> getListSharedUsers(){
		return sharedUsers;
	}
	
	public void setListUsers(List<User> listUsers) {
		this.sharedUsers = listUsers;
	}

}
