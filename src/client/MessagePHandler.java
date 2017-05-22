package client;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static utilities.ReadWriteUtil.CLIENT;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

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
				File tempDir = new File(CLIENT + File.separator + params.getRepName() + File.separator + "temp");
				tempDir.mkdirs();

				KeyPair kp = SecurityUtil.getKeyPairFromKS(Paths.get(".myGitClientKeyStore"), "mygitclient",
						"badpassword2");

				// Cliente gera a assinatura digital do ficheiro em claro
				byte[] signature = SecurityUtil.generateSignatureOfFile(params.getFile(), kp.getPrivate());
				// Envia a assinatura
				out.writeObject(signature);

				// gerar uma chave aleatoria para utilizar com o AES
				SecretKey secretKey = SecurityUtil.getKey();

				Path cifrado = Paths.get(tempDir + File.separator + params.getFileName());

				// Cifrar o ficheiro com a chave criada
				SecurityUtil.cipherFile(params.getFile(), secretKey, cifrado);

				// Envia a chave para o Servidor
				out.writeObject(secretKey);

				// Prepara e envia o ficheiro cifrado
				ReadWriteUtil.sendFile(cifrado, in, out);
				Files.deleteIfExists(cifrado);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SignatureException e) {
				e.printStackTrace();
			}
			System.out.println("O  ficheiro " + params.getFileName() + " foi copiado  para o servidor");
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
		File tempDir = new File(CLIENT + File.separator + params.getRepName() + File.separator + "temp");
		tempDir.mkdirs();

		if (result.contentEquals("OK")) {
			// Enviar os ficheiros
			for (Path path : filesList) {
				try {
					File f = path.toFile();

					KeyPair kp = SecurityUtil.getKeyPairFromKS(Paths.get(".myGitClientKeyStore"), "mygitclient",
							"badpassword2");

					// Cliente gera a assinatura digital do ficheiro em claro
					byte[] signature = SecurityUtil.generateSignatureOfFile(path, kp.getPrivate());
					// Envia a assinatura
					out.writeObject(signature);

					// gerar uma chave aleatoria para utilizar com o AES
					SecretKey secretKey = SecurityUtil.getKey();

					Path cifrado = Paths.get(tempDir + File.separator + f.getName());

					// Cifrar o ficheiro com a chave criada
					SecurityUtil.cipherFile(path, secretKey, cifrado);

					// Envia a chave para o Servidor
					out.writeObject(secretKey);

					// Prepara e envia o ficheiro
					out.writeObject((Object) f.lastModified());
					ReadWriteUtil.sendFile(cifrado, in, out);
					Files.deleteIfExists(cifrado);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (GeneralSecurityException e) {
					e.printStackTrace();
				}
				System.out.println("-- O  repositorio " + params.getRepName() + " foi copiado  para o  servidor");
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
		Path path = Paths.get(CLIENT + File.separator + params.getRepOrFileName());
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
				Long receivedTimeStamp = (Long) in.readObject();
				// Recebe a chave K do servidor para decifrar o ficheiro

				SecretKey secretKey = (SecretKey) in.readObject();

				Path path = Paths.get(CLIENT + File.separator + repoName + File.separator);
				// Recebe o ficheiro cifrado.

				File received = ReadWriteUtil.receiveFile(path.toString() + File.separator, in, out);
				System.out.println("NAME FILE RECEIVED" + received.getAbsolutePath() + received.getName());
				SecurityUtil.decipherFile2(received.toPath(), secretKey,
						Paths.get(path + File.separator + "temp" + received.getName()));

				String lastUser = (String) in.readObject();

				// Recebe a assinatura
				byte[] signature = (byte[]) in.readObject();

				Certificate c = SecurityUtil.getCertFromKeyStore(Paths.get(".myGitClientKeyStore"), "mygitclient",
						"badpassword2");

				PublicKey pk = c.getPublicKey();

				// ficheiro
				File file = new File(path + File.separator + "temp" + received.getName());
				FileInputStream fiStream = new FileInputStream(file);
				byte[] data = new byte[(int) file.length()];
				fiStream.read(data);
				fiStream.close();

				// Verifica a assinatura com o que recebeu
				Signature s = Signature.getInstance("SHA256withRSA");
				s.initVerify(pk);
				s.update(data);
				if (s.verify(signature))
					System.out.println("A assinatura eh valida!");
				else
					System.out.println("A assinatura foi corrompida");

				File inRepo = new File(
						CLIENT + File.separator + repoName + File.separator + received.getName().split(" ")[0]);

				CopyOption[] options = new CopyOption[] { REPLACE_EXISTING };
				Path p = Files.copy(file.toPath(), received.toPath(), options);
				Files.delete(file.toPath());

				p.toFile().setLastModified(receivedTimeStamp);

				if (inRepo.exists()) {
					if (received.lastModified() <= inRepo.lastModified()) {
						Files.deleteIfExists(received.toPath());
					} else if (received.lastModified() > inRepo.lastModified()) {
						received.renameTo(inRepo);
					}
				} else if (!(inRepo.exists())) {
					received.renameTo(inRepo);
				}

			} catch (ClassNotFoundException | IOException | InvalidKeyException | NoSuchAlgorithmException
					| NoSuchPaddingException | IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			} catch (SignatureException e) {
				System.out.println("Erro: ERRO NA CONVERSAO DA ASSINATURA.");
				e.printStackTrace();
			}
		}

	}

	private void loadRepoFiles(String repName) {

		try (Stream<Path> paths = Files.walk(Paths.get(CLIENT + File.separator + repName))) {
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
