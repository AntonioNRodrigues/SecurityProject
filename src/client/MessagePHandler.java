package client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import enums.TypeOperation;
import enums.TypeSend;
import message.MessageP;
import user.User;
import utilities.ReadWriteUtil;

public class MessagePHandler extends MessageHandler {

	private List<Path> filesList;

	public MessagePHandler() {
		super("MessagePHandler");
		this.filesList = new ArrayList<>();
	}	

	@Override
	public String sendMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {
	
		//sendAuthMessage(in, out, params);
		
		if (params.getOperation().contentEquals("PUSH")) {

			if (params.getTypeSend().contentEquals("FILE"))
				sendPushFileMessage(in, out, params);
			else if (params.getTypeSend().contentEquals("REPOSITORY"))
				sendPushRepMessage(in, out, params);
		}
		else if (params.getOperation().contentEquals("PULL")) {

			if (params.getTypeSend().contentEquals("FILE"))
				sendPullFileMessage(in, out, params);
			else if (params.getTypeSend().contentEquals("REPOSITORY"))
				sendPullRepMessage(in, out, params);
		}

		return "MessagePHandler:sendMessage:"+params.getLocalUser()+" "+params.getServerAddress()+" "+(params.getPassword()==null?"":"-p "+params.getPassword())+" -"+params.getOperation()+" "+params.getRepOrFileName();
	}
			
			
	private String sendPushFileMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {
				
		// o tipo do timestamp é FileTime que é timezone independent!
		// LocalDateTime timestamp = LocalDateTime.now();
	
        BasicFileAttributes attributes = getFileAttributes(params.getFile());

		// Message to use when we want to send or receive a file. Used in PULL fileName and PUSH fileName
		// serverAddress não será necessário, já está presente na criação do socket...
		MessageP mp = new MessageP(new User(params.getLocalUser()), params.getServerAddress(), params.getPassword(), TypeSend.FILE, params.getRepName(),
				params.getFileName(), TypeOperation.PUSH, 1,  attributes.lastModifiedTime().toMillis());
		
		try {
			out.writeObject((Object)mp);
		} catch (IOException e) {
			e.printStackTrace();
		}				

		// Enviar o numero de ficheiros
		try {
			out.writeObject((Integer) 1);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Enviar o ficheiro
			try {
				ReadWriteUtil.sendFile( params.getFile(), in, out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		return "MessagePHandler:sendPushFileMessage";
	}

	
	private String sendPushRepMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {
		
		MessageP mp = new MessageP(new User(params.getLocalUser(), params.getPassword()), params.getServerAddress(), params.getPassword(), TypeSend.REPOSITORY, params.getRepName(),
				TypeOperation.PUSH, 1,  0);
		
		try {
			out.writeObject((Object)mp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		loadRepoFiles(params.getRepName());
		
		// Enviar o numero de ficheiros
		try {
			out.writeObject((Integer) filesList.size());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Enviar os ficheiros
		for (Path path: filesList){
			try {
				ReadWriteUtil.sendFile( path, in, out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return "MessagePHandler:sendPushRepMessage";
	}
	
	
	private String sendPullFileMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {
		
        BasicFileAttributes attributes = getFileAttributes(params.getFile());

		// Message to use when we want to send or receive a file. Used in PULL fileName and PUSH fileName
		// serverAddress não será necessário, já está presente na criação do socket...
		MessageP mp = new MessageP(new User(params.getLocalUser()), params.getServerAddress(), params.getPassword(), TypeSend.FILE, params.getRepName(),
				params.getFileName(), TypeOperation.PULL, 1,  attributes.lastModifiedTime().toMillis());
		
		try {
			out.writeObject((Object)mp);
		} catch (IOException e) {
			e.printStackTrace();
		}				
		
		return "MessagePHandler:sendPullFileMessage";
	}

	
	private String sendPullRepMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {
		
        // Message to use when we want to send or receive a file. Used in PULL fileName and PUSH fileName
		// serverAddress não será necessário, já está presente na criação do socket...
		MessageP mp = new MessageP(new User(params.getLocalUser()), params.getServerAddress(), params.getPassword(), TypeSend.REPOSITORY, params.getRepName(),
				TypeOperation.PULL, 1,  0);
		
		try {
			out.writeObject((Object)mp);
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		return "MessagePHandler:sendPullRepMessage";
	}
	
	
	private void loadRepoFiles(String repName) {
		
		try(Stream<Path> paths = Files.walk(Paths.get("CLIENT"+ File.separator+repName))) {
		    paths.forEach(filePath -> {
		        if (Files.isRegularFile(filePath)) {		            
		            filesList.add(filePath);
		            System.out.println(filePath);
		        }
		    });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}
