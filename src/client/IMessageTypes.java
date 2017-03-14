package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface IMessageTypes {
	
	/**
	 * @return
	 */
	String getName();
	
	/**
	 * @param operation
	 * @return 
	 * @throws IOException 
	 */
	String sendMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params);	
	
}