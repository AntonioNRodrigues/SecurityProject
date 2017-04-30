package server.repository;

import static utilities.ReadWriteUtil.OWNER;
import static utilities.ReadWriteUtil.SERVER;
import static utilities.ReadWriteUtil.SHARED;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;

import user.User;
import utilities.SecurityUtil;
import utilities.SecurityUtil2;

public class RepositoryCatalog {

	private Map<String, RemoteRepository> mapRemRepos;

	public RepositoryCatalog()
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		super();
		buildMap();
		listRepos();
	}

	private void buildMap() throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {

		this.mapRemRepos = new ConcurrentHashMap<String, RemoteRepository>();
		// if buildFolder == true do nothing
		// there is no need to populate the map with repositories
		// else going to read the folder and populate the map
		if (!buildFolder()) {
			readFolder();
		}
	}

	/**
	 * method to exclude the file with termination like .sig and .server
	 * 
	 * @param f
	 *            file to check
	 * @return true if want to exclude false otherwise
	 */
	private boolean excludedFiles(File f) {
		String[] a = f.getName().split("\\.(?=[^\\.]+$)");
		if (a[a.length - 1].equals("sig") || (a[a.length - 1].equals("server")) || (a[a.length - 1].equals("hmac"))) {
			return true;
		}
		return false;
	}

	/**
	 * method to read the SERVER folder, build the object representation of each
	 * Repository and populate the mapFiles with each Repository and its files.
	 * 
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	private void readFolder() throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {

		RemoteRepository rr = null;
		// list of repos inside Server
		for (String repoFolder : new File(SERVER).list()) {
			// repositories folders

			File folder = new File(SERVER + File.separator + repoFolder);
			if (folder.isDirectory()) {
				// build a repository
				rr = new RemoteRepository(folder.getName());

				for (File file : folder.listFiles()) {
					if (!excludedFiles(file)) {
						String[] a = file.getName().split(" ");
						String nameWithoutTimestamp = a[0];
						rr.addFile(nameWithoutTimestamp, file);
					}
				}

				// inside each folder/repository exists a owner.txt file
				String owner = null;
				try {
					owner = getOwner(folder);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (owner != null) {
					rr.setOwner(owner);
				}
				mapRemRepos.put(rr.getNameRepo(), rr);

				// inside each folder/repository may exists a shared.txt file
				try {
					owner = getSharedWith(folder, rr);
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
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	private String getOwner(File f)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {

		String str = null;
		File repFolder = new File(f.getCanonicalPath() + File.separator);

		if (repFolder.isDirectory()) {
			// list all its files
			// for (String fileInFolder : repFolder.list()) {
			// get owner.txt and read it
			// if (fileInFolder.equals(OWNER)) {
			str = readOwnerFile(repFolder.getCanonicalPath(), OWNER);
			// }
			// }
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
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	private String getSharedWith(File f, RemoteRepository rr)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		String str = null;
		File repFolder = new File(f.getCanonicalPath() + File.separator);
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
	 * Ler o ficheiro shared.txt e criar a lista em memoria dos utilizadores com
	 * acesso ao repositorio
	 */
	private void iterateSharedWithFile(String repFolderName, String shared, RemoteRepository rr)
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException {

		System.out.println("load repo shared with info from encrypted repo shared with file");
		Path file = Paths.get(SERVER + File.separator + repFolderName + File.separator + SHARED);
		Path hmacFile = Paths.get(SERVER + File.separator + repFolderName + File.separator + "." + SHARED + ".hmac");

		if (Files.exists(file)) {

			if (Files.exists(hmacFile)) {

				// decipher file and read content
				SecretKey sk = SecurityUtil.getKeyFromServer();

				if (SecurityUtil2.checkFileIntegrity(file, hmacFile, sk)) {

					try {

						byte[] b = SecurityUtil2.decipherFile2Memory(file, sk);
						String content = new String(b);
						String[] array = content.split("\n");
						for (String s : array) {
							rr.getSharedUsers().add(s);
						}
					} catch (IOException | InvalidKeyException e) {
						e.printStackTrace();
					} catch (InvalidAlgorithmParameterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					System.out.println("incorrect hmac users file");
				}
			} else {
				System.out.println("hmac users file doesnt exist");
			}
		} else
			System.out.println("users file doesnt exist");

	}

	private String readOwnerFile(String repFolderName, String nameFile)
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException {

		String str = null;
		System.out.println("get repo owner from encrypted owner file");
		Path file = Paths.get(SERVER + File.separator + repFolderName + File.separator + OWNER);
		Path hmacFile = Paths.get(SERVER + File.separator + repFolderName + File.separator + "." + OWNER + ".hmac");

		if (Files.exists(file)) {

			if (Files.exists(hmacFile)) {

				// decipher file and read content
				SecretKey sk = SecurityUtil.getKeyFromServer();

				if (SecurityUtil2.checkFileIntegrity(file, hmacFile, sk)) {
					try {

						byte[] b = SecurityUtil2.decipherFile2Memory(file, sk);
						str = new String(b);

					} catch (IOException | InvalidKeyException e) {
						e.printStackTrace();
					} catch (InvalidAlgorithmParameterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					System.out.println("incorrect hmac repo owner file");
					System.out.println("server process halted");
					System.exit(0);
				}
			} else {
				System.out.println("hmac repo owner doesnt exist");
				System.out.println("server process halted");
				System.exit(0);
			}
		} else
			System.out.println("repo owner file doesnt exist");

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
		System.out.println("mapRemRepos.size(): " + mapRemRepos.size());
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
		System.out.println(rr);
		mapRemRepos.put(rr.getNameRepo(), rr);

		return rr;
	}

}
