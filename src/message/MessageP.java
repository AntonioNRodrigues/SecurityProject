package message;

import java.io.Serializable;

import enums.TypeOperation;
import enums.TypeSend;
import user.User;

public class MessageP extends Message implements Serializable{
	private static final long serialVersionUID = 1L;
	private TypeOperation operation;
	private TypeSend typeSend;
	private String name;

	public MessageP(String operation, User localUser, String serverAdress, String password, String typeSend, String name) {
		super(localUser, serverAdress, password);
		this.name = name;
		this.operation = TypeOperation.valueOf(operation);
		this.typeSend = TypeSend.valueOf(typeSend);
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

	
	
}
