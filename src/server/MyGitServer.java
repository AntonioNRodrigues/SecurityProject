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
import java.net.Socket;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.NoSuchPaddingException;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

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
		// check && validate pass length > 6 and < 10 ex::: -----> TO DO
		System.out.println("MyGitServer is Running with password");
		SecurityUtil.generateKeyFromPass(pass);

		// Check password file integrity

		MyGitServer myGitServer = new MyGitServer();
		sk = new ServerSkell(myGitServer);
		myGitServer.startServer(args);
	}

	@SuppressWarnings("resource")
	public void startServer(String[] args) {
		SSLServerSocket sSoc = null;
		// ServerSocket sSoc = null;

		try {
			System.setProperty("javax.net.ssl.keyStore", ".myGitServerKeyStore");
			System.setProperty("javax.net.ssl.trustStore", ".myGitServerTrustStore");
			System.setProperty("javax.net.ssl.keyStorePassword", "badpassword1");
			//System.setProperty("javax.net.debug", "all");

			String trustStore = System.getProperty("javax.net.ssl.trustStore");
			if (trustStore == null) {
				System.out.println("javax.net.ssl.trustStore is not defined");
			} else {
				System.out.println("javax.net.ssl.trustStore = " + trustStore);
			}

			/*
			 * TrustManager tm = new X509TrustManager() { public void
			 * checkClientTrusted(X509Certificate[] chain, String authType)
			 * throws CertificateException { //do nothing, you're the client }
			 * 
			 * @Override public X509Certificate[] getAcceptedIssuers() { //also
			 * only relevant for servers // TODO Auto-generated method stub
			 * return null; }
			 * 
			 * @Override public void checkServerTrusted(X509Certificate[] chain,
			 * String authType) throws CertificateException {
			 */
			/*
			 * chain[chain.length -1] is the candidate for the root certificate.
			 * Look it up to see whether it's in your list. If not, ask the user
			 * for permission to add it. If not granted, reject. Validate the
			 * chain using CertPathValidator and your list of trusted roots.
			 */
			// }
			// };

			/* Create keystore */
			// KeyStore keyStore =
			// KeyStore.getInstance(KeyStore.getDefaultType());
			// keyStore.load(new FileInputStream(keyStore),
			// passphrase.toCharArray());

			/* Get factory for the given keystore */
			// KeyManagerFactory kmf =
			// KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			// kmf.init(keystore, password.toCharArray()); // That's the key's
			// password, if different.

			// TrustManagerFactory tmf =
			// TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			// tmf.init(keyStore);

			// SSLContext ctx = SSLContext.getInstance("SSL");
			// ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			// SSLServerSocketFactory factory = ctx.getServerSocketFactory();

			ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
			sSoc = (SSLServerSocket) ssf.createServerSocket(Integer.parseInt(args[0]));

			// Client authentication
			// sSoc.setNeedClientAuth(true);

			// sSoc = new ServerSocket(Integer.parseInt(args[0]));
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
				// newServerThread.start();
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
				String nonce = SecurityUtil.generateNonce();
				outStream.writeObject((Object) nonce);
				sk.setIn(inStream);
				sk.setOut(outStream);
				sk.setNonce(nonce);
				Message m = null;

				// receive message
				try {
					m = ((Message) inStream.readObject());
					sk.receiveMsg(m);
				} catch (ClassNotFoundException | KeyStoreException | NoSuchAlgorithmException | CertificateException
						| NoSuchPaddingException e) {
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
