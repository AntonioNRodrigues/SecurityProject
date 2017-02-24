package message;

import java.io.Serializable;

import enums.TypeOperation;
import user.User;

public class MessageRS extends Message implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private TypeOperation typeOperation;
	private String repoName;
	private User userId;
	
	public MessageRS(User localUser, String serverAdress, String password, String repoName, User userid, String typeOpe) {
		super(localUser, serverAdress, password);
		this.setTypeOperation(enums.TypeOperation.valueOf(typeOpe));
		this.repoName = repoName;
		this.userId = userid;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public User getUserId() {
		return userId;
	}

	public void setUserId(User userId) {
		this.userId = userId;
	}

	public TypeOperation getTypeOperation() {
		return typeOperation;
	}

	public void setTypeOperation(TypeOperation typeOperation) {
		this.typeOperation = typeOperation;
	}
	

}
