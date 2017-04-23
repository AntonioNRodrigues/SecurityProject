package client;

import static utilities.ReadWriteUtil.CLIENT;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import enums.TypeOperation;
import enums.TypeSend;
import message.MessageP;
import user.User;
import utilities.ReadWriteUtil;
import utilities.SecurityUtil;

public class MessagePHandler extends MessageHandler {

	private List<Path> filesList;

	public MessagePHandler() {
		super("MessagePHandler");
		this.filesList = new ArrayList<>();
	}

	@Override
	public String sendMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params)
			throws GeneralSecurityException {

		if (params.getOperation().contentEquals(TypeOperation.PUSH.toString())) {

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

	private String sendPushFileMessage(ObjectInputStream in, ObjectOutputStream out, MyGitClient params)
			throws GeneralSecurityException {

		// o tipo do timestamp é FileTime que é timezone independent!
		// LocalDateTime timestamp = LocalDateTime.now();

		BasicFileAttributes attributes = getFileAttributes(params.getFile());

		MessageP mp = new MessageP(new User(params.getLocalUser(), params.getPassword(), MyGitClient.nonce),
				params.getServerAddress(), params.getPassword(), TypeSend.FILE, params.getRepName(),
				params.getFileName(), TypeOperation.PUSH, 1, attributes.lastModifiedTime().toMillis());

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
				// Gera chave privada atrav�s da password do utilizador -
				// TODO: VERIFICAR COMO � QUE O UTILIZADOR OBTEM A CHAVE PRIVADA
				// / COMO � PARTILHADA?!

				Path p = Paths.get(".myGitClientKeyStore");

				KeyPair kp = SecurityUtil.getKeyPairFromKS(p, "mygitclient", "badpassword2");

				// Cliente gera a assinatura digital do ficheiro em claro
				byte[] signature = SecurityUtil.generateSignatureOfFile(params.getFile(), kp.getPrivate());
				// Envia a assinatura
				out.writeObject(signature);

				// gerar uma chave aleatoria para utilizar com o AES
				SecretKey key = SecurityUtil.getKey();

				Path cifrado = Paths.get(
						CLIENT + File.separator + params.getRepName() + File.separator + params.getFileName() + ".cif");

				// Cifrar o ficheiro com a chave criada
				SecurityUtil.cipherFile(params.getFile(), key, cifrado);

				// Envia a chave para o Servidor
				out.writeObject(key);

				// Prepara e envia o ficheiro
				ReadWriteUtil.sendFile(cifrado, in, out);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SignatureException e) {
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

		MessageP mp = new MessageP(new User(params.getLocalUser(), params.getPassword(), MyGitClient.nonce),
				params.getServerAddress(), params.getPassword(), TypeSend.REPOSITORY, params.getRepName(),
				TypeOperation.PUSH, filesList.size(), 0);

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

					// Gera chave privada atrav�s da password do utilizador -
					// TODO: VERIFICAR COMO � QUE O UTILIZADOR OBTEM A CHAVE
					// PRIVADA / COMO � PARTILHADA?!
					Path p = Paths.get(".myGitClientKeyStore");

					KeyPair kp = SecurityUtil.getKeyPairFromKS(p, "mygitclient", "badpassword2");

					// Cliente gera a assinatura digital do ficheiro em claro
					byte[] signature = SecurityUtil.generateSignatureOfFile(params.getFile(), kp.getPrivate());
					// Envia a assinatura
					out.writeObject(signature);

					// gerar uma chave aleatoria para utilizar com o AES
					SecretKey key = SecurityUtil.getKey();

					System.out.println(params.getFileName());
					Path cifrado = Paths.get(CLIENT + File.separator + params.getRepName() + File.separator
							+ params.getFileName() + ".cif");
					System.out.println(cifrado.toAbsolutePath());
					// Cifrar o ficheiro com a chave criada
					SecurityUtil.cipherFile(params.getFile(), key, cifrado);

					// Envia a chave para o Servidor
					out.writeObject(key);

					// Prepara e envia o ficheiro
					out.writeObject((Object) f.lastModified());
					ReadWriteUtil.sendFile(cifrado, in, out);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (GeneralSecurityException e) {
					// TODO Auto-generated catch block
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

		// Se o ficheiro a que se está a fazer pull já existe então armazenar
		// e
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

		MessageP mp = new MessageP(new User(params.getLocalUser(), params.getPassword(), MyGitClient.nonce),
				params.getServerAddress(), params.getPassword(), TypeSend.FILE, params.getRepName(),
				params.getFileName(), TypeOperation.PULL, 1, lastModifiedTime);

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
		System.out.println(result);
		if (result.contentEquals("OK")) {
			// receive the files
			receiveFilesPullRep(params.getRepName(), in, out);

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
		// serverAddress não será necessário, já está presente na criação
		// do
		// socket...
		MessageP mp = new MessageP(new User(params.getLocalUser(), params.getPassword(), MyGitClient.nonce),
				params.getServerAddress(), params.getPassword(), TypeSend.REPOSITORY, params.getRepName(),
				TypeOperation.PULL, 0, 0);

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
			receiveFilesPullRep(params.getRepName(), in, out);
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
				Long receivedTimeStamp = (Long) in.readObject();
				String path = CLIENT + File.separator + repoName + File.separator;
				File received = ReadWriteUtil.receiveFile(path, in, out);
				received.setLastModified(receivedTimeStamp);
				File inRepo = new File(
						CLIENT + File.separator + repoName + File.separator + received.getName().split(" ")[0]);
				if (inRepo.exists()) {
					if (received.lastModified() <= inRepo.lastModified()) {
						Files.deleteIfExists(received.toPath());
					} else if (received.lastModified() > inRepo.lastModified()) {
						received.renameTo(inRepo);
					}
				} else if (!(inRepo.exists())) {
					received.renameTo(inRepo);
				}

			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}

		}

	}

	private void receiveFilesPullRep(String repoName, ObjectInputStream in, ObjectOutputStream out) {

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
				//Recebe a chave K do servidor para decifrar o ficheiro
				byte[] key = (byte[]) in.readObject();
				
				SecretKey secretKey = new SecretKeySpec(key, 0, key.length, "AES");
				
				Long receivedTimeStamp = (Long) in.readObject();
				Path path = Paths.get(CLIENT + File.separator + repoName + File.separator);
				File received = ReadWriteUtil.receiveFile(path.toString(), in, out);
				
				//Decifrar o ficheiro recebido com K
				SecurityUtil.decipherFile(received.toPath(), secretKey, path);
				
				
				//TODO: FALTA RECEBER E VERIFICAR A ASSINATURA
				
				received.setLastModified(receivedTimeStamp);
				File inRepo = new File(
						CLIENT + File.separator + repoName + File.separator + received.getName().split(" ")[0]);
				if (inRepo.exists()) {
					if (received.lastModified() <= inRepo.lastModified()) {
						Files.deleteIfExists(received.toPath());
					} else if (received.lastModified() > inRepo.lastModified()) {
						received.renameTo(inRepo);
					}
				} else if (!(inRepo.exists())) {
					received.renameTo(inRepo);
				}

			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadPaddingException e) {
				// TODO Auto-generated catch block
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
