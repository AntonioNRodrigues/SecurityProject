package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import message.Message;
import message.MessageP;
import message.MessageRS;

public class ServerSkell {
	private ObjectOutputStream out;
	private ObjectInputStream in;
	//private RepositoryCatalog catRepo;
	//private UsersCatalog catUsers;
	
	public ServerSkell(ObjectOutputStream out, ObjectInputStream in) {
		this.out = out;
		this.in = in;	
	}

	public void receiveMsg(Message msg) {
		if(msg instanceof MessageRS){
			System.out.println(msg);
		}else if (msg instanceof MessageP){
			System.out.println(msg);
		}else if(msg instanceof Message){
			System.out.println(msg);
		}
		
	}
	
	
	
	
	
}
