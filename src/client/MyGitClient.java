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
import user.User;

public class MyGitClient {
	private static final String HOST = "127.0.0.1";
	private static final int PORT = 23456;
	private static ObjectInputStream in;
	private static ObjectOutputStream out;

	public static void main(String[] args) throws UnknownHostException, IOException {
		System.out.println("Client on the move");
		Socket socket = new Socket(HOST, PORT);
		in = new ObjectInputStream(socket.getInputStream());
		out = new ObjectOutputStream(socket.getOutputStream());
		Message m = new Message(new User("n", "p"), HOST + PORT, "p");
		MessageP mp = new MessageP(new User("n"), HOST + PORT, "p", TypeSend.REPOSITORY, "REP01", TypeOperation.PUSH, 1, 0);
		MessageP mp2 = new MessageP(new User("n"), HOST + PORT, "P", TypeSend.REPOSITORY, "REP01", TypeOperation.PUSH,
				1, 0);
		MessageP mp4 = new MessageP(new User("n"), HOST + PORT, "P", TypeSend.FILE, "REP01", TypeOperation.PUSH, 1, 0);
		MessageP mp1 = new MessageP(new User("n", "p"), HOST + PORT, "P", TypeSend.FILE, "REP01", TypeOperation.PUSH, 1,
				0);

		/*out.writeObject((Object) m);
		try {
			String str = (String) in.readObject();
			System.out.println(str);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}*/
		out.writeObject((Object) mp);

		// out.writeObject((Object)mp1);
		// ReadWriteUtil.sendFile(new
		// File("CLIENT/REP01/1.txt").getAbsolutePath(), in, out);
		// out.writeObject((Object)m);
		out.close();
		in.close();
		socket.close();
	}

}