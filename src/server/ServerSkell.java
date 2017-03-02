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

	public ServerSkell(ObjectOutputStream out, ObjectInputStream in) {
		this.out = out;
		this.in = in;
		this.catRepo = new RepositoryCatalog();
		this.catUsers = new UserCatalog();
	}

	public void receiveMsg(Message msg) {
		if (msg instanceof MessageRS) {
			TypeOperation op = ((MessageRS) msg).getTypeOperation();

			switch (op) {
			case REMOVE:

				break;
			case SHARE:

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
					System.out.println(numberFiles);
					System.out.println(rr);
					while (numberFiles != 0) {
						try {
							File f = ReadWriteUtil.receiveFile(in, out);
							System.out.println(f.getName());
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						numberFiles--;
					}
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
						//do timestamps check
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
				catUsers.registerUser(msg.getLocalUser().getName(), msg.getPassword());
			}

		}

	}

}
