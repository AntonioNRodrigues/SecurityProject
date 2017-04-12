
/**
 * Grupo n.33
 * Pedro Pais, n.º 41375
 * Pedto Candido, n.º15674
 * Antonio Rodrigues n.º40853
 */
package server;

import static utilities.ReadWriteUtil.SERVER;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CopyOnWriteArrayList;

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

public class ServerSkell {
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private RepositoryCatalog catRepo;
	private UserCatalog catUsers;
	private String nonce ;

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

	public void receiveMsg(Message msg) throws ClassNotFoundException, IOException {
		catRepo.listRepos();

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
						// Validar se o utilizador é dono
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
							rr.removeUserFromRepo(mrs.getUserId());
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
						// Validar se o utilizador é dono
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
									Long timestampReceivedFile = (Long) in.readObject();
									File received = ReadWriteUtil.receiveFile(SERVER + File.separator, in, out);

									received.setLastModified(timestampReceivedFile);
									// most recent file in repository
									File fileInRepo = rr.getFile(received.getName());
									System.out.println(fileInRepo == null);

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

							File inRepo = rr.getFile(mp.getFileName());
							// client does not have the recent file so send it
							if (mp.getTimestamp() <= inRepo.lastModified()) {
								try {

									out.writeObject((Object) "OK");
									// Enviar o numero de ficheiros
									out.writeObject((Object) 1);
									// Enviar timeststamp
									out.writeObject((Object) inRepo.lastModified());
									ReadWriteUtil.sendFile(SERVER + File.separator + mp.getRepoName() + File.separator
											+ inRepo.getName(), in, out);

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
									String path = SERVER + File.separator + mp.getRepoName() + File.separator;
									String tempPath = SERVER + File.separator;
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
										System.out.println("fileInrepo != null");
									}

								} catch (ClassNotFoundException e) {
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
			// in this case since there is no pass in the system there is no way to check
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

}
