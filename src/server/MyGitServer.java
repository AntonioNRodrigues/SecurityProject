package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import message.Message;

public class MyGitServer {
	private ServerSkell sk;
	private static ObjectOutputStream outStream;
	private static ObjectInputStream inStream;


	public MyGitServer() {
		System.out.println("MyGitServer init");
		this.sk = new ServerSkell();
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
		System.out.println("MyGitServer is Running:");
		MyGitServer myGitServer = new MyGitServer();
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
		// ExecutorService executorService =
		// Executors.newFixedThreadPool(MAX_THREADS);
		System.out.println("MyGitServer Waiting for clients:");
		while (true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
				// executorService.execute(newServerThread);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		// sSoc.close();
	}

	class ServerThread extends Thread {

		private Socket socket = null;

		ServerThread(Socket inSoc) throws IOException {
			socket = inSoc;
			System.out.println("server thread to each client");
			
		}

		public void run() {
			try {
				outStream = new ObjectOutputStream(socket.getOutputStream());
				inStream = new ObjectInputStream(socket.getInputStream());
				Message m = null;

				try {
					m = ((Message) inStream.readObject());
					sk.receiveMsg(m, inStream, outStream);
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
