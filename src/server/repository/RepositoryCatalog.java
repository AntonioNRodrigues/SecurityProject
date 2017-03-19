package server.repository;

import static utilities.ReadWriteUtil.OWNER;
import static utilities.ReadWriteUtil.SERVER;
import static utilities.ReadWriteUtil.SHARED;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import user.User;

public class RepositoryCatalog {

	private Map<String, RemoteRepository> mapRemRepos;

	public RepositoryCatalog() {
		super();
		//System.out.println("REPOSITORY CATALOG IS ON");
		buildMap();
		listRepos();
	}

	private void buildMap() {
		this.mapRemRepos = new ConcurrentHashMap<String, RemoteRepository>();
		// if buildFolder == true do nothing
		// there is no need to populate the map with repositories
		// else going to read the folder and populate the map
		if (!buildFolder()) {
			readFolder();
		}
	}

	/**
	 * method to read the SERVER folder, build the object representation of each
	 * Repository and populate the mapFiles with each Repository and its files.
	 */
	private void readFolder() {
		System.out.println("READ FOLDER");
		RemoteRepository rr = null;
		// list of files inside Server
		for (String strFolder : new File(SERVER).list()) {
			// repositories folders

			System.out.println("Repository folder: "+strFolder);

			File f = new File(SERVER + File.separator + strFolder);
			if (f.isDirectory()) {
				// build a repository

				rr = new RemoteRepository(f.getName());
				rr.addFilesToRepo(rr.getNameRepo(), new ArrayList(Arrays.asList(f.listFiles())));

				// inside each folder/repository exists a owner.txt file
				String owner = null;
				try {
					owner = getOwner(f);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (owner != null) {
					rr.setOwner(owner);
				}
				mapRemRepos.put(rr.getNameRepo(), rr);

				// inside each folder/repository may exists a shared.txt file
				try {
					owner = getSharedWith(f, rr);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}

	}

	/**
	 * method to iterate over a Repository folder and to read its owner.
	 * 
	 * @param f
	 *            File Server
	 * @return the owner of the REpository
	 * @throws IOException
	 */
	private String getOwner(File f) throws IOException {
		System.out.println("GET OWNER FOLDER");
		String str = null;

		System.out.println("f.getCanonicalPath(): "+f.getCanonicalPath());

		File repFolder = new File(f.getCanonicalPath() + File.separator);

		System.out.println("repFolder.isDirectory(): "+repFolder.isDirectory());

		if (repFolder.isDirectory()) {
			// list all its files
			for (String fileInFolder : repFolder.list()) {
				// get owner.txt and read it
				if (fileInFolder.equals(OWNER)) {
					str = readOwnerFile(repFolder.getCanonicalPath(), OWNER);
				}
			}
		}
		return str;
	}


	/**
	 * method to iterate over a Repository folder and to read its owner.
	 * 
	 * @param f
	 *            File Server
	 * @return the owner of the REpository
	 * @throws IOException
	 */
	private String getSharedWith(File f, RemoteRepository rr) throws IOException {
		System.out.println("GET SHARED WITH INFO");
		String str = null;

		System.out.println("f.getCanonicalPath(): "+f.getCanonicalPath());

		File repFolder = new File(f.getCanonicalPath() + File.separator);

		System.out.println("repFolder.isDirectory(): "+repFolder.isDirectory());

		if (repFolder.isDirectory()) {
			// list all its files
			for (String fileInFolder : repFolder.list()) {
				// get owner.txt and read it
				if (fileInFolder.equals(SHARED)) {
					iterateSharedWithFile(repFolder.getCanonicalPath(), SHARED, rr);
				}
			}
		}
		return str;
	}

	/*
	 * Ler o ficheiro shared.txt e criar a lista em memoria dos utilizadores com acesso ao repositorio
	 */
	private void iterateSharedWithFile(String repFolderName, String shared, RemoteRepository rr) {

		try (BufferedReader br = new BufferedReader(new FileReader(new File(repFolderName + File.separator + SHARED)))) {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				rr.getSharedUsers().add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String readOwnerFile(String repFolderName, String nameFile) {
		String str = null;

		try (BufferedReader br = new BufferedReader(new FileReader(new File(repFolderName + File.separator + OWNER)))) {
			str = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}


	/**
	 * Method to create a folder to keep all the repositories if does not exists
	 * it means the server is running for the first time and has no elements
	 * 
	 * @return
	 */
	private boolean buildFolder() {
		File serverFolder = new File(SERVER);
		boolean create = false;
		if (!serverFolder.exists()) {
			try {
				create = serverFolder.mkdir();
			} catch (Exception e) {
				System.err.println("THE FOLDER CAN NOT BE CREATED:: CHECK PERMISSIONS");
			}
			if (create) {
				System.out.println("THE SERVER IS RUNNING FOR THE FIRST TIME");
			}
		}
		return create;

	}

	public void listRepos() {
		System.out.println("mapRemRepos.size(): "+mapRemRepos.size());
		System.out.println("Available repositories:");
		mapRemRepos.forEach((key, value) -> {
			System.out.println("Key : " + key + " Value : " + value);
		});		
	}

	public boolean repoExists(String repoName) {
		return mapRemRepos.containsKey(repoName);	
	}


	public Map<String, RemoteRepository> getMapRemRepos() {
		return mapRemRepos;
	}

	public void setMapRemRepos(Map<String, RemoteRepository> mapRemRepos) {
		this.mapRemRepos = mapRemRepos;
	}

	public RemoteRepository getRemRepository(String repoFileName) {
		return mapRemRepos.get(repoFileName);
	}

	public RemoteRepository buildRepo(User localUser, String repoFileName) {

		// para alem de retornar um novo repositorio tb e necessessario
		// adiciona-lo ao catalogo de repositorios mapRemRepos

		RemoteRepository rr = new RemoteRepository(localUser.getName(), repoFileName);
		mapRemRepos.put(rr.getNameRepo(), rr);

		return rr;
	}

}
