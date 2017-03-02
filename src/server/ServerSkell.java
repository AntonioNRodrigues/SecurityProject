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
		if (msg instanceof MessageRS) {
			System.out.println(msg);
			TypeOperation op = ((MessageRS) msg).getTypeOperation();

			switch (op) {
			case REMOVE:
				// -remove <rep_name> <user_id>
				//TODO Falta verificar se é o owner que está a aceder ao repositório. Porque só ele pode adicionar users.
				System.out.println("-remove repo_name userID");
				RemoteRepository rrr = catRepo.getRemRepository(((MessageRS) msg).getRepoName());
				rrr.removeUserFromRepo(((MessageRS) msg).getUserId());
				System.out.println(rrr);
				break;
			case SHARE:
				// -share <rep_name> <user_id>
				//TODO Falta verificar se é o owner que está a aceder ao repositório. Porque só ele pode adicionar users.
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
					System.out.println("-push repo_name");
					RemoteRepository rr = catRepo.getRemRepository(mp.getRepoFileName());
					int numberFiles = (Integer) mp.getNumberFiles();
					System.out.println(rr);

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
					System.out.println("-push file_name");
					RemoteRepository rr = catRepo.getRemRepository(mp.getRepoFileName());
					int numberFiles = (Integer) mp.getNumberFiles();
					
					try {
						File f = ReadWriteUtil.receiveFile(in, out);
						// do timestamps check
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					break;
				default:
					break;
				}
			default:
				break;
			}

		} else if (msg instanceof Message) {
			System.out.println(msg);
			User u = catUsers.getMapUsers().get(msg.getLocalUser().getName());
			// the user already exists check is the password is filled
			if (u != null && u.getPassword() == null) {
				System.out.println("the user is in the cat");
				// ask for password
				try {
					out.writeObject((Object) "Please fill your password");
					String password = (String) in.readObject();
					u.setPassword(password);
					// persite the user in the file users.txt
					catUsers.persisteUser(u.getName(), password);
				} catch (IOException | ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// there is no user with that name, register the user
			if (u == null) {
				System.out.println("The user is not in catalog");
				catUsers.registerUser(msg.getLocalUser().getName(), msg.getPassword());
			}

		}

	}

}
