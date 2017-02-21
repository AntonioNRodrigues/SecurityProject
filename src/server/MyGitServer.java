package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MyGitServer {
	
	private static boolean checkParams(String[] args) {
		return (args.length == 1) ? true : false;
	}

	public static void main(String[] args) {
		System.out.println("MyGitServer is Running:");
		MyGitServer myGitServer = new MyGitServer();
		myGitServer.startServer(args);
	}

	public void startServer(String[] args) {
		ServerSocket sSoc = null;

		try {
			sSoc = new ServerSocket(Integer.parseInt(args[0]));
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		while (true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
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
			System.out.println("thread do server para cada cliente");
		}

		public void run() {
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
				/*
				 * String user = null; String passwd = null;
				 * 
				 * try { user = (String) inStream.readObject(); passwd =
				 * (String) inStream.readObject(); System.out.
				 * println("thread: depois de receber a password e o user"); }
				 * catch (ClassNotFoundException e1) { e1.printStackTrace(); }
				 * 
				 * if (user.length() != 0) { outStream.writeObject(new
				 * Boolean(true)); } else { outStream.writeObject(new
				 * Boolean(false)); }
				 * 
				 * // File f = receiveFile();
				 */
				outStream.close();
				inStream.close();

				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
