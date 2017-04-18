package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;

public interface IMessageTypes {

	/**
	 * @return
	 */
	String getName();

	/**
	 * @param operation
	 * @return
	 * @throws GeneralSecurityException 
	 */
	String sendMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) throws GeneralSecurityException;

}