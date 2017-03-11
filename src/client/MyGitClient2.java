package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.sun.glass.ui.TouchInputSupport;

import enums.TypeOperation;
import enums.TypeSend;
import message.Message;
import message.MessageP;
import message.MessageRS;
import user.User;
import utilities.ReadWriteUtil;

public class MyGitClient2 {
	private static final int VALUE = 1024;
	private static final String HOST = "127.0.0.1";
	private static final int PORT = 23456;
	
	private static String version;
	private static File propertiesFile;
	private String operation;
	private String serverAddress;
	private String host;
	private int port;
	private String localUser;
	private String password;
	private String userId;
	private String fileName;
	private String repName;
	private String repOrFileName;
	
	
	public MyGitClient2(String[] args) {
		
		// TODO Auto-generated constructor stub
		if(!validateArgs(args)) 
			printUsage();

	}

	public static void main(String[] args) throws UnknownHostException, IOException {

		MyGitClient2 myGitClient = new MyGitClient2(args);
		String op = myGitClient.getOperation();
		System.out.println("op: "+myGitClient.operation);

		if (TypeOperation.contains(op)) {

			Socket socket = new Socket(myGitClient.getHost(), myGitClient.getPort());
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			
			//Create the message handler
			IMessageTypes mTypes = MessageFactory.INSTANCE.getmsgType(op);
			if (mTypes!=null) {
				String str = mTypes.sendMessage(in, out, myGitClient);
				System.out.println(str);
			}
			
			//if (str!=null) {
				
				
			/*
			 * Message m = null; Scanner sc = new Scanner(System.in); String str =
			 * null; do { System.out.println("COMMAND?"); str = sc.nextLine(); // do
			 * validation of params String[] array = str.split(" ");
			 * System.out.println(array.length); if(array.length == 6){ String[] a =
			 * array[4].split(":"); socket = new Socket(HOST, PORT); in = new
			 * ObjectInputStream(socket.getInputStream()); out = new
			 * ObjectOutputStream(socket.getOutputStream()); m = new Message(new
			 * User(array[2], array[3]), array[4], array[2]); System.out.println(m);
			 * out.writeObject((Object)m); }if(array.length == 7){ String[] a =
			 * array[4].split(":"); socket = new Socket(HOST, PORT); in = new
			 * ObjectInputStream(socket.getInputStream()); out = new
			 * ObjectOutputStream(socket.getOutputStream()); m = new Message(new
			 * User(array[2], array[3]), array[4], array[2]); System.out.println(m);
			 * out.writeObject((Object)m); }
			 * 
			 * } while (!(str.equalsIgnoreCase("QUIT")));
			 */

			/*Message m = new Message(new User("n", "p"), HOST + PORT, "p");
			File folder = new File("CLIENT/REP01");
			File[] listFolder = folder.listFiles();

			MessageP mp = new MessageP(new User("n"), HOST + PORT, "p", TypeSend.REPOSITORY, folder.getAbsolutePath(),
					TypeOperation.PUSH, listFolder.length, folder.lastModified());

			MessageP mp2 = new MessageP(new User("n"), HOST + PORT, "P", TypeSend.REPOSITORY, "REP01", TypeOperation.PUSH, 1, 0);
			MessageP mp4 = new MessageP(new User("n"), HOST + PORT, "P", TypeSend.FILE, "REP01", TypeOperation.PUSH, 1, 0);
			MessageP mp1 = new MessageP(new User("n", "p"), HOST + PORT, "P", TypeSend.FILE, "REP01", TypeOperation.PUSH, 1, 0);
					
			out.writeObject((Object) m);
			out.writeObject((Object)listFolder.length);
			for (File file : listFolder) {
				ReadWriteUtil.sendFile(file.getAbsolutePath(), in, out);
			}*/
				
			//}

			out.close();
			in.close();
			socket.close();		
		}
		else {
			
			

		}
	}
		
		
	public String getOperation() {
		// TODO Auto-generated method stub
		return this.operation;
	}

	public String getServerAddress() {
		// TODO Auto-generated method stub
		return serverAddress;
	}
	
	public int getPort() {
		// TODO Auto-generated method stub
		return port;
	}

	public String getHost() {
		// TODO Auto-generated method stub
		return host;
	}

	public String getLocalUser() {
		// TODO Auto-generated method stub
		return localUser;
	}
	
	public String getPassword() {
		// TODO Auto-generated method stub
		return password;
	}

	public String getUserId() {
		// TODO Auto-generated method stub
		return userId;
	}

	public String getRepName() {
		// TODO Auto-generated method stub
		return repName;
	}
	
	public String getFileName() {
		// TODO Auto-generated method stub
		return fileName;
	}
	
	public String getRepOrFileName() {
		// TODO Auto-generated method stub
		return repOrFileName;
	}
	
	public static void printUsage(){
		
		System.out.println("Invalid command, available options are: ");
		System.out.println("");
		System.out.println("Usage: myGit -init <rep_name>");
		System.out.println("Usage: myGit <localUser> <serverAddress> [ -p <password> ]");
		System.out.println("Usage: myGit <localUser> <serverAddress> [ -p <password> ] -push <rep_name>");
		System.out.println("Usage: myGit <localUser> <serverAddress> [ -p <password> ] -push <file_name>");
		System.out.println("Usage: myGit <localUser> <serverAddress> [ -p <password> ] -pull <file_name>");
		System.out.println("Usage: myGit <localUser> <serverAddress> [ -p <password> ] -pull <rep_name>");
		System.out.println("Usage: myGit <localUser> <serverAddress> [ -p <password> ] -share <rep_name> <userId>");
		System.out.println("Usage: myGit <localUser> <serverAddress> [ -p <password> ] -remove <rep_name> <userId>");		
		
		System.exit(-1);
	}


