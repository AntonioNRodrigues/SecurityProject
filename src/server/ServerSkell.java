
/**
 * Grupo n.33
 * Pedro Pais, n.º 41375
 * Pedto Candido, n.º15674
 * Antonio Rodrigues n.º40853
 */
package server;

import static utilities.ReadWriteUtil.SERVER;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import enums.TypeOperation;
import enums.TypeSend;
import message.Message;
import message.MessageA;
import message.MessageP;
import message.MessageRS;
import server.repository.RemoteRepository;
import server.repository.RepositoryCatalog;
import user.User;
import user.UserCatalog;
import utilities.ReadWriteUtil;
import utilities.SecurityUtil;

public class ServerSkell {
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private RepositoryCatalog catRepo;
	private UserCatalog catUsers;
	private String nonce;

	public ServerSkell(MyGitServer my) {
		this.catRepo = my.getCatRepo();
		this.catUsers = my.getCatUsers();
	}

	public ServerSkell(ObjectOutputStream out, ObjectInputStream in) {
		this.out = out;
		this.in = in;
	}

	public ObjectOutputStream getOut() {
		return out;
	}

	public void setOut(ObjectOutputStream out) {
		this.out = out;
	}

	public ObjectInputStream getIn() {
		return in;
	}

	public void setIn(ObjectInputStream in) {
		this.in = in;
	}

