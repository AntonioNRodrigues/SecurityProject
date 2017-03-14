package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import message.Message;
import user.User;

public abstract class MessageHandler implements IMessageTypes {
	
	/**
	 * The name of the message handler 
	 */
	private String name;
    
	/**
	 * Constructs a message handler given its name
	 * 
	 * @param name The name of the handler
	 * @return 
	 */
	public MessageHandler (String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	
	public String sendAuthMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {

		// Mensagem de autenticação
		Message m = new Message(new User(params.getLocalUser(), params.getPassword()), params.getHost() + params.getPort(), params.getPassword());

		try {
			out.writeObject((Object)m);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;	
}
	
	protected BasicFileAttributes getFileAttributes(Path filePath) {
		
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
		
		return attributes;
	}

}