package client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import client.MessagePHandler;
import client.MessageRSHandler;
import enums.TypeOperation;

public enum MessageFactory {

	INSTANCE;

	private Map<String, IMessageTypes> msgTypes;

	/**
	 * Constructs the factory
	 */
	private MessageFactory() {
		msgTypes = new HashMap<String, IMessageTypes>();
		loadmsgTypes();
	}

	/**
	 * Loads message types
	 */
	private void loadmsgTypes() {

		msgTypes.put(TypeOperation.AUTH.toString(), new MessageAHandler());
		msgTypes.put(TypeOperation.PUSH.toString(), new MessagePHandler());
		msgTypes.put(TypeOperation.PULL.toString(), new MessagePHandler());
		msgTypes.put(TypeOperation.SHARE.toString(), new MessageRSHandler());
		msgTypes.put(TypeOperation.REMOVE.toString(), new MessageRSHandler());

	}

	/**
	 * Find message type by name
	 */
	public IMessageTypes getmsgType(String name) {
		try {
			return msgTypes.get(name);

		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @return The collection of the message translators types available
	 */
	public Set<String> msgTypesList() {
		return msgTypes.keySet();
	}

	public static MessageFactory getInstance() {
		return INSTANCE;
	}

}
