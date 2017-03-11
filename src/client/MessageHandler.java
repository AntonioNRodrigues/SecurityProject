package client;

public abstract class MessageHandler implements IMessageTypes {
	
	/**
	 * The name of the message handler 
	 */
	private String name;
    
	/**
	 * Constructs a message handler given its name
	 * 
	 * @param name The name of the handler
	 * @return 
	 */
	public MessageHandler (String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return this.name;
	}

}