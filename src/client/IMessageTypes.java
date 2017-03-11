package client;

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
	 */
	String sendMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient2 params);	
	

}
