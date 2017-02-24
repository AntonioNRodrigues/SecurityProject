package message;

import java.io.Serializable;

import enums.TypeOperation;
import enums.TypeSend;
import user.User;

public class MessageP extends Message implements Serializable {
	private static final long serialVersionUID = 1L;
	private TypeOperation operation;
	private TypeSend typeSend;
	private String name;

	public MessageP(User localUser, String serverAdress, String password, TypeSend typeSend, String name,
			TypeOperation operation) {
		super(localUser, serverAdress, password);
		this.name = name;
		this.operation = operation;
		this.typeSend = typeSend;
	}

	public TypeSend getTypeSend() {
		return typeSend;
	}

	public void setTypeSend(TypeSend typeSend) {
		this.typeSend = typeSend;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TypeOperation getOperation() {
		return operation;
	}

	public void setOperation(TypeOperation operation) {
		this.operation = operation;
	}

	@Override
	public String toString() {
		return "MessageP [User "+this.getLocalUser() +"operation=" + operation + ", typeSend=" + typeSend + ", name=" + name + "]";
	}
	
}
