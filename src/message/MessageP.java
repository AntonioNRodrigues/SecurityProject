package message;

import java.io.Serializable;

import enums.TypeOperation;
import enums.TypeSend;
import user.User;

public class MessageP extends Message implements Serializable {
	private static final long serialVersionUID = 1L;
	private TypeOperation operation;
	private TypeSend typeSend;
	private String repoName;
	private String fileName;
	private int numberFiles;
	private long timestamp;
	private String password;

	/**
	 * Constructor to use when we dont want to send a file name. Using in PULL
	 * RepoName and PUSH RepoName
	 * 
	 */
	public MessageP(User localUser, String serverAdress, String password, TypeSend typeSend, String repoName,
			TypeOperation operation, int numberFiles, long ts) {
		super(localUser, serverAdress, password);
		this.repoName = repoName;
		this.fileName = null;
		this.operation = operation;
		this.typeSend = typeSend;
		this.setNumberFiles(numberFiles);
		this.timestamp = ts;
		this.password = password;
	}

	/**
	 * Constructor to use when we want to send a file name. Using in PULL
	 * fileName and PUSH FileName
	 * 
	 */
	public MessageP(User localUser, String serverAdress, String password, TypeSend typeSend, String repoName,
			String fileName, TypeOperation operation, int numberFiles, long ts) {
		super(localUser, serverAdress, password);
		this.repoName = repoName;
		this.fileName = fileName;
		this.operation = operation;
		this.typeSend = typeSend;
		this.setNumberFiles(numberFiles);
		this.timestamp = ts;
		this.password = password;
	}

	public TypeSend getTypeSend() {
		return typeSend;
	}

	public void setTypeSend(TypeSend typeSend) {
		this.typeSend = typeSend;
	}

	public TypeOperation getOperation() {
		return operation;
	}

	public void setOperation(TypeOperation operation) {
		this.operation = operation;
	}

	public int getNumberFiles() {
		return numberFiles;
	}

	public void setNumberFiles(int numberFiles) {
		this.numberFiles = numberFiles;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
