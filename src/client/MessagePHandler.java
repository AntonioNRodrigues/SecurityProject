package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.attribute.BasicFileAttributes;
import enums.TypeOperation;
import enums.TypeSend;
import message.MessageP;
import user.User;

public class MessagePHandler extends MessageHandler {

	public MessagePHandler() {
		// TODO Auto-generated constructor stub
		super("MessagePHandler");
	}	

	@Override
	public String sendMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {
		// TODO Auto-generated method stub	
	
		sendAuthMessage(in, out, params);
	
		if (params.getTypeSend().contentEquals("FILE"))
			sendPushFileMessage(in, out, params);
		else if (params.getTypeSend().contentEquals("REPOSITORY"))
			sendPushRepMessage(in, out, params);
		else {
			// TODO: tratar do error handling!!!
			System.out.println("ERRO: param invalido");
		}

		return "MessagePHandler:sendMessage:"+params.getLocalUser()+" "+params.getServerAddress()+" "+(params.getPassword()==null?"":"-p "+params.getPassword())+" -"+params.getOperation()+" "+params.getRepOrFileName();
	}
			
			
	public String sendPushFileMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {
				
		//o tipo do timestamp é FileTime que é timezone independent!
		//LocalDateTime timestamp = LocalDateTime.now();
	
        BasicFileAttributes attributes = getFileAttributes(params.getFile());

		// Message to use when we want to send or receive a file. Used in PULL fileName and PUSH fileName
		//serverAddress não será necessário, já está presente na criação do socket...
		MessageP mp = new MessageP(new User(params.getLocalUser()), params.getServerAddress(), params.getPassword(), TypeSend.FILE, params.getRepName(),
				params.getFileName(), TypeOperation.PUSH, 1,  attributes.lastModifiedTime().toMillis());
		
		try {
			out.writeObject((Object)mp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		try {
			out.writeObject((Object)mp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		
		return "MessagePHandler:sendPushFileMessage";
	}

	public String sendPushRepMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {
		
        // Message to use when we want to send or receive a file. Used in PULL fileName and PUSH fileName
		//serverAddress não será necessário, já está presente na criação do socket...
		MessageP mp = new MessageP(new User(params.getLocalUser()), params.getServerAddress(), params.getPassword(), TypeSend.REPOSITORY, params.getRepName(),
				TypeOperation.PUSH, 1,  0);
		
		try {
			out.writeObject((Object)mp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		
		/*Message m = new Message(new User("n", "p"), HOST + PORT, "p");
		File folder = new File("CLIENT/REP01");
		File[] listFolder = folder.listFiles();

		MessageP mp = new MessageP(new User("n"), HOST + PORT, "p", TypeSend.REPOSITORY, folder.getAbsolutePath(),
				TypeOperation.PUSH, listFolder.length, folder.lastModified());

		MessageP mp2 = new MessageP(new User("n"), HOST + PORT, "P", TypeSend.REPOSITORY, "REP01", TypeOperation.PUSH, 1, 0);
		MessageP mp4 = new MessageP(new User("n"), HOST + PORT, "P", TypeSend.FILE, "REP01", TypeOperation.PUSH, 1, 0);
		MessageP mp1 = new MessageP(new User("n", "p"), HOST + PORT, "P", TypeSend.FILE, "REP01", TypeOperation.PUSH, 1, 0);
		*/
		
		return "MessagePHandler:sendPushRepMessage";
	}
	
	
	public String sendPullFileMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {
		
        BasicFileAttributes attributes = getFileAttributes(params.getFile());

		// Message to use when we want to send or receive a file. Used in PULL fileName and PUSH fileName
		//serverAddress não será necessário, já está presente na criação do socket...
		MessageP mp = new MessageP(new User(params.getLocalUser()), params.getServerAddress(), params.getPassword(), TypeSend.FILE, params.getRepName(),
				params.getFileName(), TypeOperation.PULL, 1,  attributes.lastModifiedTime().toMillis());
		
		try {
			out.writeObject((Object)mp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		try {
			out.writeObject((Object)mp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		
		return "MessagePHandler:sendPullFileMessage";
	}

	
	public String sendPullRepMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {
		
        // Message to use when we want to send or receive a file. Used in PULL fileName and PUSH fileName
		//serverAddress não será necessário, já está presente na criação do socket...
		MessageP mp = new MessageP(new User(params.getLocalUser()), params.getServerAddress(), params.getPassword(), TypeSend.REPOSITORY, params.getRepName(),
				TypeOperation.PULL, 1,  0);
		
		try {
			out.writeObject((Object)mp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		try {
			out.writeObject((Object)mp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "MessagePHandler:sendPullRepMessage";
	}



}
