package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import enums.TypeOperation;
import enums.TypeSend;
import message.Message;
import message.MessageP;
import message.MessageRS;
import server.repository.RepositoryCatalog;
import user.User;
import user.UserCatalog;

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
			System.out.println(msg);
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
				//ask for password
				try {
					out.writeObject((Object)"Please fill your password");
					String password = (String) in.readObject();
					u.setPassword(password);
					//persite the user in the file users.txt 
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
