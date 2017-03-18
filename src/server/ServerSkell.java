package server;
import static utilities.ReadWriteUtil.SERVER;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.List;

import enums.TypeOperation;
import enums.TypeSend;
import message.Message;
import message.MessageP;
import message.MessageRS;
import server.repository.RemoteRepository;
import server.repository.RepositoryCatalog;
import user.User;
import user.UserCatalog;
import utilities.ReadWriteUtil;

public class ServerSkell {
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private RepositoryCatalog catRepo;
	private UserCatalog catUsers;

	public ServerSkell(MyGitServer my) {
		this.catRepo = my.getCatRepo();
		this.catUsers = my.getCatUsers();
	}

	public ServerSkell() {
		// this.catRepo = new RepositoryCatalog();
		// this.catUsers = new UserCatalog();
	}

	public ServerSkell(ObjectOutputStream out, ObjectInputStream in) {
		this.out = out;
		this.in = in;
		// this.catRepo = new RepositoryCatalog();
		// this.catUsers = new UserCatalog();
	}

	public ObjectOutputStream getOut() {
		return out;
	}

	public void setOut(ObjectOutputStream out) {
		this.out = out;
	}

	public ObjectInputStream getIn() {
		return in;
	}

	public void setIn(ObjectInputStream in) {
		this.in = in;
	}

