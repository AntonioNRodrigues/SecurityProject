package server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import message.Message;
import message.MessageP;
import server.repository.RemoteRepository;
import server.repository.RepositoryCatalog;
import utilities.ReadWriteUtil;

public class MyGitServer {
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
				RepositoryCatalog catRepo = null;
				RemoteRepository rr = null;
				boolean multipleFiles = false;
				Message m = null;
				int sizeList = 1;

				// receive message
				try {
					m = ((Message) inStream.readObject());
					sk.receiveMsg(m);
				} catch (ClassNotFoundException e) {
					// e.printStackTrace();
				}
//				} finally {
//					if (m instanceof MessageP) {
//						MessageP mp = (MessageP) m;
//						
//						rr = catRepo.getRemRepository(((MessageP) m).getRepoName());
//						sizeList = mp.getNumberFiles();
//						List<File> praAtualizar = new ArrayList<File>();
//						for (int i = 0; i < sizeList; i++) {
//							try {
//								File received = ReadWriteUtil.receiveFile(inStream, outStream);
//								
//								
//								// COMPARAR TIMESTAMPS
//
//								Long timeMsgFile = ((MessageP) m).getTimestamp();
//								if(rr.getMostRecentFile(received.getName()).lastModified() < received.lastModified())
//									praAtualizar.add(received);
//													
//							} catch (ClassNotFoundException e) {
//								e.printStackTrace();
//							}
//						}
//						
//						// Guardar ficheiros caso seja necessário
//						// (persistir)
//						rr.addFilesToRepo(rr.getNameRepo(), praAtualizar);
//					}
//				}

				outStream.close();
				inStream.close();
				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
