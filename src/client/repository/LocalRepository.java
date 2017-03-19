package client.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import user.User;

public class LocalRepository {
	private User onwer;
	private Long timestamp;
	private String nameRepo;
	private List<Path> listFiles;
	private List<String> sharedUsers;

	public LocalRepository(String name) {
		this.nameRepo = name;
		this.listFiles = new ArrayList<>();

		loadLocalRepo();
	}

	/*
	 * To be used in push/pull repo operations...a local repository object will
	 * be created, listFiles will contain the list of the folder files...
	 */
	private void loadLocalRepo() {

		try (Stream<Path> paths = Files.walk(Paths.get("CLIENT" + File.separator + this.nameRepo))) {
			paths.forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					listFiles.add(filePath);
					System.out.println(filePath);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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

	public List<Path> getListFiles() {
		return listFiles;
	}

	public void setListFiles(List<Path> listFiles) {
		this.listFiles = listFiles;
	}

	public List<String> getListSharedUsers() {
		return sharedUsers;
	}

	public void setListUsers(List<String> listUsers) {
		this.sharedUsers = listUsers;
	}

}
