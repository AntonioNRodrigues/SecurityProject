package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MessageAHandler extends MessageHandler {

	public MessageAHandler() {
		super("MessageAHandler");
		// TODO Auto-generated constructor stub
	}

	@Override
	public String sendMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {
		// TODO Auto-generated method stub
		
		sendAuthMessage(in, out, params);

		return "MessageAHandler:sendMessage:"+params.getLocalUser()+" "+params.getServerAddress()+" "+(params.getPassword()==null?"":"-p "+params.getPassword());
	}

}
