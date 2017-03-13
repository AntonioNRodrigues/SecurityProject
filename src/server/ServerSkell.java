package server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.Set;

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

	public ServerSkell() {
		this.catRepo = new RepositoryCatalog();
		this.catUsers = new UserCatalog();
	}

	public ServerSkell(ObjectOutputStream out, ObjectInputStream in) {
		this.out = out;
		this.in = in;
		this.catRepo = new RepositoryCatalog();
		this.catUsers = new UserCatalog();
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
		RemoteRepository rr = null;
		if (authentication(msg)) {
			// out.writeObject((Object) "THE USER IS AUTHENTICATED");
			if (msg instanceof MessageRS) {
				System.out.println(msg);
				MessageRS mrs = (MessageRS) msg;
				TypeOperation op = mrs.getTypeOperation();
				switch (op) {
				case REMOVE:
					System.out.println("-REMOVE REPO NAME USERID");
					rr = catRepo.getRemRepository(mrs.getRepoName());
					if (mrs.getLocalUser().getName().equals(rr.getOwner())) {
						rr.removeUserFromRepo(mrs.getUserId());
					}
					System.out.println(rr);
					break;
				case SHARE:
					System.out.println("-SHARE REPO NAME USERID");
					rr = catRepo.getRemRepository(mrs.getRepoName());
					if (mrs.getLocalUser().getName().equals(rr.getOwner())) {
						rr.addUserToRepo(mrs.getUserId());
					}
					System.out.println(rr);
					break;
				default:
					break;
				}

			} else if (msg instanceof MessageP) {
				System.out.println(msg);
				MessageP mp = ((MessageP) msg);
				TypeSend ts = mp.getTypeSend();
				TypeOperation op = mp.getOperation();

				switch (ts) {
				case REPOSITORY:
					switch (op) {
					case PULL:
						System.out.println("-PULL REPOSITORY");
						rr = catRepo.getRemRepository(mp.getRepoName());
						out.writeObject((Object) rr.sizeUniqueFilesInMap());
						Set<File> set = rr.getListMostRecentFiles();

						/*
						 * TO DO iterate over the set and send each file check a
						 * better place to do so i dont think this works inside
						 * this method WAITING FOR CLIENT TO DEAL WITH IT
						 */
						for (File f : set) {
							ReadWriteUtil.sendFile(f.getName(), in, out);
						}

						break;
					case PUSH:
						// -push repo_name
						System.out.println("-PUSH REPOSITORY");
						System.out.println(mp.getRepoName());
						rr = catRepo.getRemRepository(mp.getRepoName());
						System.out.println(rr == null);
						if (rr == null) {
							// repository does not exist
							rr = catRepo.buildRepo(mp.getLocalUser(), mp.getRepoName());
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
						if (rr != null) {
							File inRepo = rr.getMostRecentFile(mp.getFileName());
							// client does not have the recent file so send it
							if (lastModifiedDate < inRepo.lastModified()) {
								try {
									ReadWriteUtil.sendFile(mp.getFileName(), in, out);
								} catch (IOException e) {
									e.printStackTrace();
								}
							} else {
								out.writeObject((Object) "THE SERVER HAS NOT A RECENT VERSION FOR U");
							}
						}
						break;
					case PUSH:
						// -push file_name
						System.out.println("-PUSH FILE");
						rr = catRepo.getRemRepository(mp.getRepoName());
						if (rr != null) {
							// the repo exists them proceed with push file
							try {
								File received = ReadWriteUtil.receiveFile(in, out);
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
							out.writeObject((Object) "THE REPOSITORY DOES NOT EXIST");
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
			out.writeObject((Object) "YOU DOT NOT HAVE PREMISSIONS TO KEEP GOING PLEASE CHECK PASSWORD");
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
