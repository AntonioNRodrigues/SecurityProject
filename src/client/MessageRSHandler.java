package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MessageRSHandler extends MessageHandler {

	public MessageRSHandler() {
		// TODO Auto-generated constructor stub
		super("MessageRSHandler");
	}	

	@Override
	public String sendMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient2 params) {
		// TODO Auto-generated method stub
		return "MessageRSHHandler:sendMessage:"+params.getLocalUser()+" "+params.getServerAddress()+" "+(params.getPassword()==null?"":"-p "+params.getPassword())+" -"+params.getOperation()+" "+params.getRepOrFileName();
	}

}