	public void receiveMsg(Message msg) throws ClassNotFoundException, IOException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, NoSuchPaddingException {
		catRepo.listRepos();

		// check if the user has the ownership over the request

		RemoteRepository rr = null;
		if (authentication(msg)) {
			// System.out.println("THE USER IS AUTHENTICATED");

			if (msg instanceof MessageA) {
				// caso de uso: "java myGit pedro 127.0.0.1:23456 -p badpwd1"
				// do nothing, all the work done inside authentication(msg)
				// but but keep this here to not reach the else in the end
			} else if (msg instanceof MessageRS) {
				// System.out.println(msg);
				MessageRS mrs = (MessageRS) msg;
				TypeOperation op = mrs.getTypeOperation();

				switch (op) {
				case REMOVE:

					// Repositorio nao existe
					if (!catRepo.repoExists(mrs.getRepoName())) {

						out.writeObject((Object) "NOK");
						out.writeObject((Object) "Erro: O repositório não existe");

					} else {
						// Repositorio existe

						rr = catRepo.getRemRepository(mrs.getRepoName());

						boolean error = false;
						// Validar se o utilizador eh dono
						if (!rr.getOwner().equals(mrs.getLocalUser().getName())) {
							error = true;
							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: Não é dono do repositório");
						}

						// Validar se o userId existe
						if (!error && !catUsers.userExists(mrs.getUserId())) {
							error = true;
							out.writeObject((Object) "NOK");
							out.writeObject(
									(Object) "Erro: O utilizador indicado na remoção de partilha do repositório não existe");
						}

						// Validar se o userId é utilizador com acesso ao
						// repositorio
						if (!error && !rr.getSharedUsers().contains(mrs.getUserId())) {
							error = true;
							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: O utilizador indicado não tem acesso ao repositório");
						}

						if (!error) {
							out.writeObject((Object) "OK");
							rr.removeSharedUserFromRepo(mrs.getUserId());
						}
					}

					break;

				case SHARE:

					// Repositorio nao existe
					if (!catRepo.repoExists(mrs.getRepoName())) {

						out.writeObject((Object) "NOK");
						out.writeObject((Object) "Erro: O repositório não existe");

					} else {
						// Repositorio existe

						rr = catRepo.getRemRepository(mrs.getRepoName());

						boolean error = false;
						// Validar se o utilizador eh dono
						if (!rr.getOwner().equals(mrs.getLocalUser().getName())) {
							error = true;
							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: Não é dono do repositório");
						}

						// Validar se o userId existe
						if (!error && !catUsers.userExists(mrs.getUserId())) {
							error = true;
							out.writeObject((Object) "NOK");
							out.writeObject(
									(Object) "Erro: O utilizador com quem vai partilhar o repositório não existe");
						}

						if (!error) {
							out.writeObject((Object) "OK");
							System.out.println("mrs.getUserId(): " + mrs.getUserId());
							rr.addShareUserToRepo(mrs.getUserId());
						}
					}

					break;
				default:
					break;
				}

			} else if (msg instanceof MessageP) {

				MessageP mp = ((MessageP) msg);
				TypeSend typeSend = mp.getTypeSend();
				TypeOperation operation = mp.getOperation();

				switch (typeSend) {
				case REPOSITORY:
					switch (operation) {
					case PULL:

						boolean error = false;
						// Validar se o repositorio existe
						if (!catRepo.repoExists(mp.getRepoName())) {
							error = true;
							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: O repositorio indicado nao existe");
						} else
							rr = catRepo.getRemRepository(mp.getRepoName());

						// Validar se o utilizador e dono ou tem acesso
						// partilhado ao repositorio
						if (!error && !(rr.getOwner().equals(mp.getLocalUser().getName())
								|| rr.getSharedUsers().contains(mp.getLocalUser().getName()))) {
							error = true;
							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: o utilizador nao tem acesso ao repositorio");
						}
						if (!error) {

							CopyOnWriteArrayList<Path> uniqueList = rr.getUniqueList();
							if (uniqueList.size() > 0) {
								try {
									out.writeObject((Object) "OK");
									// Enviar o numero de ficheiros
									out.writeObject((Object) uniqueList.size());

									for (Path f : uniqueList) {
										// Enviar timeststamp
										out.writeObject((Object) f.toFile().lastModified());
										// pull interaction
										pullInteraction(mp, f.toString().split(" ")[0], "");

										// send file
										ReadWriteUtil.sendFile(SERVER + File.separator + mp.getRepoName()
												+ File.separator + f.toFile().getName(), in, out);

										String lastUser = rr.getLastUser(f.getFileName().toString().split(" ")[0]);

										out.writeObject((Object) lastUser);

										// send the signatureof the file
										sendSignature(Paths.get(f.toString()));

									}
								} catch (IOException e) {
									e.printStackTrace();
									out.writeObject((Object) "NOK");
									out.writeObject((Object) "SERVER ERROR");
								}
							} else {
								out.writeObject((Object) "NOK");
								out.writeObject(
										(Object) "O repositorio local esta sincronizado, nao existe nenhuma versao mais recente dos ficheiros.");
							}
						}
						break;

					case PUSH:

						if (!catRepo.repoExists(mp.getRepoName())) {
							// repository does not exist
							rr = catRepo.buildRepo(mp.getLocalUser(), mp.getRepoName());
							// System.out.println("repository created");
						} else
							// Repositorio existe
							rr = catRepo.getRemRepository(mp.getRepoName());

						// Validar se o utilizador e dono ou tem acesso
						// partilhado ao repositorio
						if (rr.getOwner().equals(mp.getLocalUser().getName())
								|| rr.getSharedUsers().contains(mp.getLocalUser().getName())) {

							out.writeObject((Object) "OK");

							// Receber os ficheiros
							int sizeList = mp.getNumberFiles();
							for (int i = 0; i < sizeList; i++) {
								try {

									String path = SERVER + File.separator + mp.getRepoName() + File.separator;
									// Recebe assinatura do ficheiro
									byte[] signature = (byte[]) in.readObject();
									// Guarda-a com a extensao .sig

									FileOutputStream fos = new FileOutputStream(path + "temp" + ".sig");
									fos.write(signature);
									fos.close();

									// Recebe chave key para depois cifra-la
									// usando a sua chave publica
									SecretKey key = (SecretKey) in.readObject();

									KeyPair kPair = SecurityUtil.getKeyPairFromKS(Paths.get(".myGitServerKeyStore"),
											"mygitserver", "badpassword1");

									Cipher cif = Cipher.getInstance("RSA");
									cif.init(Cipher.WRAP_MODE, kPair.getPublic());
									byte[] chaveCifrada = cif.wrap(key);

									FileOutputStream kos = new FileOutputStream(path + "temp" + ".key.server");
									kos.write(chaveCifrada);
									kos.close();

									Long timestampReceivedFile = (Long) in.readObject();
									File received = ReadWriteUtil.receiveFile(SERVER + File.separator, in, out);

									Files.copy(Paths.get(path + "temp.sig"),
											new FileOutputStream(new File(path + received.getName() + ".sig")));

									Files.copy(Paths.get(path + "temp.key.server"),
											new FileOutputStream(new File(path + received.getName() + ".key.server")));

									rr.addLastUser(received.getName(), mp.getLocalUser().getName());

									received.setLastModified(timestampReceivedFile);

									// most recent file in repository
									File fileInRepo = rr.getFile(received.getName());

									if (fileInRepo == null) {
										File f = new File(path + received.getName()
												+ ReadWriteUtil.timestamp(timestampReceivedFile));
										received.renameTo(f);
										f.setLastModified(timestampReceivedFile);
										rr.addFile(f.getName().split(" ")[0], f);
									} else if (fileInRepo != null) {
										long timeStampFileInRepo = fileInRepo.lastModified();
										if (timeStampFileInRepo < received.lastModified()) {
											File f = new File(path + received.getName()
													+ ReadWriteUtil.timestamp(timestampReceivedFile));
											received.renameTo(f);
											f.setLastModified(timestampReceivedFile);
											rr.addFile(f.getName().split(" ")[0], f);
										} else {
											Files.deleteIfExists(received.toPath());
										}
									}
									Files.deleteIfExists(Paths.get(path + "temp.key.server"));
									Files.deleteIfExists(Paths.get(path + "temp.sig"));

								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								} catch (InvalidKeyException e) {
									e.printStackTrace();
								} catch (IllegalBlockSizeException e) {
									e.printStackTrace();
								}
							}

						} else {
							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: o utilizador não tem acesso ao repositório");
						}

						break;

					default:
						break;
					}
					break;

				case FILE:
					switch (operation) {
					case PULL:
						boolean error = false;
						// Validar se o repositorio existe
						if (!catRepo.repoExists(mp.getRepoName())) {
							error = true;
							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: O repositório indicado não existe");
						} else
							rr = catRepo.getRemRepository(mp.getRepoName());

						if (!error && !(rr.getOwner().equals(mp.getLocalUser().getName())
								|| rr.getSharedUsers().contains(mp.getLocalUser().getName()))) {
							error = true;
							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: o utilizador não tem acesso ao repositório");
						}

						// Validar se o ficheiro existe
						if (!error && !rr.fileExists(mp.getFileName())) {
							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: O ficheiro indicado não existe");
						} else {

							// Inicio do envio do ficheiro cifrado
							File inRepoCifrado = rr.getFile(mp.getFileName());

							// client does not have the recent file so send it
							if (mp.getTimestamp() <= inRepoCifrado.lastModified()) {
								try {
									String path = SERVER + File.separator + mp.getRepoName() + File.separator;
									out.writeObject((Object) "OK");
									// Enviar o numero de ficheiros
									out.writeObject((Object) new Integer(1));

									// Enviar timeststamp
									out.writeObject((Object) inRepoCifrado.lastModified());

									// pull interaction
									pullInteraction(mp, path, mp.getFileName());

									// send file
									ReadWriteUtil.sendFile(SERVER + File.separator + mp.getRepoName() + File.separator
											+ inRepoCifrado.getName(), in, out);

									String lastUser = rr.getLastUser(mp.getFileName());

									out.writeObject((Object) lastUser);

									// Vai buscar a assinatura e envia para o
									// cliente
									sendSignature(Paths.get(path + mp.getFileName()));

								} catch (IOException e) {
									e.printStackTrace();
									out.writeObject((Object) "NOK");
									out.writeObject((Object) "SERVER ERROR");
								}
							} else {
								out.writeObject((Object) "NOK");
								out.writeObject((Object) "Nao existe nenhuma versao mais recente do ficheiro.");
							}
						}

						break;

					case PUSH:

						// Repositorio nao existe
						if (!catRepo.repoExists(mp.getRepoName())) {

							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: O repositorio nao existe");

						} else {
							// Repositorio existe

							rr = catRepo.getRemRepository(mp.getRepoName());

							// Validar se o utilizador eh dono ou tem acesso
							// partilhado ao repositorio
							if (rr.getOwner().equals(mp.getLocalUser().getName())
									|| rr.getSharedUsers().contains(mp.getLocalUser().getName())) {

								out.writeObject((Object) "OK");

								// Receber ficheiro
								try {

									// Prepara caminhos para o ficheiro
									String path = SERVER + File.separator + mp.getRepoName() + File.separator;
									String tempPath = SERVER + File.separator;

									// Recebe assinatura do ficheiro
									byte[] signature = (byte[]) in.readObject();
									// Guarda-a com a extens�o .sig

									FileOutputStream fos = new FileOutputStream(path + mp.getFileName() + ".sig");
									fos.write(signature);
									fos.close();

									// Recebe chave key para depois cifra-la
									// usando a sua chave publica
									SecretKey secretKey = (SecretKey) in.readObject();

									// method to get the certificate from
									// keystore

									KeyPair kPair = SecurityUtil.getKeyPairFromKS(Paths.get(".myGitServerKeyStore"),
											"mygitserver", "badpassword1");
									Cipher cif = Cipher.getInstance("RSA");
									cif.init(Cipher.WRAP_MODE, kPair.getPublic());
									byte[] chaveCifrada = cif.wrap(secretKey);

									FileOutputStream kos = new FileOutputStream(
											path + mp.getFileName() + ".key.server");
									kos.write(chaveCifrada);
									kos.close();

									// Recebe ficheiro
									File received = ReadWriteUtil.receiveFile(tempPath, in, out);
									received.setLastModified(mp.getTimestamp());

									rr.addLastUser(received.getName(), mp.getLocalUser().getName());

									File fileInRepo = rr.getFile(mp.getFileName());
									// file does not exist add it to list
									if (fileInRepo == null) {
										// new file inside the repo
										File f = new File(
												path + mp.getFileName() + ReadWriteUtil.timestamp(mp.getTimestamp()));
										// copy the received file inside the
										// server to the repoName
										received.renameTo(f);
										f.setLastModified(mp.getTimestamp());
										CopyOnWriteArrayList<Path> tempL = new CopyOnWriteArrayList<>();
										tempL.add(f.toPath());
										rr.getMapVersions().put(mp.getFileName(), tempL);
									} else if (fileInRepo != null) {
										Long timeStampFileInRepo = fileInRepo.lastModified();
										if (timeStampFileInRepo < received.lastModified()) {
											File f = new File(path + mp.getFileName()
													+ ReadWriteUtil.timestamp(mp.getTimestamp()));
											// pass the received file to repo
											// folder
											received.renameTo(f);
											f.setLastModified(mp.getTimestamp());
											rr.getMapVersions().get(mp.getFileName()).add(f.toPath());
										} else {
											Files.deleteIfExists(received.toPath());
										}
									}

								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								} catch (InvalidKeyException e) {
									e.printStackTrace();
								} catch (IllegalBlockSizeException e) {
									e.printStackTrace();
								}

							} else {
								out.writeObject((Object) "NOK");
								out.writeObject((Object) "Erro: o utilizador não tem acesso ao repositório");
							}

						}
						break;
					}
				}
			}
		} else

		{
			out.writeObject((Object) "NOK");
			out.writeObject((Object) "YOU DOT NOT HAVE PREMISSIONS TO KEEP GOING PLEASE CHECK PASSWORD");
		}
	}

