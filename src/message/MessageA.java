package message;

import user.User;

public class MessageA extends Message {

	private static final long serialVersionUID = 1L;

	public MessageA(User localUser, String serverAdress, String password) {
		super(localUser, serverAdress, password);
	}
}
