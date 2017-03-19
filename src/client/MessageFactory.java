package client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import client.MessagePHandler;
import client.MessageRSHandler;

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

		msgTypes.put("AUTH", new MessageAHandler());
		msgTypes.put("PUSH", new MessagePHandler());
		msgTypes.put("PULL", new MessagePHandler());
		msgTypes.put("SHARE", new MessageRSHandler());
		msgTypes.put("REMOVE", new MessageRSHandler());

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
