package server.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RepositoryCatalog {
	private Map<String, RemoteRepository> mapRemRepos;
	private static final String SERVER = "SERVER";
	private static final String OWNER = "owner.txt";

	public RepositoryCatalog() {
		super();
		buildMap();
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
			File f = new File(SERVER + "/" + strFolder);
			if (f.isDirectory()) {
				// build a repository
				rr = new RemoteRepository(f.lastModified(), f.getName());
				rr.addFilesToRepo(rr.getNameRepo(), Arrays.asList(f.listFiles()));
				// inside each folder/repositoriy exists a owner.txt file
				String owner = null;
				try {
					owner = getOwnerFolder(f);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (owner != null) {
					rr.setOnwer(owner);
				}
				mapRemRepos.put(rr.getNameRepo(), rr);
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
	private String getOwnerFolder(File f) throws IOException {
		BufferedReader br = null;
		String str = null;

		File repFolder = new File(f.getCanonicalPath() + "/");

		if (repFolder.isDirectory()) {
			// list all its files
			for (String s : repFolder.list()) {
				// get owner.txt and red it
				if (s.equals(OWNER)) {
					File g = new File(repFolder.getCanonicalPath() + "/" + OWNER);
					try {
						br = new BufferedReader(new FileReader(g));
						str = br.readLine();
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
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

	public Map<String, RemoteRepository> getMapRemRepos() {
		return mapRemRepos;
	}

	public void setMapRemRepos(Map<String, RemoteRepository> mapRemRepos) {
		this.mapRemRepos = mapRemRepos;
	}

	public RemoteRepository getRemRepository(String repoFileName) {
		return mapRemRepos.get(repoFileName);
	}
	
	
}
