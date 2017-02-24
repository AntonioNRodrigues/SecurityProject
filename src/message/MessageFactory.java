package message;

import enums.TypeOperation;
import enums.TypeSend;
import user.User;

public class MessageFactory {

	public Message getMessage(User localUser, String serverAdress, String password) {
		return new Message(localUser, serverAdress, password);

	}

	public MessageP getMessageP(User localUser, String serverAdress, String password, String operation,
			String repo_file_name, String typeSend) {
		return new MessageP(localUser, serverAdress, password, typeSend(typeSend), repo_file_name,
				operation(operation));

	}

	public MessageRS getMessageRS(User localUser, String serverAdress, String password, String opertaion,
			String repo_name, String userId) {
		return new MessageRS(localUser, serverAdress, password, repo_name, userId, operation(opertaion));
	}

	private TypeOperation operation(String op) {
		TypeOperation typeOpe = null;
		if (op.equals(TypeOperation.PULL)) {
			typeOpe = TypeOperation.PULL;
		} else if (op.equals(TypeOperation.PUSH)) {
			typeOpe = TypeOperation.PUSH;
		} else if (op.equals(TypeOperation.REMOVE)) {
			typeOpe = TypeOperation.REMOVE;
		} else if (op.equals(TypeOperation.SHARE)) {
			typeOpe = TypeOperation.SHARE;
		}
		return typeOpe;
	}

	private TypeSend typeSend(String typeSend) {
		return (typeSend.equals(TypeSend.FILE)) ? TypeSend.FILE : TypeSend.REPOSITORY;
	}

}