	private boolean validateArgs(String[] args){
		
		System.out.println("args.length: "+args.length);
		for (String a: args) {
			System.out.println(a);
		}
		
		int ind;
		List<String> lArgs = Arrays.asList(args);
		System.out.println("lArgs.size(): "+lArgs.size());

		
		if (lArgs.contains("-init")) {
			ind = lArgs.indexOf("-init");
			this.operation="INIT";
			
			if (lArgs.size()>=ind+1) 
				this.repName = lArgs.get(ind+1);
			System.out.println();
		}
		else if (lArgs.contains("-push")) {
			ind = lArgs.indexOf("-push");
			System.out.println("ind: "+ind);
			
			this.operation="PUSH";

			if (lArgs.size()>=ind+1) 		
				this.repOrFileName = lArgs.get(ind+1);
			else
				return false;
			
			if (!valConnArgs(lArgs, ind))
				return false;
		}
		else if (lArgs.contains("-pull")) {
			ind = lArgs.indexOf("-pull");
			this.operation="PULL";

			if (lArgs.size()>=ind+1) 		
				this.repOrFileName = lArgs.get(ind+1);
			else
				return false;
			
			if (!valConnArgs(lArgs, ind))
				return false;
			
		}
		else if (lArgs.contains("-share")) {
			ind = lArgs.indexOf("-share");
			this.operation="SHARE";
			
			if (lArgs.size()>=ind+2) { 		
				this.repName = lArgs.get(ind+1);
				this.userId = lArgs.get(ind+2);
			}
			else
				return false;
			
			if (!valConnArgs(lArgs, ind))
				return false;
			
		}
		else if (lArgs.contains("-remove")) {
			ind = lArgs.indexOf("-remove");
			this.operation="REMOVE";
			
			if (lArgs.size()>=ind+2) { 		
				this.repName = lArgs.get(ind+1);
				this.userId = lArgs.get(ind+2);
			}
			else
				return false;
			
			if (!valConnArgs(lArgs, ind))
				return false;
		}
		else {
			
			if (lArgs.size()==2) {
				this.localUser=lArgs.get(0);
				this.serverAddress=lArgs.get(1);
			}
			else if (lArgs.size()==4) {
				this.localUser=lArgs.get(0);
				this.serverAddress=lArgs.get(1);
				this.password=lArgs.get(3);
			}
			else
				return false;
			
		}
		
		System.out.println("localUser: "+this.localUser);
		System.out.println("serverAddress: "+this.serverAddress);
		System.out.println("host: "+this.host);
		System.out.println("port: "+this.port);
		System.out.println("password: "+this.password);
		System.out.println("operation: "+this.operation);
		System.out.println("repName: "+this.repName);
		System.out.println("fileName: "+this.fileName);
		System.out.println("repOrFileName: "+this.repOrFileName);
		System.out.println("userId: "+this.userId);
		
		
		return true;
	}

	private boolean valConnArgs(List<String> lArgs, int ind){
		if (ind==2) {
			this.localUser=lArgs.get(0);
			this.serverAddress=lArgs.get(1);
			String[] srvAdr = this.serverAddress.split(":");
			this.host = srvAdr[0];
			this.port = Integer.parseInt(srvAdr[1]);
		}
		else if (ind==4) {
			this.localUser=lArgs.get(0);
			this.serverAddress=lArgs.get(1);
			this.password=lArgs.get(3);
			String[] srvAdr = this.serverAddress.split(":");
			this.host = srvAdr[0];
			this.port = Integer.parseInt(srvAdr[1]);
		}
		else {
			System.out.println("Error: valConnArgs");
			return false;
		}
		return true;
	}

	    public static void checkPropertiesFile() {
	    	
	        OutputStream output = null;	        
	    	String userHomeDir = System.getProperty("user.home");
	    	String fullPathFileName = userHomeDir + File.separator + ".myGit";
	        propertiesFile = new File(fullPathFileName);
	        
	        //create properties file if doesn't exist
	        if  (!propertiesFile.exists())
				try {
					output = new FileOutputStream(propertiesFile);
					output.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	    }

	    public static String getProperty(String propertyName) {
	        
	    	checkPropertiesFile();
	    	
	    	Properties prop = new Properties();
	        InputStream input = null;
	        String propertyValue = null;

	        try {
	            input = new FileInputStream(propertiesFile);
	            prop.load(input);

	            propertyValue = prop.getProperty(propertyName);

	        } catch (IOException ex) {
	            ex.printStackTrace();
	        } finally {
	            if (input != null) {
	                try {
	                    input.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
			return propertyValue;

	    }

	    public static void setProperty(String propertyName, String propertyValue)
	    {
	    	checkPropertiesFile();

	        Properties prop = new Properties();
	        OutputStream output = null;
	        
	    try {

	        output = new FileOutputStream(propertiesFile);

	        String currentPropertyValues=null;
	        currentPropertyValues=getProperty(propertyName);
	        if (currentPropertyValues!=null)
	        	prop.setProperty(propertyName, currentPropertyValues+","+propertyValue);
	        else
	        	prop.setProperty(propertyName, propertyValue);

	        prop.store(output, null);

	    } catch (IOException io) {
	        io.printStackTrace();
	    } finally {
	        if (output != null) {
	            try {
	                output.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	   }
	

			
	
}