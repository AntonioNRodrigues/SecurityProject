package server;

import static utilities.ReadWriteUtil.SERVER;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

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
				TypeSend ts = mp.getTypeSend();
				TypeOperation op = mp.getOperation();

				switch (ts) {
				case REPOSITORY:
					switch (op) {
					case PULL:

						boolean error = false;
						// Validar se o repositorio existe
						if (!catRepo.repoExists(mp.getRepoName())) {
							error = true;
							// System.out.println("Erro: O repositório indicado
							// não existe");
							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: O repositório indicado não existe");
						} else
							rr = catRepo.getRemRepository(mp.getRepoName());

						// Validar se o utilizador é dono ou tem acesso
						// partilhado ao repositorio
						if (!error && !(rr.getOwner().equals(mp.getLocalUser().getName())
								|| rr.getSharedUsers().contains(mp.getLocalUser().getName()))) {
							error = true;
							// System.out.println("Erro: o utilizador não tem
							// acesso ao repositório");
							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: o utilizador não tem acesso ao repositório");
						}

						if (!error) {
							List<File> filesList = rr.getFiles(mp.getRepoName());

							// System.out.println("filesList.size():
							// "+filesList.size());
							if (filesList.size() > 0) {
								try {
									out.writeObject((Object) "OK");
									// Enviar o numero de ficheiros
									out.writeObject((Integer) filesList.size());

									for (File f : filesList) {
										out.writeObject((Object) f.lastModified());
										ReadWriteUtil.sendFile(SERVER + File.separator + mp.getRepoName()
												+ File.separator + f.getName(), in, out);
									}
								} catch (IOException e) {
									e.printStackTrace();
									out.writeObject((Object) "NOK");
									out.writeObject((Object) "SERVER ERROR");
								}
							} else {
								// falta tratar do versionamento...
								// System.out.println("THE SERVER HAS NOT A
								// RECENT VERSION FOR U");
								out.writeObject((Object) "NOK");
								out.writeObject((Object) "THE SERVER HAS NOT A RECENT VERSION FOR U");
							}
						}
						break;

					// Enviar numero de ficheiros
					// System.out.println("rr.sizeUniqueFilesInMap()):
					// "+rr.sizeUniqueFilesInMap());
					// out.writeObject((Integer) rr.sizeUniqueFilesInMap());
					// Set<File> set = rr.getListMostRecentFiles();

					/*
					 * TO DO iterate over the set and send each file check a
					 * better place to do so i dont think this works inside this
					 * method WAITING FOR CLIENT TO DEAL WITH IT
					 */

					// Enviar ficheiros
					// for (File f : set) {
					// ReadWriteUtil.sendFile("SERVER"+File.separator+mp.getRepoName()+File.separator+f.getName(),
					// in, out);
					// }

					// Enviar numero de ficheiros
					// System.out.println("rr.sizeUniqueFilesInMap()):
					// "+rr.sizeUniqueFilesInMap());
					// out.writeObject((Integer) rr.sizeUniqueFilesInMap());
					// Set<File> set = rr.getListMostRecentFiles();

					/*
					 * TO DO iterate over the set and send each file check a
					 * better place to do so i dont think this works inside this
					 * method WAITING FOR CLIENT TO DEAL WITH IT
					 */

					// Enviar ficheiros
					// for (File f : set) {
					// ReadWriteUtil.sendFile("SERVER"+File.separator+mp.getRepoName()+File.separator+f.getName(),
					// in, out);
					// }

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
									File received = ReadWriteUtil.receiveFile(in, out);
									received.setLastModified(timestampReceivedFile);
									File fileInRepo = rr.getFile(mp.getRepoName(), received.getName());
									if (fileInRepo == null) {
										File f = new File(path + received.getName());
										received.renameTo(f);
										f.setLastModified(timestampReceivedFile);
										rr.getListFiles().add(f);
									} else if (fileInRepo != null) {
										long timeStampFileInRepo = fileInRepo.lastModified();
										if (timeStampFileInRepo < received.lastModified()) {
											File f = new File(path + received.getName() + ReadWriteUtil.random());
											received.renameTo(f);
											f.setLastModified(timestampReceivedFile);
											rr.getListFiles().add(f);
										} else {
											Files.deleteIfExists(received.toPath());
										}
									}
									/*
									 * String path = "SERVER" + File.separator +
									 * mp.getRepoName() + File.separator; File
									 * received =
									 * ReadWriteUtil.receiveFile(path, in, out);
									 * 
									 * if (!rr.fileExists(mp.getRepoName(),
									 * received.getName()))
									 * rr.addFile(mp.getRepoName(), received);
									 */

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
					switch (op) {
					case PULL:
						// System.out.println("-PULL FILE");
						long lastModifiedDate = mp.getTimestamp();

						boolean error = false;
						// Validar se o repositorio existe
						if (!catRepo.repoExists(mp.getRepoName())) {
							error = true;
							// System.out.println("Erro: O repositório indicado
							// não existe");
							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: O repositório indicado não existe");
						} else
							rr = catRepo.getRemRepository(mp.getRepoName());

						// Validar se o utilizador é dono ou tem acesso
						// partilhado ao repositorio
						if (!error && !(rr.getOwner().equals(mp.getLocalUser().getName())
								|| rr.getSharedUsers().contains(mp.getLocalUser().getName()))) {
							error = true;
							// System.out.println("Erro: o utilizador não tem
							// acesso ao repositório");
							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: o utilizador não tem acesso ao repositório");
						}

						// Validar se o ficheiro existe
						if (!error && !rr.fileExists(mp.getRepoName(), mp.getFileName())) {
							// System.out.println("Erro: O ficheiro indicado não
							// existe");
							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: O ficheiro indicado não existe");
						} else {

							File inRepo = rr.getFile(mp.getRepoName(), mp.getFileName());

							// client does not have the recent file so send it
							if (lastModifiedDate < inRepo.lastModified()) {
								try {

									out.writeObject((Object) "OK");
									// Enviar o numero de ficheiros
									out.writeObject((Integer) 1);
									// Enviar o ficheiro
									ReadWriteUtil.sendFile(SERVER + File.separator + mp.getRepoName() + File.separator
											+ mp.getFileName(), in, out);

								} catch (IOException e) {
									e.printStackTrace();
									out.writeObject((Object) "NOK");
									out.writeObject((Object) "SERVER ERROR");
								}
							} else {
								// System.out.println("THE SERVER HAS NOT A
								// RECENT VERSION FOR U");
								out.writeObject((Object) "NOK");
								out.writeObject((Object) "THE SERVER HAS NOT A RECENT VERSION FOR U");
							}
						}

						break;

					case PUSH:

						// Repositorio nao existe
						if (!catRepo.repoExists(mp.getRepoName())) {

							out.writeObject((Object) "NOK");
							out.writeObject((Object) "Erro: O repositório não existe");

						} else {
							// Repositorio existe

							rr = catRepo.getRemRepository(mp.getRepoName());

							// Validar se o utilizador é dono ou tem acesso
							// partilhado ao repositorio
							if (rr.getOwner().equals(mp.getLocalUser().getName())
									|| rr.getSharedUsers().contains(mp.getLocalUser().getName())) {

								out.writeObject((Object) "OK");

								// Receber ficheiro
								try {
									String path = SERVER + File.separator + mp.getRepoName() + File.separator;
									String tempPath = SERVER + File.separator;
									File received = ReadWriteUtil.receiveFile(tempPath, in, out);
									System.out.println(received);
									received.setLastModified(mp.getTimestamp());
									File fileInRepo = rr.getFile(mp.getRepoName(), mp.getFileName());
									// file does not exist add it to list
									if (fileInRepo == null) {
										// new file inside the repo
										File f = new File(path + mp.getFileName());
										// copy the received file inside the
										// server to the repoName
										received.renameTo(f);
										f.setLastModified(mp.getTimestamp());
										// File f = new
										// File(ReadWriteUtil.getRealFileName(received.getName()));
										rr.getListFiles().add(f);
										// Files.deleteIfExists(received.toPath());
									} else if (fileInRepo != null) {
										Long timeStampFileInRepo = fileInRepo.lastModified();
										if (timeStampFileInRepo < received.lastModified()) {
											// add a new version

											File f = new File(path + mp.getFileName() + ReadWriteUtil.random());
											System.out.println(f.getName());
											// pass the received file to repo
											// folder
											received.renameTo(f);
											f.setLastModified(mp.getTimestamp());
											rr.getListFiles().add(f);
											System.out.println(rr.getListFiles());
											// Files.deleteIfExists(received.toPath());
										} else {
											Files.deleteIfExists(received.toPath());
										}
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
			// System.out.println("YOU DOT NOT HAVE PREMISSIONS TO KEEP GOING
			// PLEASE CHECK PASSWORD");
		}
	}

	private boolean authentication(Message msg) {

		if (msg instanceof MessageA) {
			// caso de uso: "java myGit pedro 127.0.0.1:23456 -p badpwd1"
			MessageA m = (MessageA) msg;

			User u = catUsers.getMapUsers().get(m.getLocalUser().getName());
			// user does not exist, register user
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

}