	public void receiveMsg(Message msg) throws ClassNotFoundException, IOException {

		System.out.println("catRepo.listRepos(): ");
		catRepo.listRepos();

		RemoteRepository rr = null;
		if (authentication(msg)) {
			// out.writeObject((Object) "THE USER IS AUTHENTICATED");
			System.out.println("THE USER IS AUTHENTICATED");

			if (msg instanceof MessageRS) {
				MessageRS mrs = (MessageRS) msg;
				System.out.println(mrs);
				TypeOperation op = mrs.getTypeOperation();
				rr = catRepo.getRemRepository(mrs.getRepoName());
				if (rr == null) {
					System.out.printf("THE REPOSITORY WITH THAT NAME %s DOES NOT EXIST\n", mrs.getRepoName());
				} else {
					if (mrs.getLocalUser().getName().equals(rr.getOwner())) {
						switch (op) {
						case REMOVE:
							System.out.println("-REMOVE REPOSITORY");
							rr.removeUserFromRepo(mrs.getUserId());
							System.out.printf("THE USER %s just stoped to have acces THE REPOSITORY %s",
									mrs.getUserId(), mrs.getRepoName());
							break;
						case SHARE:
							System.out.println("-SHARE REPOSITORY");
							rr.addShareUserToRepo(mrs.getUserId());
							System.out.printf("THE REPO %s IS SHARED WITH %s", mrs.getRepoName(),
									mrs.getUserId());
							break;
						default:
							break;
						}
					} else {
						System.out.printf("THE USER %s DOES NOT HAVE PERMISSIONS TO ADD OR REMOVE TO THE REPOSITORY %s",
								mrs.getLocalUser().getName(), mrs.getRepoName());
					}
				}

			} else if (msg instanceof MessageP) {

				System.out.println(msg);
				MessageP mp = ((MessageP) msg);

				System.out.println(mp);
				TypeSend ts = mp.getTypeSend();
				TypeOperation op = mp.getOperation();

				switch (ts) {
				case REPOSITORY:
					switch (op) {
					case PULL:

						System.out.println("ServerSkell: mp.getNumberFiles() :" + mp.getNumberFiles());
						System.out.println("-PULL REPOSITORY");
						System.out.println(mp.getRepoName());
						rr = catRepo.getRemRepository(mp.getRepoName());
						System.out.println(rr == null);

						if (catRepo.repoExists(mp.getRepoName())) {

							// Enviar numero de ficheiros
							// System.out.println("rr.sizeUniqueFilesInMap()):
							// "+rr.sizeUniqueFilesInMap());
							// out.writeObject((Integer)
							// rr.sizeUniqueFilesInMap());
							// Set<File> set = rr.getListMostRecentFiles();

							/*
							 * TO DO iterate over the set and send each file
							 * check a better place to do so i dont think this
							 * works inside this method WAITING FOR CLIENT TO
							 * DEAL WITH IT
							 */

							// Enviar ficheiros
							// for (File f : set) {
							// ReadWriteUtil.sendFile("SERVER"+File.separator+mp.getRepoName()+File.separator+f.getName(),
							// in, out);
							// }

							// Enviar numero de ficheiros
							// System.out.println("rr.sizeUniqueFilesInMap()):
							// "+rr.sizeUniqueFilesInMap());
							// out.writeObject((Integer)
							// rr.sizeUniqueFilesInMap());
							// Set<File> set = rr.getListMostRecentFiles();

							/*
							 * TO DO iterate over the set and send each file
							 * check a better place to do so i dont think this
							 * works inside this method WAITING FOR CLIENT TO
							 * DEAL WITH IT
							 */

							// Enviar ficheiros
							// for (File f : set) {
							// ReadWriteUtil.sendFile("SERVER"+File.separator+mp.getRepoName()+File.separator+f.getName(),
							// in, out);
							// }

							// trazer todos os ficheiros!
							List<File> filesList = rr.getFiles(mp.getRepoName());
							if (filesList.size() > 0) {
								try {

									// Enviar o numero de ficheiros
									out.writeObject((Integer) filesList.size());

									for (File f : filesList)
										ReadWriteUtil.sendFile(SERVER + File.separator + mp.getRepoName()
												+ File.separator + f.getName(), in, out);

								} catch (IOException e) {
									e.printStackTrace();
								}
							}

						} else {
							System.out.println("THE REPOSITORY DOES NOT EXIST!!!");
						}
						break;
					case PUSH:
						// -push repo_name
						System.out.println("ServerSkell: mp.getNumberFiles() :" + mp.getNumberFiles());
						System.out.println("-PUSH REPOSITORY");
						System.out.println(mp.getRepoName());
						rr = catRepo.getRemRepository(mp.getRepoName());
						System.out.println(rr == null);

						if (rr == null) {
							// repository does not exist
							rr = catRepo.buildRepo(mp.getLocalUser(), mp.getRepoName());

							System.out.println("repository created");

							/////////
							int sizeList = mp.getNumberFiles();
							for (int i = 0; i < sizeList; i++) {
								try {

									String path = SERVER + File.separator + mp.getRepoName() + File.separator;
									File received = ReadWriteUtil.receiveFile(path, in, out);

									// COMPARAR TIMESTAMPS
									// if(received.lastModified()...)

									// Guardar ficheiros caso seja necessário
									// (persistir)

								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								}
							}
							/////////

						}
						break;
					default:
						break;
					}
					break;
				case FILE:
					switch (op) {
					case PULL:
						System.out.println("-PULL FILE");
						rr = catRepo.getRemRepository(mp.getRepoName());
						long lastModifiedDate = mp.getTimestamp();
						if (catRepo.repoExists(mp.getRepoName())) {

							File inRepo = rr.getFile(mp.getRepoName(), mp.getFileName());
							// client does not have the recent file so send it
							if (lastModifiedDate < inRepo.lastModified()) {
								try {

									// Enviar o numero de ficheiros
									out.writeObject((Integer) 1);
									ReadWriteUtil.sendFile(SERVER + File.separator + mp.getRepoName() + File.separator
											+ mp.getFileName(), in, out);
								} catch (IOException e) {
									e.printStackTrace();
								}
							} else {
								out.writeObject((Object) "THE SERVER HAS NOT A RECENT VERSION FOR U");
							}
						} else {
							System.out.println("THE REPOSITORY DOES NOT EXIST!!!");
						}
						break;
					case PUSH:
						// -push file_name
						System.out.println("ServerSkell: mp.getNumberFiles() :" + mp.getNumberFiles());
						System.out.println("-PUSH FILE");
						System.out.println("mp.getRepoName(): " + mp.getRepoName());
						System.out.println("mp.getFileName(): " + mp.getFileName());

						rr = catRepo.getRemRepository(mp.getRepoName());
						System.out.println(rr == null);

						// if (rr == null) {
						// rr = catRepo.buildRepo(mp.getLocalUser(),
						// mp.getRepoName());

						rr = catRepo.getRemRepository(mp.getRepoName());
						if (rr != null) {

							// the repo exists them proceed with push file
							try {

								String path = SERVER + File.separator + mp.getRepoName() + File.separator;
								File received = ReadWriteUtil.receiveFile(path, in, out);
								received.setLastModified(mp.getTimestamp());
								System.out.println(received.getName() + received.lastModified());
								File inRepo = rr.getMostRecentFile(received.getName());
								System.out.println(inRepo.getName() + inRepo.lastModified());
								// if received file has lastmodified > than the
								// one that exists in the repo
								if (received.lastModified() > inRepo.lastModified()) {
									// added to the list
									rr.getVersionList(received.getName()).add(received);
								} else {
									// delete file the repo has a recent file
									Files.deleteIfExists(received.toPath());
								}
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							// out.writeObject((Object) "THE REPOSITORY DOES NOT
							// EXIST");
							System.out.println("THE REPOSITORY DOES NOT EXIST!!!");

						}

						break;
					default:
						break;
					}
				default:
					break;
				}
			}
		} else {
			// out.writeObject((Object) "YOU DOT NOT HAVE PREMISSIONS TO KEEP
			// GOING PLEASE CHECK PASSWORD");
			System.out.println("YOU DOT NOT HAVE PREMISSIONS TO KEEP GOING PLEASE CHECK PASSWORD");
		}
	}

	private boolean authentication(Message msg) {
		User u = catUsers.getMapUsers().get(msg.getLocalUser().getName());
		// user does not exist, register user
		if (u == null) {
			System.out.println("THE USER WAS NOT FOUND:: REGISTERING USER");
			catUsers.registerUser(msg.getLocalUser().getName(), msg.getPassword());

			return true;
		}
		// user exists check permissions
		if (u != null) {
			// user exists but does not have the password filled
			if (u.getPassword().equals("")) {
				try {
					out.writeObject((Object) "Please fill your password");
					String password = (String) in.readObject();
					// password did not come
					if (password == null) {
						return false;
					}
					u.setPassword(password);
					// persist the user in the file users.txt
					catUsers.persisteUser(u.getName(), password);
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			// user has password filled and its the same
			if (u.getPassword().equals(msg.getLocalUser().getPassword())) {
				return true;
			}
		}
		return false;
	}

}
