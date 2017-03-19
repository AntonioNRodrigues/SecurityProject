package client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import enums.TypeOperation;
import enums.TypeSend;
import message.MessageP;
import user.User;
import utilities.ReadWriteUtil;

public class MessagePHandler extends MessageHandler {

	private List<Path> filesList;

	public MessagePHandler() {
		super("MessagePHandler");
		this.filesList = new ArrayList<>();
	}

	@Override
	public String sendMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {

		if (params.getOperation().contentEquals("PUSH")) {

			if (params.getTypeSend().contentEquals("FILE"))
				sendPushFileMessage(in, out, params);
			else if (params.getTypeSend().contentEquals("REPOSITORY"))
				sendPushRepMessage(in, out, params);
		} else if (params.getOperation().contentEquals("PULL")) {

			if (params.getTypeSend().contentEquals("FILE"))
				sendPullFileMessage(in, out, params);
			else if (params.getTypeSend().contentEquals("REPOSITORY"))
				sendPullRepMessage(in, out, params);
		}

		return "MessagePHandler:sendMessage:" + params.getLocalUser() + " " + params.getServerAddress() + " "
				+ (params.getPassword() == null ? "" : "-p " + params.getPassword()) + " -" + params.getOperation()
				+ " " + params.getRepOrFileName();
	}

	private String sendPushFileMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {

		// o tipo do timestamp é FileTime que é timezone independent!
		// LocalDateTime timestamp = LocalDateTime.now();

		BasicFileAttributes attributes = getFileAttributes(params.getFile());

		MessageP mp = new MessageP(new User(params.getLocalUser(), params.getPassword()), params.getServerAddress(),
				params.getPassword(), TypeSend.FILE, params.getRepName(), params.getFileName(), TypeOperation.PUSH, 1,
				attributes.lastModifiedTime().toMillis());

		try {
			out.writeObject((Object) mp);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String result = "";
		try {
			result = (String) in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		if (result.contentEquals("OK")) {
			// Enviar o ficheiro
			try {
				ReadWriteUtil.sendFile(params.getFile(), in, out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("-- O  ficheiro " + params.getFileName() + " foi copiado  para o servidor");
		} else if (result.contentEquals("NOK")) {
			System.out.println("OK");
			String error = "";
			try {
				error = (String) in.readObject();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			System.out.println(error);
		} else
			System.out.println("Something really bad happened...");

		return "MessagePHandler:sendPushFileMessage";
	}

	private String sendPushRepMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {

		loadRepoFiles(params.getRepName());

		MessageP mp = new MessageP(new User(params.getLocalUser(), params.getPassword()), params.getServerAddress(),
				params.getPassword(), TypeSend.REPOSITORY, params.getRepName(), TypeOperation.PUSH, filesList.size(),
				0);

		try {
			out.writeObject((Object) mp);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// get status from server
		String result = null;
		try {
			result = (String) in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		if (result.contentEquals("OK")) {
			// Enviar os ficheiros
			for (Path path : filesList) {
				try {
					File f = path.toFile();
					out.writeObject((Object) f.lastModified());
					ReadWriteUtil.sendFile(path, in, out);
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("-- O  repositório " + params.getRepName() + " foi copiado  para o  servidor");
			}

		} else if (result.contentEquals("NOK")) {
			String error = "";
			try {
				error = (String) in.readObject();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			System.out.println(error);
		} else
			System.out.println("Something really bad happened...");

		return "MessagePHandler:sendPushRepMessage";
	}

	private String sendPullFileMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {

		// Se o ficheiro a que se está a fazer pull já existe então armazenar e
		// enviar a data da última modificação
		Path path = Paths.get("CLIENT" + File.separator + params.getRepOrFileName());
		boolean exists = Files.exists(path);
		// boolean isDirectory = Files.isDirectory(path);
		boolean isFile = Files.isRegularFile(path);

		long lastModifiedTime = 0;
		BasicFileAttributes attributes = null;
		if (exists && isFile) {
			attributes = getFileAttributes(params.getFile());
			lastModifiedTime = attributes.lastModifiedTime().toMillis();
		}

		MessageP mp = new MessageP(new User(params.getLocalUser(), params.getPassword()), params.getServerAddress(),
				params.getPassword(), TypeSend.FILE, params.getRepName(), params.getFileName(), TypeOperation.PULL, 1,
				lastModifiedTime);

		try {
			out.writeObject((Object) mp);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String result = "";
		try {
			result = (String) in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		if (result.contentEquals("OK")) {
			// receive the files
			receiveFiles(params.getRepName(), in, out);
			System.out.println("O  ficheiro " + params.getFileName() + " foi copiado do servidor");
		} else if (result.contentEquals("NOK")) {
			String error = "";
			try {
				error = (String) in.readObject();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			System.out.println(error);
		} else
			System.out.println("Something happened...");

		return "MessagePHandler:sendPullFileMessage";
	}

	private String sendPullRepMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params) {

		// Message to use when we want to send or receive a file. Used in PULL
		// fileName and PUSH fileName
		// serverAddress não será necessário, já está presente na criação do
		// socket...
		MessageP mp = new MessageP(new User(params.getLocalUser(), params.getPassword()), params.getServerAddress(),
				params.getPassword(), TypeSend.REPOSITORY, params.getRepName(), TypeOperation.PULL, 0, 0);

		try {
			out.writeObject((Object) mp);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String result = null;
		try {
			result = (String) in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		if (result.contentEquals("OK")) {
			// receive the files
			receiveFilesPushRep(params.getRepName(), in, out);
			System.out.println("O  repositorio " + params.getRepName() + " foi copiado do servidor");
		} else if (result.contentEquals("NOK")) {
			String error = "";
			try {
				error = (String) in.readObject();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			System.out.println(error);
		} else
			System.out.println("Something happened...");

		return "MessagePHandler:sendPullRepMessage";
	}

	private void receiveFiles(String repoName, ObjectInputStream in, ObjectOutputStream out) {

		// mesmmo protocolo do servidor, receber primeiro o numero de ficheiros,
		// ler depois os ficheiros
		int sizeList = 0;
		try {
			sizeList = (Integer) in.readObject();
			// System.out.println("sizelist: " + sizeList);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < sizeList; i++) {
			try {
				// Long receivedTimeStamp = (Long) in.readObject();
				String path = "CLIENT" + File.separator + repoName + File.separator;
				File received = ReadWriteUtil.receiveFile(path, in, out);
				// received.setLastModified(receivedTimeStamp);

			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			// do timestamp check and reject or accept the file;
		}

	}

	private void receiveFilesPushRep(String repoName, ObjectInputStream in, ObjectOutputStream out) {

		// mesmmo protocolo do servidor, receber primeiro o numero de ficheiros,
		// ler depois os ficheiros
		int sizeList = 0;
		try {
			sizeList = (Integer) in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < sizeList; i++) {
			try {
				Long receivedTimeStamp = (Long) in.readObject();
				String path = "CLIENT" + File.separator + repoName + File.separator;
				File received = ReadWriteUtil.receiveFile(path, in, out);
				received.setLastModified(receivedTimeStamp);
				

			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void loadRepoFiles(String repName) {

		try (Stream<Path> paths = Files.walk(Paths.get("CLIENT" + File.separator + repName))) {
			paths.forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					filesList.add(filePath);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
