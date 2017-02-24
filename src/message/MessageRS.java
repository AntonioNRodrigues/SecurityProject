package message;

import java.io.Serializable;

import enums.TypeOperation;
import user.User;

public class MessageRS extends Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private TypeOperation typeOperation;
	private String repoName;
	private String userId;

	public MessageRS(User localUser, String serverAdress, String password, String repoName, String userid,
			TypeOperation typeOpe) {
		super(localUser, serverAdress, password);
		this.typeOperation = typeOpe;
		this.repoName = repoName;
		this.userId = userid;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public TypeOperation getTypeOperation() {
		return typeOperation;
	}

	public void setTypeOperation(TypeOperation typeOperation) {
		this.typeOperation = typeOperation;
	}

	@Override
	public String toString() {
		return "MessageRS [typeOperation=" + typeOperation + ", repoName=" + repoName + ", userId=" + userId + "]";
	}
	
}
