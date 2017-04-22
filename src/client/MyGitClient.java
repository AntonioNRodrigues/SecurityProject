/**
 * Grupo n.33
 * Pedro Pais, n.º 41375
 * Pedto Candido, n.º15674
 * Antonio Rodrigues n.º40853
 */
package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import enums.TypeOperation;
import enums.TypeSend;
import utilities.ReadWriteUtil;

public class MyGitClient {

	private static File propertiesFile;
	private String operation;
	private String typeSend;
	private String serverAddress;
	private String host;
	private int port;
	private String localUser;
	private String password;
	private String userId;
	private String fileName;
	private Path file;
	private String repName;
	private String repOrFileName;
	static String nonce;

	public MyGitClient(String[] args) {

		if (!validateArgs(args))
			printUsage();

	}

	public static void main(String[] args) throws UnknownHostException, IOException, NoSuchAlgorithmException,
			KeyManagementException, InvalidAlgorithmParameterException {

		MyGitClient myGitClient = new MyGitClient(args);
		String op = myGitClient.getOperation();
		if (op.toUpperCase().contentEquals("INIT")) {

			/*
			 * Neste caso não é necessário criar objecto repositório local O
			 * programa limitar-se-á a criar a directoria correspondente ao
			 * repositório, se ainda não existir...
			 */

			createLocalRepo(myGitClient.getRepName());

		} else if (TypeOperation.contains(op)) {

			System.setProperty("javax.net.ssl.keyStore", ".myGitClientKeyStore");
			System.setProperty("javax.net.ssl.trustStore", ".myGitClientTrustStore");
			System.setProperty("javax.net.ssl.keyStorePassword", "badpassword2");
			//System.setProperty("javax.net.debug", "all");

			String trustStore = System.getProperty("javax.net.ssl.trustStore");
			if (trustStore == null) {
				System.out.println("javax.net.ssl.trustStore is not defined");
			} else {
				System.out.println("javax.net.ssl.trustStore = " + trustStore);
			}
			/*
			 * class DefaultTrustManager implements X509TrustManager {
			 * 
			 * @Override public void checkClientTrusted(X509Certificate[] arg0,
			 * String arg1) throws CertificateException {}
			 * 
			 * @Override public void checkServerTrusted(X509Certificate[] arg0,
			 * String arg1) throws CertificateException {}
			 * 
			 * @Override public X509Certificate[] getAcceptedIssuers() { return
			 * null; } }
			 */

			SocketFactory sf = SSLSocketFactory.getDefault();
			Socket socket = sf.createSocket(myGitClient.getHost(), myGitClient.getPort());
			// Socket socket = new Socket(myGitClient.getHost(),
			// myGitClient.getPort());

			// System.out.println("Loading KeyStore " + file + "...");
			// final InputStream in = new FileInputStream(file);
			// final KeyStore ks =
			// KeyStore.getInstance(KeyStore.getDefaultType());
			// ks.load(in, passphrase);
			// in.close();

			/*
			 * final SSLContext context = SSLContext.getInstance("TLS"); final
			 * TrustManagerFactory tmf =
			 * TrustManagerFactory.getInstance(TrustManagerFactory
			 * .getDefaultAlgorithm()); tmf.init(new ManagerFactoryParameters()
			 * { });
			 * 
			 * final X509TrustManager defaultTrustManager = (X509TrustManager)
			 * tmf.getTrustManagers()[0]; final DefaultTrustManager tm = new
			 * DefaultTrustManager(); context.init(null, new TrustManager[] { tm
			 * }, null); final SSLSocketFactory factory =
			 * context.getSocketFactory(); Socket socket =
			 * sf.createSocket(myGitClient.getHost(), myGitClient.getPort());
			 */

			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			try {
				nonce = (String) in.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			// Create the message handler
			IMessageTypes mTypes = MessageFactory.INSTANCE.getmsgType(op);
			if (mTypes != null) {
				try {
					String str = mTypes.sendMessage(in, out, myGitClient);
				} catch (GeneralSecurityException e) {
					e.printStackTrace();
				}
			}

			out.close();
			in.close();
			socket.close();
		} else {

			System.out.println("ERRO");
		}
	}

	private static void createLocalRepo(String repName) {

		Path path = Paths.get("CLIENT" + File.separator + repName);
		boolean exists = Files.exists(path);
		boolean isDirectory = Files.isDirectory(path);
		boolean isFile = Files.isRegularFile(path);

		if (exists && isDirectory) {
			System.out.println("ERRO: Um repositório com esse nome já existe.");
		} else if (exists && isFile) {
			System.out.println("ERRO: Já existe um ficheiro com o mesmo nome dado ao repositório.");
		} else {
			try {
				Files.createDirectories(path);
				System.out.println("-- O repositório " + repName + " foi criado localmente");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getOperation() {
		return this.operation;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	public String getLocalUser() {
		return localUser;
	}

	public String getPassword() {
		return password;
	}

	public String getUserId() {
		return userId;
	}

	public String getRepName() {
		return repName;
	}

	public String getFileName() {
		return fileName;
	}

	public String getRepOrFileName() {
		return repOrFileName;
	}

	public static void printUsage() {

		System.out.println("Invalid command, available options are: ");
		System.out.println("");
		System.out.println("Usage: myGit -init <rep_name>");
		System.out.println("Usage: myGit <localUser> <serverAddress> [ -p <password> ]");
		System.out.println("Usage: myGit <localUser> <serverAddress> [ -p <password> ] -push <rep_name>");
		System.out.println("Usage: myGit <localUser> <serverAddress> [ -p <password> ] -push <file_name>");
		System.out.println("Usage: myGit <localUser> <serverAddress> [ -p <password> ] -pull <file_name>");
		System.out.println("Usage: myGit <localUser> <serverAddress> [ -p <password> ] -pull <rep_name>");
		System.out.println("Usage: myGit <localUser> <serverAddress> [ -p <password> ] -share <rep_name> <userId>");
		System.out.println("Usage: myGit <localUser> <serverAddress> [ -p <password> ] -remove <rep_name> <userId>");

		System.exit(-1);
	}

	// TODO: A alterar no futuro assim que estiver tudo a funcionar
	private boolean validateArgs2(String[] args) {

		// Variavel para nao haver return e permitir imprimir a lista final
		boolean validated = false;

		int ind;
		List<String> lArgs = Arrays.asList(args);
		// System.out.println("lArgs.size(): " + lArgs.size());

		// Variavel aux para o comando
		String command = "";

		/// PASSWORD && COMMAND
		// Caso do INIT que nao tem password
		if (lArgs.get(0).equals("-init"))
			command = lArgs.get(0);

		// Opcao para enviar a password
		else if (lArgs.size() > 2)
			if (lArgs.get(2).equals("-p")) {
				try {
					if (lArgs.get(3).startsWith("-")) {
						System.out.println("ERRO: Escreva uma password sem comecar com o caracter '-'");
						System.exit(-1);
					}
					this.password = lArgs.get(3);
					// caso haja um commando adicional (push, pull, share or
					// remove)
					if (lArgs.size() > 5)
						command = lArgs.get(4);
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("ERRO: Por favor escreva uma password a seguir a flag -p.");
					System.exit(-1);
				}
			} else {
				command = lArgs.get(2);
			}

		/// END OF PASSWORD && COMMAND

		// Depende do comando que � enviado! command = "-init", "-push",
		// "-share".....
		switch (command) {
		case "-init":
			ind = lArgs.indexOf(command);
			this.operation = "INIT";

			if (lArgs.size() >= ind + 1)
				this.repName = lArgs.get(ind + 1);
			// System.out.println();
			validated = true;
			break;

		case "-push":
			ind = lArgs.indexOf(command);
			// System.out.println("ind: " + ind);

			this.operation = "PUSH";

			if (lArgs.size() >= ind + 1) {
				this.repOrFileName = lArgs.get(ind + 1);
				validated = true;
			} else
				validated = false;

			if (!valConnArgs(lArgs, ind))
				validated = false;

			if (!valTypeSend(this.repOrFileName))
				validated = false;
			break;

		case "-pull":
			ind = lArgs.indexOf(command);
			this.operation = "PULL";

			if (lArgs.size() >= ind + 1) {
				this.repOrFileName = lArgs.get(ind + 1);
				validated = true;
			} else
				validated = false;

			if (!valConnArgs(lArgs, ind))
				validated = false;

			break;
		case "-share":
			ind = lArgs.indexOf(command);
			this.operation = "SHARE";

			if (lArgs.size() >= ind + 2) {
				this.repName = lArgs.get(ind + 1);
				this.userId = lArgs.get(ind + 2);
				validated = true;
			} else
				validated = false;

			if (!valConnArgs(lArgs, ind))
				validated = false;

			break;

		case "-remove":
			ind = lArgs.indexOf(command);
			this.operation = "REMOVE";

			if (lArgs.size() >= ind + 2) {
				this.repName = lArgs.get(ind + 1);
				this.userId = lArgs.get(ind + 2);
				validated = true;
			} else
				validated = false;

			if (!valConnArgs(lArgs, ind))
				validated = false;
			break;

		default:
			switch (lArgs.size()) {
			// TODO: Repensar se vale a pena fazer AUTH se n�o houver password
			// (ISTO N�O DEIXA DE SER REDUNDANTE)
			case 2: // n�o existe password
				if (!valConnArgs(lArgs, 2))
					validated = false;
				this.operation = "AUTH";
				validated = true;
				break;
			case 4: // existe password
				if (!valConnArgs(lArgs, 2))
					validated = false;
				this.operation = "AUTH";
				this.password = lArgs.get(3);
				validated = true;
				break;
			default:
				printUsage();
				break;
			}

		}
		return validated;
	}

	private boolean validateArgs(String[] args) {

		// Variavel para nao haver return e permitir imprimir a lista final
		boolean validated = false;

		int ind;
		List<String> lArgs = Arrays.asList(args);
		// System.out.println("lArgs.size(): " + lArgs.size());

		if (lArgs.contains("-init")) {
			ind = lArgs.indexOf("-init");
			this.operation = "INIT";

			if (lArgs.size() >= ind + 1)
				this.repName = lArgs.get(ind + 1);
			// System.out.println();
			return true;

		} else if (lArgs.contains("-push")) {
			ind = lArgs.indexOf("-push");
			// System.out.println("ind: " + ind);

			this.operation = TypeOperation.PUSH.toString();

			if (lArgs.size() >= ind + 1) {
				this.repOrFileName = lArgs.get(ind + 1);
				validated = true;
			} else
				validated = false;

			if (!valConnArgs(lArgs, ind))
				validated = false;

			if (!valTypeSend(this.repOrFileName))
				validated = false;

		} else if (lArgs.contains("-pull")) {
			ind = lArgs.indexOf("-pull");

			this.operation = TypeOperation.PULL.toString();

			if (lArgs.size() >= ind + 1) {
				this.repOrFileName = lArgs.get(ind + 1);
				validated = true;
			} else
				validated = false;

			if (!valConnArgs(lArgs, ind))
				validated = false;

			if (!valTypeSend2(this.repOrFileName))
				validated = false;

		} else if (lArgs.contains("-share")) {
			ind = lArgs.indexOf("-share");
			this.operation = TypeOperation.SHARE.toString();

			if (lArgs.size() >= ind + 2) {
				this.repName = lArgs.get(ind + 1);
				this.userId = lArgs.get(ind + 2);
				validated = true;
			} else
				validated = false;

			if (!valConnArgs(lArgs, ind))
				validated = false;

		} else if (lArgs.contains("-remove")) {
			ind = lArgs.indexOf("-remove");
			this.operation = TypeOperation.REMOVE.toString();

			if (lArgs.size() >= ind + 2) {
				this.repName = lArgs.get(ind + 1);
				this.userId = lArgs.get(ind + 2);
				validated = true;
			} else
				validated = false;

			if (!valConnArgs(lArgs, ind))
				validated = false;

		} else {

			if (lArgs.size() == 4) {
				this.operation = TypeOperation.AUTH.toString();
				if (valConnArgs(lArgs, 4))
					validated = true;
			} else
				validated = false;
		}

		return validated;
	}

	// Para o push
	private boolean valTypeSend(String repOrFileName) {

		String[] repFile = this.repOrFileName.split(File.separator);

		// owner/repo/file ...
		if (repFile.length == 3) {

			this.setTypeSend("FILE");
			this.userId = repFile[0];
			this.repName = repFile[1];
			this.fileName = repFile[2];

			Path path = Paths.get(ReadWriteUtil.CLIENT + File.separator + repFile[1] + File.separator + repFile[2]); // owner/repo/file
			boolean exists = Files.exists(path);
			boolean isFolder = Files.isDirectory(path);
			boolean isFile = Files.isRegularFile(path);

			if (exists && isFile) {
				this.setFile(path);
				return true;
			} else {
				System.out.println("Erro: O ficheiro indicado não existe");
				return false;
			}
		}

		else if (repFile.length == 2) {

			// owner/repo
			Path path1 = Paths.get(ReadWriteUtil.CLIENT + File.separator + repFile[1]);
			boolean exists = Files.exists(path1);
			boolean isFolder = Files.isDirectory(path1);
			boolean isFile = Files.isRegularFile(path1);

			if (exists && isFolder) {
				this.userId = repFile[0];
				this.repName = repFile[1];
				this.setTypeSend(TypeSend.REPOSITORY.toString());
				this.setFile(path1);
				return true;
			}

			// repo/file
			Path path2 = Paths.get(ReadWriteUtil.CLIENT + File.separator + repFile[0] + File.separator + repFile[1]);
			exists = Files.exists(path2);
			isFolder = Files.isDirectory(path2);
			isFile = Files.isRegularFile(path2);

			if (exists && isFile) {
				this.repName = repFile[0];
				this.fileName = repFile[1];
				this.setTypeSend(TypeSend.FILE.toString());
				this.setFile(path2);
				return true;
			}

			System.out.println("Erro: O ficheiro indicado não existe");
			return false;
		}
		// repo
		else if (repFile.length == 1) {

			Path path3 = Paths.get("CLIENT" + File.separator + repFile[0]); // repo
			boolean exists = Files.exists(path3);
			boolean isFolder = Files.isDirectory(path3);

			if (exists && isFolder) {
				this.repName = repFile[0];
				this.setTypeSend("REPOSITORY");
				return true;
			} else {
				System.out.println("Erro: O folder/repositório indicado não existe");
				return false;
			}
		} else {
			System.out.println("Erro: Parametros invalidos");
			return false;
		}
	}

	// Para ser usado no pull
	private boolean valTypeSend2(String repOrFileName) {

		String[] repFile = this.repOrFileName.split(File.separator);

		// owner/repo/file ...
		if (repFile.length == 3) {

			this.setTypeSend("FILE");
			this.userId = repFile[0];
			this.repName = repFile[1];
			this.fileName = repFile[2];

			Path path = Paths.get("CLIENT" + File.separator + repFile[1]); // owner/repo/file
			boolean exists = Files.exists(path);
			boolean isFolder = Files.isDirectory(path);
			// não podemos testar existencia de ficheiro no pull
			// boolean isFile = Files.isRegularFile(path);

			if (exists && isFolder) {
				this.setTypeSend("FILE");
				this.setFile(path);
				return true;
			} else {
				System.out.println("Erro: Parametros invalidos");
				return false;
			}
		} else if (repFile.length == 2) {

			Path path1 = Paths.get("CLIENT" + File.separator + repFile[1]); // owner/repo
			boolean exists = Files.exists(path1);
			boolean isFolder = Files.isDirectory(path1);
			// boolean isFile = Files.isRegularFile(path);

			if (exists && isFolder) {
				this.userId = repFile[0];
				this.repName = repFile[1];
				this.setTypeSend("REPOSITORY");
				this.setFile(path1);
				return true;
			}

			Path path2 = Paths.get("CLIENT" + File.separator + repFile[0]); // repo/file
			exists = Files.exists(path2);
			isFolder = Files.isDirectory(path2);

			if (exists && isFolder) {
				this.repName = repFile[0];
				this.fileName = repFile[1];
				this.setTypeSend("FILE");
				this.setFile(path2);
				return true;
			}

			System.out.println("Erro: Parametros invalidos");
			return false;
		}
		// repo
		else if (repFile.length == 1) {

			Path path3 = Paths.get("CLIENT" + File.separator + repFile[0]); // repo
			boolean exists = Files.exists(path3);
			boolean isFolder = Files.isDirectory(path3);

			if (exists && isFolder) {
				this.repName = repFile[0];
				this.setTypeSend("REPOSITORY");
				return true;
			} else {
				System.out.println("Erro: Parametros invalidos");
				return false;
			}
		} else {
			System.out.println("Erro: Parametros invalidos");
			return false;
		}
	}

	private boolean valConnArgs(List<String> lArgs, int ind) {
		if (ind == 2) {
			this.localUser = lArgs.get(0);
			this.serverAddress = lArgs.get(1);
			String[] srvAdr = this.serverAddress.split(":");
			this.host = srvAdr[0];
			this.port = Integer.parseInt(srvAdr[1]);
		} else if (ind == 4) {
			this.localUser = lArgs.get(0);
			this.serverAddress = lArgs.get(1);
			this.password = lArgs.get(3);
			String[] srvAdr = this.serverAddress.split(":");
			this.host = srvAdr[0];
			this.port = Integer.parseInt(srvAdr[1]);
		} else {
			System.out.println("Error: valConnArgs");
			return false;
		}
		return true;
	}

	public static void checkPropertiesFile() {

		OutputStream output = null;
		String userHomeDir = System.getProperty("user.home");
		String fullPathFileName = userHomeDir + File.separator + ".myGit";
		propertiesFile = new File(fullPathFileName);

		// create properties file if doesn't exist
		if (!propertiesFile.exists())
			try {
				output = new FileOutputStream(propertiesFile);
				output.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

	}

	public static String getProperty(String propertyName) {

		checkPropertiesFile();

		Properties prop = new Properties();
		InputStream input = null;
		String propertyValue = null;

		try {
			input = new FileInputStream(propertiesFile);
			prop.load(input);

			propertyValue = prop.getProperty(propertyName);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return propertyValue;

	}

	public static void setProperty(String propertyName, String propertyValue) {
		checkPropertiesFile();

		Properties prop = new Properties();
		OutputStream output = null;

		try {

			output = new FileOutputStream(propertiesFile);

			String currentPropertyValues = null;
			currentPropertyValues = getProperty(propertyName);
			if (currentPropertyValues != null)
				prop.setProperty(propertyName, currentPropertyValues + "," + propertyValue);
			else
				prop.setProperty(propertyName, propertyValue);

			prop.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String getTypeSend() {
		return typeSend;
	}

	public void setTypeSend(String typeSend) {
		this.typeSend = typeSend;
	}

	public Path getFile() {
		return file;
	}

	public void setFile(Path file) {
		this.file = file;
	}

}
