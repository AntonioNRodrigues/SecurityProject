package server.repository;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import files.Ficheiro;

public class RepositoryCatalog {
	private Map<String, Set<Ficheiro>> mapFiles;
	private static final String SERVER = "SERVER";

	public RepositoryCatalog() {
		super();
		buildMap();
	}

	private void buildMap() {
		this.setMapFiles(new ConcurrentHashMap<>());
		// if buildFolder == true do nothing
		// there is no need to populate the map with repositories
		// else going to read the folder and populate the map
		if (!buildFolder()) {
			// readFolder()
		}

	}

	/**
	 * Method to create a folder to keep all the repositories if does not exists
	 * it means the server is running for the fisrt time and has no elements
	 * 
	 * @return
	 */
	private boolean buildFolder() {
		File f = new File(SERVER);
		boolean create = false;
		if (!f.exists()) {
			try {
				create = f.mkdir();
			} catch (Exception e) {
				System.err.println("The folder can not be created");
			}

			if (create) {
				System.out.println("THE SERVER IS RUNNING FOR THE FIRST TIME");
			}

		}
		return create;

	}

	public Map<String, Set<Ficheiro>> getMapFiles() {
		return mapFiles;
	}

	public void setMapFiles(Map<String, Set<Ficheiro>> mapFiles) {
		this.mapFiles = mapFiles;
	}

}
