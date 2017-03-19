package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MessageAHandler extends MessageHandler {

	public MessageAHandler() {
		super("MessageAHandler");
	}

	@Override
	public String sendMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {
		sendAuthMessage(in, out, params);

		return "MessageAHandler:sendMessage:" + params.getLocalUser() + " " + params.getServerAddress() + " "
				+ (params.getPassword() == null ? "" : "-p " + params.getPassword());
	}

}
