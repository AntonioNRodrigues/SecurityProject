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
		this.numberFiles = numberFiles;
		this.timestamp = ts;
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
		this.numberFiles = numberFiles;
		this.timestamp = ts;
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

	@Override
	public String toString() {
		return "MessageP [repoName=" + repoName + ", fileName=" + fileName + ", operation=" + operation + ", typeSend="
				+ typeSend + ", numberFiles=" + numberFiles + ", timestamp=" + timestamp + "]";
	}

}
