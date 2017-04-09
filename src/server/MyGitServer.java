/**
 * Grupo n.33
 * Pedro Pais, n.º 41375
 * Pedto Candido, n.º15674
 * Antonio Rodrigues n.º40853
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.Message;
import server.repository.RepositoryCatalog;
import user.UserCatalog;
import utilities.SecurityUtil;

public class MyGitServer {
	private static final int MAX_THREADS = 5;
	private static ServerSkell sk;

	private RepositoryCatalog catRepo;
	private UserCatalog catUsers;

	public MyGitServer() {
		this.catRepo = new RepositoryCatalog();
		this.catUsers = new UserCatalog();
	}

	private static boolean checkParams(String[] args) {
		return (args.length == 1) ? true : false;
	}

	public static void main(String[] args) {
		if (!checkParams(args)) {
			System.out.println("MyGitServer is NOT Running");
			System.out.println("MyGitServer requires port number");
			System.exit(-1);
		}
		System.out.println("MyGitServer:: Please fill up the password");
		Scanner sc = new Scanner(System.in);
		String pass = sc.nextLine();
		//check && validate pass length > 6 and < 10 ex::: -----> TO DO
		System.out.println("MyGitServer is Running with password");
		SecurityUtil.generateKeyFromPass(pass);
		MyGitServer myGitServer = new MyGitServer();
		sk = new ServerSkell(myGitServer);
		myGitServer.startServer(args);
	}

	@SuppressWarnings("resource")
	public void startServer(String[] args) {
		ServerSocket sSoc = null;

		try {
			sSoc = new ServerSocket(Integer.parseInt(args[0]));
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		 ExecutorService executorService =
		 Executors.newFixedThreadPool(MAX_THREADS);
		System.out.println("MyGitServer Waiting for clients:");
		while (true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				//newServerThread.start();
				 executorService.execute(newServerThread);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		// sSoc.close();
	}

	public RepositoryCatalog getCatRepo() {
		return catRepo;
	}

	public void setCatRepo(RepositoryCatalog catRepo) {
		this.catRepo = catRepo;
	}

	public UserCatalog getCatUsers() {
		return catUsers;
	}

	public void setCatUsers(UserCatalog catUsers) {
		this.catUsers = catUsers;
	}

	class ServerThread extends Thread {

		private Socket socket = null;

		ServerThread(Socket inSoc) throws IOException {
			socket = inSoc;
			System.out.println("server thread to each client");
		}

		public void run() {
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
				sk.setIn(inStream);
				sk.setOut(outStream);
				Message m = null;

				// receive message
				try {
					m = ((Message) inStream.readObject());
					sk.receiveMsg(m);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				outStream.close();
				inStream.close();
				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
