package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import enums.TypeOperation;
import enums.TypeSend;
import message.Message;
import message.MessageP;
import message.MessageRS;
import user.User;

public class MyGitClient {
	private static final int VALUE = 1024;
	private static final String HOST = "127.0.0.1";
	private static final int PORT = 23456;
	private static ObjectInputStream in;
	private static ObjectOutputStream out;

	public static void main(String[] args) throws UnknownHostException, IOException {
		System.out.println("Client on the move");
		Socket socket = new Socket(HOST, PORT);
		in = new ObjectInputStream(socket.getInputStream());
		out = new ObjectOutputStream(socket.getOutputStream());

		out.writeObject((Object) new Message(new User("nnn", "ppp"), "address", "ppp"));
		out.writeObject((Object) new MessageRS(new User("name", "password"), "serverAddres", "pass", "reposName",
				"userId", TypeOperation.PULL));
		out.writeObject(
				(Object) new MessageP(new User("n", "p"), "password", "sss", TypeSend.FILE, "name", TypeOperation.PULL));

		out.close();
		in.close();
		socket.close();
	}
}
