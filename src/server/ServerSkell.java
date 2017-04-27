
/**
 * Grupo n.33
 * Pedro Pais, n.º 41375
 * Pedto Candido, n.º15674
 * Antonio Rodrigues n.º40853
 */
package server;

import static utilities.ReadWriteUtil.SERVER;

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
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
							rr.addShareUserToRepo(mrs.getUserId());
						}
					}

					break;
				default:
					break;
				}

			} else if (msg instanceof MessageP) {
				// PublicKey publicKeyOfUser = null;
				/*
				 * boolean variable to use if the user != owner set to true else
				 * set it to false
				 */
				// boolean difUser = false;
				MessageP mp = ((MessageP) msg);
				TypeSend typeSend = mp.getTypeSend();
				TypeOperation operation = mp.getOperation();
				// RemoteRepository remoRepo =
				// catRepo.getRemRepository(mp.getRepoName());
				// if the user is not the owner of the repo and has permissions
				// to do stuff to the repo

				// if
				// (!(remoRepo.getOwner().equals(mp.getLocalUser().getName()))
				// &&
				// remoRepo.getSharedPublicKey().containsKey(mp.getLocalUser().getName()))
				// {
				// this is the public key of the user which has access to the
				// repo
				// if its push repo or file --> the user chipher the content of
				// repo or file with his privateKey
				// sends to the server and the server uses the publickey to
				// decipher the content

				// in the case od pull repo or file the server ciphers the
				// content with the public key of the user
				// and sends it to the user. has to decipher the content with
				// its private key
				// publicKeyOfUser = receiveMsgDifferentOwner(mp);
				// if(publicKeyOfUser == null){
				// out.writeObject("You do not have access to this operation");
				// }

				// }

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
							// the user has the access to the repo but does not
							// have set is publicKey
						} /*
							 * else if (rr.getSharedPublicKey().containsKey(mp.
							 * getLocalUser().getName()) &&
							 * (rr.getSharedPublicKey().get(mp.getLocalUser().
							 * getName()) == null)) { //ask the user to send the
							 * key publicKeyOfUser =
							 * receiveMsgDifferentOwner(mp);
							 * 
							 * }
							 */

						if (!error) {

							CopyOnWriteArrayList<Path> uniqueList = rr.getUniqueList();

							if (uniqueList.size() > 0) {
								try {
									out.writeObject((Object) "OK");
									// Enviar o numero de ficheiros
									out.writeObject((Integer) uniqueList.size());

									for (Path f : uniqueList) {
										out.writeObject((Object) f.toFile().lastModified());
										ReadWriteUtil.sendFile(SERVER + File.separator + mp.getRepoName()
												+ File.separator + f.toFile().getName(), in, out);
									}
								} catch (IOException e) {
									e.printStackTrace();
									out.writeObject((Object) "NOK");
									out.writeObject((Object) "SERVER ERROR");
								}
							} else {
								out.writeObject((Object) "NOK");
								out.writeObject(
										(Object) "O repositório local está sincronizado, não existe nenhuma versão mais recente dos ficheiros.");
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

						// Validar se o utilizador é dono ou tem acesso
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
									System.out.println("--" + signature);
									System.out.println("--" + mp.getFileName()); // this
																					// one
																					// is
																					// null
									System.out.println("--" + path);
									FileOutputStream fos = new FileOutputStream(path + mp.getFileName() + ".sig");
									fos.write(signature);

									fos.close();

									// Recebe chave key para depois cifra-la
									// usando a sua chave publica
									SecretKey key = (SecretKey) in.readObject();

									// TODO: CORRIGIR ESTA PARTE: Cifra a chave
									// com a chave p�blica, usando uma keytool
									// (ATEN�AO... AS KEYTOOLS foram criadas
									// antes?!)
									// cifrar chave AES com chave publica
									/*
									 * 1- buscar chave publica -> keystore 2 -
									 * cifrar chave publica
									 */
									Path p = Paths.get(".myGitServerKeyStore");

									KeyPair kp = SecurityUtil.getKeyPairFromKS(p, "mygitserver", "badpassword1");

									// method to get the certificate from
									// keystore

									Certificate cert = SecurityUtil.getCertFromKeyStore(p, "mygitserver",
											"badpassword1");

									Cipher cif = Cipher.getInstance("RSA");
									cif.init(Cipher.WRAP_MODE, cert);
									byte[] chaveCifrada = cif.wrap(key);

									byte[] keyEncoded = key.getEncoded();
									FileOutputStream kos = new FileOutputStream(
											path + mp.getFileName() + ".key.server");
									kos.write(chaveCifrada);
									kos.close();

									Long timestampReceivedFile = (Long) in.readObject();
									File received = ReadWriteUtil.receiveFile(SERVER + File.separator, in, out);

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

								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								} catch (InvalidKeyException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IllegalBlockSizeException e) {
									// TODO Auto-generated catch block
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

						// Validar se o utilizador é dono ou tem acesso
						// partilhado ao repositorio
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

							String path = SERVER + File.separator + mp.getRepoName() + File.separator;
							// Saca da chave do ficheiro que est� guardada com a

							// extens�o .key.server
							FileInputStream keyFile = new FileInputStream(path + mp.getFileName() + ".key.server");
							byte[] key = new byte[16];
							keyFile.read(key);

							// Vai � Keystore para buscar a sua chave privada
							// e
							// decripta a chave.
							Path p = Paths.get(".myGitServerKeyStore");
							KeyPair kp = SecurityUtil.getKeyPairFromKS(p, "mygitserver", "badpassword1");
							PrivateKey chaveParaDecifrar = kp.getPrivate();

							Cipher decrypt = Cipher.getInstance("AES");

							byte[] chaveDecifrada = new byte[16];
							try {
								decrypt.init(Cipher.DECRYPT_MODE, chaveParaDecifrar);

								chaveDecifrada = decrypt.doFinal(key);
							} catch (InvalidKeyException e1) {
								e1.printStackTrace();
								System.out.println("ERRO: N�O FOI POSS�VEL INICIALIZAR O DECRIPTADOR");
							} catch (IllegalBlockSizeException e) {
								System.out.println("ERRO: O TAMANHO DO ARRAY N�O � O MAIS CORRECTO");
							} catch (BadPaddingException e) {
								e.printStackTrace();
							}

							// Convert byte[] to Secret Key
							SecretKey keyFinal = new SecretKeySpec(chaveDecifrada, 0, chaveDecifrada.length, "AES");

							// Envia a chave K para o cliente
							out.writeObject(keyFinal);

							// Vai buscar a assinatura e envia para o cliente
							ObjectInputStream ois = new ObjectInputStream(
									new FileInputStream(path + mp.getFileName() + ".sig"));
							String data = (String) ois.readObject();
							out.writeObject(data);
							ois.close();

							// In�cio do envio do ficheiro cifrado
							File inRepoCifrado = rr.getFile(mp.getFileName());

							// client does not have the recent file so send it
							if (mp.getTimestamp() <= inRepoCifrado.lastModified()) {
								try {

									out.writeObject((Object) "OK");
									// Enviar o numero de ficheiros
									out.writeObject((Object) 1);
									// Enviar timeststamp
									out.writeObject((Object) inRepoCifrado.lastModified());
									ReadWriteUtil.sendFile(SERVER + File.separator + mp.getRepoName() + File.separator
											+ inRepoCifrado.getName(), in, out);

								} catch (IOException e) {
									e.printStackTrace();
									out.writeObject((Object) "NOK");
									out.writeObject((Object) "SERVER ERROR");
								}
							} else {
								out.writeObject((Object) "NOK");
								out.writeObject((Object) "Não existe nenhuma versão mais recente do ficheiro.");
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

									FileOutputStream ass = new FileOutputStream(path + mp.getFileName() + ".sig");
									ass.write(signature);
									ass.close();

									// Recebe chave key para depois cifra-la
									// usando a sua chave publica
									SecretKey key = (SecretKey) in.readObject();

									// method to get the certificate from
									// keystore

									Certificate cert = SecurityUtil.getCertFromKeyStore(
											Paths.get(".myGitServerKeyStore"), "mygitserver", "badpassword1");

									Cipher cif = Cipher.getInstance("RSA");
									cif.init(Cipher.WRAP_MODE, cert);
									byte[] chaveCifrada = cif.wrap(key);

									FileOutputStream kos = new FileOutputStream(
											path + mp.getFileName() + ".key.server");
									kos.write(chaveCifrada);
									kos.close();

									// Recebe ficheiro
									File received = ReadWriteUtil.receiveFile(tempPath, in, out);
									received.setLastModified(mp.getTimestamp());
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
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IllegalBlockSizeException e) {
									// TODO Auto-generated catch block
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
					out.writeObject((Object) "-- O  utilizador " + m.getLocalUser().getName() + " foi criado");
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
				System.out.println(mdCompare);
				if (mdCompare) {
					try {
						out.writeObject((Object) "OK");
						out.writeObject((Object) "-- O  utilizador " + m.getLocalUser().getName() + " foi autenticado");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// user has password filled and its the same

				if (u.getPassword().equals(m.getLocalUser().getPassword())) {
					try {
						out.writeObject((Object) "OK");
						out.writeObject((Object) "-- O  utilizador " + m.getLocalUser().getName() + " foi autenticado");
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						out.writeObject((Object) "NOK");
						out.writeObject((Object) "Erro: O  utilizador " + m.getLocalUser().getName()
								+ " introduziu uma password inválida");
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

	public PublicKey receiveMsgDifferentOwner(MessageP mp) {
		PublicKey pk = null;
		RemoteRepository rr = catRepo.getRemRepository(mp.getLocalUser().getName());
		// if the user does not have the public key in the sharedPublicLey Map
		if (rr.getSharedPublicKey().get(mp.getLocalUser().getName()) == null) {
			// ask for key
			try {
				out.writeObject((Object) "Please give me your public key");
				byte[] publicKey = (byte[]) in.readObject();
				// buildKey from the byte[] --> pk =
				// rebuildPublicKey(publicKey);
				// if (pk == null){
				// out.writeObject((Object) "You did not provide a valid public
				// key");
				// }
				// add publicKey to the sharedPublicKey Map
				// return pk;

			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			pk = rr.getSharedPublicKey().get(mp.getLocalUser().getName());
		}
		return pk;
	}

}
