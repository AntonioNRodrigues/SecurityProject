package client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import enums.TypeOperation;
import enums.TypeSend;
import message.Message;
import message.MessageP;
import message.MessageRS;
import user.User;
import utilities.ReadWriteUtil;

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

		out.writeObject((Object) new Message(new User("manel", "manel"), "address", "manel"));
		//out.writeObject((Object) new MessageRS(new User("name", "password"), "serverAddres", "pass", "reposName",
			//	"userId", TypeOperation.PULL));

		//List<File> tempList = Arrays.asList(new File("CLIENT/REP01/").listFiles());
		//System.out.println(tempList);
		//out.writeObject((Object) new MessageP(new User("n", "p"), "password", "sss", TypeSend.REPOSITORY, "REP01",
			//	TypeOperation.PUSH, tempList.size()));
		/*for (File f : tempList) {
			out.writeObject((Object) new MessageP(new User("n", "p"), "password", "sss", TypeSend.REPOSITORY,
					"REP01/" + f.getName(), TypeOperation.PUSH, 0));
			ReadWriteUtil.sendFile("REP01/" + f.getName(), in, out);

		}*/

		// out.writeObject((Object) new MessageP(new User("n", "p"), "password",
		// "sss", TypeSend.FILE,
		// new File("CLIENT/REP01/1.txt").getCanonicalPath(),
		// TypeOperation.PUSH, 1));
		// ReadWriteUtil.sendFile(new
		// File("CLIENT/REP01/1.txt").getAbsolutePath(), in, out);

		out.close();
		in.close();
		socket.close();
	}
}
