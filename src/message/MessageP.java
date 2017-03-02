package message;

import java.io.Serializable;

import enums.TypeOperation;
import enums.TypeSend;
import user.User;

public class MessageP extends Message implements Serializable {
	private static final long serialVersionUID = 1L;
	private TypeOperation operation;
	private TypeSend typeSend;
	private String repo_file_name;
	private int numberFiles;
	
	public MessageP(User localUser, String serverAdress, String password, TypeSend typeSend, String repoFileName,
			TypeOperation operation, int numberFiles) {
		super(localUser, serverAdress, password);
		this.repo_file_name = repoFileName;
		this.operation = operation;
		this.typeSend = typeSend;
		this.setNumberFiles(numberFiles);
	}

	public TypeSend getTypeSend() {
		return typeSend;
	}

	public void setTypeSend(TypeSend typeSend) {
		this.typeSend = typeSend;
	}

	public String getRepoFileName() {
		return repo_file_name;
	}

	public void setRepoFileName(String repoFileName) {
		this.repo_file_name = repoFileName;
	}

	public TypeOperation getOperation() {
		return operation;
	}

	public void setOperation(TypeOperation operation) {
		this.operation = operation;
	}

	@Override
	public String toString() {
		return "MessageP [User " + this.getLocalUser() + "operation=" + operation + ", typeSend=" + typeSend + ", name="
				+ repo_file_name + "]";
	}

	public int getNumberFiles() {
		return numberFiles;
	}

	public void setNumberFiles(int numberFiles) {
		this.numberFiles = numberFiles;
	}

}
