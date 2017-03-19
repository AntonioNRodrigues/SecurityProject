package message;

import user.User;

public class MessageA extends Message {

	public MessageA(User localUser, String serverAdress, String password) {
		super(localUser, serverAdress, password);
	}
}
