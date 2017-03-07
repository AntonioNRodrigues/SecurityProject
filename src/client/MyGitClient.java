package client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;

import enums.TypeOperation;
import enums.TypeSend;
import message.Message;
import message.MessageP;
import user.User;
import utilities.ReadWriteUtil;

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

		/*
		 * Message m = null; Scanner sc = new Scanner(System.in); String str =
		 * null; do { System.out.println("COMMAND?"); str = sc.nextLine(); // do
		 * validation of params String[] array = str.split(" ");
		 * System.out.println(array.length); if(array.length == 6){ String[] a =
		 * array[4].split(":"); socket = new Socket(HOST, PORT); in = new
		 * ObjectInputStream(socket.getInputStream()); out = new
		 * ObjectOutputStream(socket.getOutputStream()); m = new Message(new
		 * User(array[2], array[3]), array[4], array[2]); System.out.println(m);
		 * out.writeObject((Object)m); }if(array.length == 7){ String[] a =
		 * array[4].split(":"); socket = new Socket(HOST, PORT); in = new
		 * ObjectInputStream(socket.getInputStream()); out = new
		 * ObjectOutputStream(socket.getOutputStream()); m = new Message(new
		 * User(array[2], array[3]), array[4], array[2]); System.out.println(m);
		 * out.writeObject((Object)m); }
		 * 
		 * } while (!(str.equalsIgnoreCase("QUIT")));
		 */

		Message m = new Message(new User("n", "p"), HOST + PORT, "p");
		File folder = new File("CLIENT/REP01");
		File[] listFolder = folder.listFiles();

		MessageP mp = new MessageP(new User("n"), HOST + PORT, "p", TypeSend.REPOSITORY, folder.getAbsolutePath(),
				TypeOperation.PUSH, listFolder.length, folder.lastModified());

		MessageP mp2 = new MessageP(new User("n"), HOST + PORT, "P", TypeSend.REPOSITORY, "REP01", TypeOperation.PUSH,
				1, 0);
		MessageP mp4 = new MessageP(new User("n"), HOST + PORT, "P", TypeSend.FILE, "REP01", TypeOperation.PUSH, 1, 0);
		MessageP mp1 = new MessageP(new User("n", "p"), HOST + PORT, "P", TypeSend.FILE, "REP01", TypeOperation.PUSH, 1,
				0);

		out.writeObject((Object) m);

		for (File file : listFolder) {
			ReadWriteUtil.sendFile(file.getAbsolutePath(), in, out);
		}

		out.close();
		in.close();
		socket.close();
	}

}