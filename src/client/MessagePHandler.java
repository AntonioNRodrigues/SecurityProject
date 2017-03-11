package client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;

import enums.TypeOperation;
import enums.TypeSend;
import message.Message;
import message.MessageP;
import user.User;

public class MessagePHandler extends MessageHandler {

	public MessagePHandler() {
		// TODO Auto-generated constructor stub
		super("MessagePHandler");
	}	

	@Override
	public String sendMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient2 params) {
		// TODO Auto-generated method stub
	
	
		//host e port não serão necessários, já estão presentes na criação do socket...	

		// Mensagem de autenticação
		Message m = new Message(new User(params.getLocalUser(), params.getPassword()), params.getHost() + params.getPort(), params.getPassword());

		try {
			out.writeObject((Object)m);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		//LocalDateTime time = LocalDateTime.now();
		
        File file = new File(params.getRepOrFileName());
        Path filePath = file.toPath();
        BasicFileAttributes attributes = null;
        try
        {
            attributes =
                    Files.readAttributes(filePath, BasicFileAttributes.class);
        }
        catch (IOException exception)
        {
            System.out.println("Exception handled when trying to get file " +
                    "attributes: " + exception.getMessage());
        }
		System.out.println("creationTime: " + attributes.creationTime());
		System.out.println("lastAccessTime: " + attributes.lastAccessTime());
		System.out.println("lastModifiedTime: " + attributes.lastModifiedTime());
		
		
		//serverAddress não será necessário, já está presente na criação do socket...

		// Message to use when we want to send or receive a file. Used in PULL fileName and PUSH fileName
		
		//o tipo do timestamp é FileTime que é timezone independent!
	
		MessageP mp = new MessageP(new User(params.getLocalUser()), params.getServerAddress(), params.getPassword(), TypeSend.FILE, "REP01",
				params.getRepOrFileName(), TypeOperation.PUSH, 1, attributes.lastModifiedTime().toMillis());			

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

		
		
		

		/**
		 * Message to use when we want to send or receive a repository. Used in PULL repName and PUSH repName
		 * 
		 *
		public MessageP(User localUser, String serverAdress, String password, TypeSend typeSend, String repoName,
				TypeOperation operation, int numberFiles, long ts) {
			super(localUser, serverAdress, password);
			this.repoName = repoName;
			this.fileName = null;
			this.operation = operation;
			this.typeSend = typeSend;
			this.setNumberFiles(numberFiles);
			this.timestamp = ts;
		}*/

			
			
		return "MessagePHandler:sendMessage:"+params.getLocalUser()+" "+params.getServerAddress()+" "+(params.getPassword()==null?"":"-p "+params.getPassword())+" -"+params.getOperation()+" "+params.getRepOrFileName();
	}

}
