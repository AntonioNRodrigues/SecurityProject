package client;

import java.io.File;
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

		// test AUTH -- new user
		Message auth = new Message(new User("antonio", "password"), HOST + ":" + PORT, "password");
		// out.writeObject((Object)auth);
		// register new user ----DONE

		Message auth2 = new Message(new User("manel"), HOST + ":" + PORT, "");
		// out.writeObject((Object) auth2);
		String str = "newPass";
		try {
			// str = (String) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Message m = new Message(new User("n", "p"), HOST + PORT, "p");
		File folder = new File("CLIENT/REP01");
		System.out.println(folder);
		File[] listFolder = folder.listFiles();
		System.out.println(listFolder[0]);
		// send repo
		MessageP mp = new MessageP(new User("antonio", "password"), HOST + PORT, "password", TypeSend.REPOSITORY, folder.getName(),
				TypeOperation.PUSH, listFolder.length, folder.lastModified());
		out.writeObject((Object) mp);
		// send each file in repo
		System.err.println("next");
		for (File file : listFolder) {
			MessageP pp = new MessageP(new User("antonio","password"), HOST + ":" + PORT, "password", TypeSend.FILE, folder.getName(),
					file.getName(), TypeOperation.PUSH, 1, file.lastModified());
			System.out.println( file.getName());
			out.writeObject((Object) pp);
		}
		MessageP mp2 = new MessageP(new User("n"), HOST + PORT, "P", TypeSend.REPOSITORY, "REP01", TypeOperation.PUSH,
				1, 0);
		// MessageP mp4 = new MessageP(new User("n"), HOST + PORT, "P",
		// TypeSend.FILE, "REP01", TypeOperation.PUSH, 1, 0);
		MessageP mp1 = new MessageP(new User("n", "p"), HOST + PORT, "p", TypeSend.FILE, "REP01/1.txt",
				TypeOperation.PUSH, 1, 0);

		// out.writeObject((Object) m);
		// out.writeObject((Object)listFolder.length);
		for (File file : listFolder) {
			// ReadWriteUtil.sendFile(file.getAbsolutePath(), in, out);
		}
		// out.writeObject((Object) mp1);
		// ReadWriteUtil.sendFile("REP01/1.txt",in, out);
		out.close();
		in.close();
		socket.close();
	}

}