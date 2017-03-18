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
		
		//sendAuthMessage(in, out, params);
		
		if (params.getOperation().contentEquals("SHARE"))
			sendShareMessage(in, out, params);
		else if (params.getOperation().contentEquals("REMOVE"))		
			sendRemoveMessage(in, out, params);
		else{
			System.err.println("O tipo do comando não corresponde a um comando válido.");
			System.exit(-1);
		}
		
		return "MessageRSHHandler:sendMessage:"+params.getLocalUser()+" "+params.getServerAddress()+" "+(params.getPassword()==null?"":"-p "+params.getPassword())+" -"+params.getOperation()+" "+(params.getRepOrFileName() == null? (params.getFileName()==null?params.getRepName():params.getFileName()) :params.getRepOrFileName())+(params.getUserId()==null?" ":" " + params.getUserId());
	}

	public String sendShareMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {

		MessageRS mrs = new MessageRS(new User(params.getLocalUser(), params.getPassword()), params.getServerAddress(), params.getPassword(),
				params.getRepName(), params.getUserId(), TypeOperation.SHARE);
		try {
			out.writeObject((Object)mrs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		String result="";
		try {
			result = (String) in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (result.contentEquals("OK"))
			// receive the files
			System.out.println("O repositório "+params.getRepName()+" foi partilhado com o utilizador "+params.getUserId());
		else if (result.contentEquals("NOK")) {
			String error="";
			try {
				error = (String) in.readObject();
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(error);
		}
		else 
			System.out.println("Something happened...");
			


		return "MessageRSHandler:sendShareMessage";
	}


	public String sendRemoveMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {

		MessageRS mrs = new MessageRS(new User(params.getLocalUser(), params.getPassword()), params.getServerAddress(), params.getPassword(),
				params.getRepName(), params.getUserId(), TypeOperation.REMOVE);
		try {
			out.writeObject((Object)mrs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		String result="";
		try {
			result = (String) in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (result.contentEquals("OK"))
			// receive the files
			System.out.println("O repositório "+params.getRepName()+" deixou de ser partilhado com o utilizador "+params.getUserId());
		else if (result.contentEquals("NOK")) {
			String error="";
			try {
				error = (String) in.readObject();
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(error);
		}
		else 
			System.out.println("Something happened...");		

		return "MessageRSHandler:sendRemoveMessage";	
	}

}
