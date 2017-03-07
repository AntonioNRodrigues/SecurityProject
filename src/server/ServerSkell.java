package server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
		if (authentication(msg)) {
			out.writeObject((Object)"THE USER IS AUTHENTICATED");
			if (msg instanceof MessageRS) {
				System.out.println(msg);
				TypeOperation op = ((MessageRS) msg).getTypeOperation();

				switch (op) {
				case REMOVE:
					// -remove <rep_name> <user_id>
					// TODO Falta verificar se � o owner que est� a aceder ao
					// reposit�rio. Porque s� ele pode adicionar users.
					System.out.println("-remove repo_name userID");
					RemoteRepository rrr = catRepo.getRemRepository(((MessageRS) msg).getRepoName());
					rrr.removeUserFromRepo(((MessageRS) msg).getUserId());
					System.out.println(rrr);
					break;
				case SHARE:
					// -share <rep_name> <user_id>
					// TODO Falta verificar se � o owner que est� a aceder ao
					// reposit�rio. Porque s� ele pode adicionar users.
					System.out.println("-share repo_name userID");
					RemoteRepository rrs = catRepo.getRemRepository(((MessageRS) msg).getRepoName());
					rrs.addUserToRepo(((MessageRS) msg).getUserId());
					System.out.println(rrs);
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
						
						break;
					case PUSH:
						// -push repo_name
						System.out.println("-PUSH REPO");
						RemoteRepository rr = catRepo.getRemRepository(mp.getRepoFileName());
						if (rr == null) {
							// repository does not exist
							rr = catRepo.buildRepo(mp.getLocalUser(), mp.getRepoFileName());
						}
						int sizeList = mp.getNumberFiles();
						
						
						break;
					default:
						break;
					}
					break;
				case FILE:
					switch (op) {
					case PULL:

						break;
					case PUSH:
						// -push file_name
						System.out.println("-PUSH FILE");
						RemoteRepository rr = catRepo.getRemRepository(mp.getRepoFileName());
						if (rr != null) {
							// the repo exists them proceed with push file
							try {
								File f = ReadWriteUtil.receiveFile(in, out);
								// do timestamps check
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
		}
		else{
			out.writeObject((Object)"YOU DOT NOT HAVE PREMISSIONS TO KEEP GOING PLEASE CHECK PASSWORD");
		}
	}

	private boolean authentication(Message msg) {
		User u = catUsers.getMapUsers().get(msg.getLocalUser().getName());
		// user does not exist, register user
		if (u == null) {
			catUsers.registerUser(msg.getLocalUser().getName(), msg.getPassword());
			return true;
		}
		// user exists check permissions
		if (u != null) {
			// user exists but does not have the password filled
			if (u.getPassword().equals(null)) {
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
