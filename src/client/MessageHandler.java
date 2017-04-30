package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;

import message.Message;
import message.MessageA;
import user.User;
import utilities.ReadWriteUtil;
import utilities.SecurityUtil;

public abstract class MessageHandler implements IMessageTypes {
	/**
	 * The name of the message handler
	 */
	private String name;

	/**
	 * Constructs a message handler given its name
	 * 
	 * @param name
	 *            The name of the handler
	 * @return
	 */
	public MessageHandler(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public String sendAuthMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {
		// Mensagem de autenticacao
		Message m = new MessageA(new User(params.getLocalUser(), params.getPassword(), MyGitClient.nonce),
				params.getServerAddress(), params.getPassword());

		try {
			out.writeObject((Object) m);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// get status from server
		String result = "";
		String status = "";
		try {
			result = (String) in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		if (result.contentEquals("OK") || result.contentEquals("NOK")) {
			try {
				status = (String) in.readObject();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			System.out.println(status);
		} else {
			status = "Ocorreu um erro no processamento do pedido";
		}
		return status;
	}

	protected BasicFileAttributes getFileAttributes(Path filePath) {

		BasicFileAttributes attributes = null;
		try {
			attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
		} catch (IOException exception) {
			System.out.println("Exception handled when trying to get file " + "attributes: " + exception.getMessage());
		}
		return attributes;
	}

}