	private boolean authentication(Message msg) {

		if (msg instanceof MessageA) {
			// caso de uso: "java myGit pedro 127.0.0.1:23456 -p badpwd1"
			MessageA m = (MessageA) msg;

			User u = catUsers.getMapUsers().get(m.getLocalUser().getName());
			// user does not exist, register user
			// in this case since there is no pass in the system there is no way
			// to check
			// the messageDigest is good or not
			if (u == null) {
				catUsers.registerUser(m.getLocalUser().getName(), m.getPassword());
				try {
					out.writeObject((Object) "OK");
					out.writeObject((Object) "O  utilizador " + m.getLocalUser().getName() + " foi criado");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// user exists check permissions
			if (u != null) {

				byte[] mdUser = m.getLocalUser().getB();
				String str = u.getPassword() + getNonce();
				byte[] mdServer = SecurityUtil.calcSintese(str);

				boolean mdCompare = MessageDigest.isEqual(mdUser, mdServer);
				if (mdCompare) {
					try {
						out.writeObject((Object) "OK");
						out.writeObject((Object) "O  utilizador " + m.getLocalUser().getName() + " foi autenticado");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// user has password filled and its the same

				if (u.getPassword().equals(m.getLocalUser().getPassword())) {
					try {
						out.writeObject((Object) "OK");
						out.writeObject((Object) "O  utilizador " + m.getLocalUser().getName() + " foi autenticado");
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						out.writeObject((Object) "NOK");
						out.writeObject((Object) "Erro: O  utilizador " + m.getLocalUser().getName()
								+ " introduziu uma password invalida");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return true;
		} else {

			User u = catUsers.getMapUsers().get(msg.getLocalUser().getName());
			// user does not exist, register user
			if (u == null) {
				System.out.println("THE USER WAS NOT FOUND:: REGISTERING USER");
				catUsers.registerUser(msg.getLocalUser().getName(), msg.getPassword());
				return true;
			}
			// user exists check permissions
			if (u != null) {
				// user exists but does not have the password filled
				if (u.getPassword().equals("")) {
					try {
						out.writeObject((Object) "Please fill your password");
						String password = (String) in.readObject();
						// password did not come
						if (password == null) {
							return false;
						}
						u.setPassword(password);
						// persist the user in the file users.txt
						catUsers.persisteUser(u.getName(), password);
					} catch (IOException | ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
				// user has password filled and its the same
				if (u.getPassword().equals(msg.getLocalUser().getPassword())) {
					return true;
				}
			}
			return false;
		}
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	private void pullInteraction(MessageP mp, String path, String nameFile) {
		try {

			// saca chave .key.server
			FileInputStream fis = new FileInputStream(path + nameFile + ".key.server");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] b = new byte[16];
			int len = 0;
			while ((len = fis.read(b)) != -1) {
				baos.write(b, 0, len);
			}
			fis.close();

			Path p = Paths.get(".myGitServerKeyStore");
			KeyPair kp = SecurityUtil.getKeyPairFromKS(p, "mygitserver", "badpassword1");

			Key k = null;
			Cipher decrypt = Cipher.getInstance("RSA");

			try {
				decrypt.init(Cipher.UNWRAP_MODE, kp.getPrivate());
				k = decrypt.unwrap(baos.toByteArray(), "AES", Cipher.SECRET_KEY);

			} catch (InvalidKeyException e1) {
				e1.printStackTrace();
				System.out.println("ERRO: NAO FOI POSSIVEL INICIALIZAR O DECRIPTADOR");
			}
			baos.close();

			SecretKey keyFinal = (SecretKey) k;
			// Envia a chave K para o cliente
			out.writeObject((Object) keyFinal);
		} catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}

	}

	private void sendSignature(Path f) {
		String realPath = f.toString().split(" ")[0] + ".sig";
		File realFile = new File(realPath);
		try (FileInputStream fiStream = new FileInputStream(realFile)) {
			byte[] data = new byte[(int) realFile.length()];
			fiStream.read(data);
			out.writeObject((Object) data);
		} catch (Exception e) {
		}

	}

}
