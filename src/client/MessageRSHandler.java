package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import enums.TypeOperation;
import message.MessageRS;
import user.User;

public class MessageRSHandler extends MessageHandler {

	public MessageRSHandler() {
		// TODO Auto-generated constructor stub
		super("MessageRSHandler");
	}	

	@Override
	public String sendMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {
		
		sendAuthMessage(in, out, params);
		
		sendShareMessage(in, out, params);
		
		return "MessageRSHHandler:sendMessage:"+params.getLocalUser()+" "+params.getServerAddress()+" "+(params.getPassword()==null?"":"-p "+params.getPassword())+" -"+params.getOperation()+" "+(params.getRepOrFileName() == null? (params.getFileName()==null?params.getRepName():params.getFileName()) :params.getRepOrFileName())+(params.getUserId()==null?" ":" " + params.getUserId());
	}

	public String sendShareMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {

		MessageRS mrs = new MessageRS(new User(params.getLocalUser()), params.getServerAddress(), params.getPassword(),
				params.getRepName(), params.getUserId(), TypeOperation.SHARE);
		try {
			out.writeObject((Object)mrs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		try {
			out.writeObject((Object)mrs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				


		return "MessageRSHandler:sendShareMessage";
	}


	public String sendRemoveMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {

		MessageRS mrs = new MessageRS(new User(params.getLocalUser()), params.getServerAddress(), params.getPassword(),
				params.getRepName(), params.getUserId(), TypeOperation.REMOVE);
		try {
			out.writeObject((Object)mrs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		try {
			out.writeObject((Object)mrs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				

		return "MessageRSHandler:sendRemoveMessage";	
	}

}
