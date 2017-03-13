package server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.Message;
import message.MessageP;
import server.repository.RemoteRepository;
import utilities.ReadWriteUtil;

public class MyGitServer {
	private static final int MAX_THREADS = 5;
	private static ServerSkell sk;

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
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
				sk.setIn(inStream);
				sk.setOut(outStream);
				RemoteRepository rr = null;
				boolean runCode = false;
				Message m = null;
				// receive message
				try {
					m = ((Message) inStream.readObject());
					sk.receiveMsg(m);
				} catch (ClassNotFoundException e) {
					// e.printStackTrace();
				} finally {
					if (m instanceof MessageP) {
						MessageP mp = (MessageP) m;
						if (mp.getNumberFiles() > 1) {
							runCode = false;
						}
					}
				}

				// its not a message is list of files when we do a push
				// repository
				// see a better way to get the number of files that have to be
				// send
				// trying this in the code, probably this not work... see effect
				// with multiple threads
				if (runCode) {

					int sizeList = 0;
					try {
						sizeList = (Integer) inStream.readObject();
						System.out.println("sizelist: " + sizeList);
					} catch (ClassNotFoundException e1) {
						// e1.printStackTrace();
					}

					for (int i = 0; i < sizeList; i++) {
						try {
							File received = ReadWriteUtil.receiveFile(inStream, outStream);
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
						// do timestamp check and reject or accept the file;
					}
					runCode = false;
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
