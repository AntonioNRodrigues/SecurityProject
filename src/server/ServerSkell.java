package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import enums.TypeOperation;
import enums.TypeSend;
import message.Message;
import message.MessageP;
import message.MessageRS;
import server.repository.RepositoryCatalog;
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
		}

	}

}
