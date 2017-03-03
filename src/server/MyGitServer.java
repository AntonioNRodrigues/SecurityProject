package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.Message;

public class MyGitServer {
	private static final int MAX_THREADS = 5;
	private static ServerSkell sk;

	private static boolean checkParams(String[] args) {
		return (args.length == 1) ? true : false;
	}

	public static void main(String[] args) {
		if(!checkParams(args)){
			System.out.println("MyGitServer is NOT Running");
			System.out.println("MyGitServer requires port number");
			System.exit(-1);
		}
		System.out.println("MyGitServer is Running:");
		MyGitServer myGitServer = new MyGitServer();
		sk = new ServerSkell();
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
		ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
		System.out.println("MyGitServer Waiting for clients:");
		while (true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				executorService.execute(newServerThread);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		// sSoc.close();
	}

	class ServerThread extends Thread {

		private Socket socket = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("server thread to each client");
		}

		public void run() {
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
				sk.setIn(inStream);
				sk.setOut(outStream);

				// receive message
				try {
					sk.receiveMsg((Message) inStream.readObject());
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
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
