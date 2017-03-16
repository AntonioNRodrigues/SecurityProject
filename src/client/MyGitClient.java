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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import enums.TypeOperation;

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
	//private LocalRepository lRepo;

	public MyGitClient(String[] args) {

		if (!validateArgs(args))
			printUsage();

	}

	public static void main(String[] args)
			throws UnknownHostException, IOException {

		MyGitClient myGitClient = new MyGitClient(args);
		String op = myGitClient.getOperation();
		System.out.println("op: " + op);

		if (op.toUpperCase().contentEquals("INIT")) {

			/*
			 * Neste caso não é necessário criar objecto repositório
			 * local O programa limitar-se-á a criar a directoria
			 * correspondente ao repositório, se ainda não existir...
			 */

			createLocalRepo(myGitClient.getRepName());
			
		} else if (TypeOperation.contains(op)) {

			Socket socket = new Socket(myGitClient.getHost(),
					myGitClient.getPort());
			ObjectInputStream in = new ObjectInputStream(
					socket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(
					socket.getOutputStream());

			// Create the message handler
			IMessageTypes mTypes = MessageFactory.INSTANCE.getmsgType(op);
			if (mTypes != null) {
				String str = mTypes.sendMessage(in, out, myGitClient);
				System.out.println(str);
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
			System.out.println(
					"ERRO: Um repositório com esse nome já existe.");
		} else if (exists && isFile) {
			System.out.println(
					"ERRO: Já existe um ficheiro com o mesmo nome dado ao repositório.");
		}
		else {
			try {
				Files.createDirectories(path);
				System.out.println(
						"-- O repositório "+repName+" foi criado localmente");
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
		System.out.println(
				"Usage: myGit <localUser> <serverAddress> [ -p <password> ]");
		System.out.println(
				"Usage: myGit <localUser> <serverAddress> [ -p <password> ] -push <rep_name>");
		System.out.println(
				"Usage: myGit <localUser> <serverAddress> [ -p <password> ] -push <file_name>");
		System.out.println(
				"Usage: myGit <localUser> <serverAddress> [ -p <password> ] -pull <file_name>");
		System.out.println(
				"Usage: myGit <localUser> <serverAddress> [ -p <password> ] -pull <rep_name>");
		System.out.println(
				"Usage: myGit <localUser> <serverAddress> [ -p <password> ] -share <rep_name> <userId>");
		System.out.println(
				"Usage: myGit <localUser> <serverAddress> [ -p <password> ] -remove <rep_name> <userId>");

		System.exit(-1);
	}

	// TODO: A alterar no futuro assim que estiver tudo a funcionar
	private boolean validateArgs2(String[] args) {

		// Variavel para nao haver return e permitir imprimir a lista final
		boolean validated = false;

		System.out.println("args.length: " + args.length);

		for (String a : args) {
			System.out.println(a);
		}

		int ind;
		List<String> lArgs = Arrays.asList(args);
		System.out.println("lArgs.size(): " + lArgs.size());

		// Vari�vel aux para o comando
		String command = "";

		/// PASSWORD && COMMAND
		// Caso do INIT que nao tem password
		if (lArgs.get(0).equals("-init"))
			command = lArgs.get(0);

		// Op��o para enviar a password
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
			System.out.println();
			validated = true;
			break;

		case "-push":
			ind = lArgs.indexOf(command);
			System.out.println("ind: " + ind);

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

		// Apenas para testar (nao estava a ser imprimido)
		System.out.println("-------------------------DEPOIS DE VALIDAR OS ARGUMENTOS ---------------------");
		System.err.println("validated: " + validated);
		System.out.println("localUser: " + this.localUser);
		System.out.println("serverAddress: " + this.serverAddress);
		System.out.println("host: " + this.host);
		System.out.println("port: " + this.port);
		System.out.println("password: " + this.password);
		System.out.println("operation: " + this.operation);
		System.out.println("repName: " + this.repName);
		System.out.println("fileName: " + this.fileName);
		System.out.println("repOrFileName: " + this.repOrFileName);
		System.out.println("userId: " + this.userId);
		System.out.println("---------------------------------------------------------------------------");

		return validated;
	}
	
	// Old one
	private boolean validateArgs(String[] args) {

		// Variavel para nao haver return e permitir imprimir a lista final
		boolean validated = false;

		System.out.println("args.length: " + args.length);

		for (String a : args) {
			System.out.println(a);
		}

		int ind;
		List<String> lArgs = Arrays.asList(args);
		System.out.println("lArgs.size(): " + lArgs.size());

		if (lArgs.contains("-init")) {
			ind = lArgs.indexOf("-init");
			this.operation = "INIT";

			if (lArgs.size() >= ind + 1)
				this.repName = lArgs.get(ind + 1);
			System.out.println();
			return true;

		} else if (lArgs.contains("-push")) {
			ind = lArgs.indexOf("-push");
			System.out.println("ind: " + ind);

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

		} else if (lArgs.contains("-pull")) {
			ind = lArgs.indexOf("-pull");
			
			this.operation = "PULL";

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
			this.operation = "SHARE";

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
			this.operation = "REMOVE";

			if (lArgs.size() >= ind + 2) {
				this.repName = lArgs.get(ind + 1);
				this.userId = lArgs.get(ind + 2);
				validated = true;
			} else
				validated = false;

			if (!valConnArgs(lArgs, ind))
				validated = false;

		} else {

			//if (lArgs.size() == 2) {
			//	if (valConnArgs(lArgs, 2))
			//		validated = true;
			//} else 

			if (lArgs.size() == 4) {
				this.operation = "AUTH";
				if (valConnArgs(lArgs, 4))
					validated = true;			
			} else
				validated = false;
		}

		// Apenas para testar (nao estava a ser imprimido)
		System.out.println(
				"-------------------------DEPOIS DE VALIDAR OS ARGUMENTOS ---------------------");
		System.err.println("validated: " + validated);
		System.out.println("localUser: " + this.localUser);
		System.out.println("serverAddress: " + this.serverAddress);
		System.out.println("host: " + this.host);
		System.out.println("port: " + this.port);
		System.out.println("password: " + this.password);
		System.out.println("operation: " + this.operation);
		System.out.println("repName: " + this.repName);
		System.out.println("fileName: " + this.fileName);
		System.out.println("repOrFileName: " + this.repOrFileName);
		System.out.println("userId: " + this.userId);
		System.out.println(
				"---------------------------------------------------------------------------");

		return validated;
	}

	
	private boolean valTypeSend(String repOrFileName) {

		// TODO: working on it now...confusing specs...see
		// java myGit maria 127.0.0.1:23456 -p badpwd  -push myrep
		// java myGit pedro 127.0.0.1:23456 -p badpwd1 -pull maria/myrep
		// java myGit pedro 127.0.0.1:23456 -p badpwd1 -push maria/myrep/myGit.java
		// java myGit maria 127.0.0.1:23456 -p badpwd  -pull myrep/myGit.java
		// java myGit pedro 127.0.0.1:23456 -p badpwd1 -pull maria/myrep
		// Obrigo que se escreva sempre o rep no caminho do ficheiro como nos exemplos do trabalho?
		// Se quiser fazer push do ficheiro myGyt.java que esta no meu repositorio myrep terei de fazer
		// java myGit maria 127.0.0.1:23456 -p badpwd  -push myrep/myGit.java
		// TODO: ainda nao esta comtemplado o caso de repositorios partilhados: maria/myrep/myGit.java
		
		Path path = Paths.get("CLIENT"+ File.separator + repOrFileName);
		boolean exists = Files.exists(path);
		boolean isDirectory = Files.isDirectory(path);
		boolean isFile = Files.isRegularFile(path);

		// Feito para o push
		// Não funciona para o pull file pois o ficheiro pode ainda não existir
		// localmente...
		if (exists && isDirectory) {
			this.setTypeSend("REPOSITORY");
			this.repName = repOrFileName;
		} else if (exists && isFile) {
			this.setTypeSend("FILE");
			
			String[] repFile = this.repOrFileName.split(File.separator);
			this.repName = repFile[0];
			this.fileName =repFile[1];

			this.setFile(path);
		} else {
			return false;
		}
		return true;
	}

	private boolean valTypeSend2(String repOrFileName) {
			
			String[] repFile = this.repOrFileName.split(File.separator);
			
			if (repFile.length == 2) {
				this.repName = repFile[0];
				this.fileName =repFile[1];
				this.setTypeSend("FILE");

				Path path = Paths.get("CLIENT"+ File.separator + repOrFileName);
				boolean exists = Files.exists(path);
				boolean isDirectory = Files.isDirectory(path);
				boolean isFile = Files.isRegularFile(path);

				if (exists && isFile) {
					this.setFile(path);
				}

				return true;
			}
			else if (repFile.length == 1) {
				this.repName = repFile[0];
				this.setTypeSend("REPOSITORY");
				return true;
			}
			else {
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
				prop.setProperty(propertyName,
						currentPropertyValues + "," + propertyValue);
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
