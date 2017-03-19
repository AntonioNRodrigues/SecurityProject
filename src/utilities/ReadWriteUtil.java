package utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.Random;

public class ReadWriteUtil {
	private static final int VALUE = 1024;
	public static final String SERVER = "SERVER";
	public static final String OWNER = "owner.txt";
	public static final String SHARED = "shared.txt";
	public static final String USERS = "users.txt";

	public static void sendFile(Path path, ObjectInputStream inStream, ObjectOutputStream outStream)
			throws IOException {
		File f = path.toFile();

		BufferedInputStream inputFileStream = new BufferedInputStream(new FileInputStream(f));
		Long sizeFile = f.length();
		// send size of file
		outStream.writeObject((Object) sizeFile);
		System.out.println(f.getName());
		// send the filename
		outStream.writeObject((Object) f.getName());

		byte buffer[] = new byte[VALUE];
		int n = 0;

		while ((n = inputFileStream.read(buffer, 0, VALUE)) != -1) {
			outStream.write(buffer, 0, n);
		}

		inputFileStream.close();
	}

	public static void sendFile(String filename, ObjectInputStream inStream, ObjectOutputStream outStream)
			throws IOException {
		File f = new File(filename);

		BufferedInputStream inputFileStream = new BufferedInputStream(new FileInputStream(f));
		Long sizeFile = f.length();
		// send size of file
		outStream.writeObject((Object) sizeFile);
		System.out.println(f.getName());
		// send the filename
		outStream.writeObject((Object) f.getName());

		byte buffer[] = new byte[VALUE];
		int n = 0;

		while ((n = inputFileStream.read(buffer, 0, VALUE)) != -1) {
			outStream.write(buffer, 0, n);
		}

		inputFileStream.close();
	}

	public static File receiveFile(String path, ObjectInputStream inStream, ObjectOutputStream outStream)
			throws ClassNotFoundException, IOException {

		Long sizeFile = (Long) inStream.readObject();
		String filename = (String) inStream.readObject();

		File fileReceived = new File(path + filename);
		BufferedOutputStream bf = new BufferedOutputStream(new FileOutputStream(fileReceived));
		int len = 0;
		byte[] buffer = new byte[VALUE];
		int lido;

		while (len < sizeFile) {
			int resto = (int) (sizeFile - len);
			int n = (resto < VALUE) ? resto : buffer.length;
			lido = inStream.read(buffer, 0, n);

			if (lido == -1) {
				break;
			}

			bf.write(buffer, 0, n);
			len += lido;
		}
		bf.close();

		return fileReceived;
	}
	public static File receiveFile(ObjectInputStream inStream, ObjectOutputStream outStream)
			throws ClassNotFoundException, IOException {

		Long sizeFile = (Long) inStream.readObject();
		String filename = (String) inStream.readObject();

		File fileReceived = new File(filename);
		BufferedOutputStream bf = new BufferedOutputStream(new FileOutputStream(fileReceived));
		int len = 0;
		byte[] buffer = new byte[VALUE];
		int lido;

		while (len < sizeFile) {
			int resto = (int) (sizeFile - len);
			int n = (resto < VALUE) ? resto : buffer.length;
			lido = inStream.read(buffer, 0, n);

			if (lido == -1) {
				break;
			}

			bf.write(buffer, 0, n);
			len += lido;
		}
		bf.close();

		return fileReceived;
	}

	public static String random() {
		return " (" + (new Random().nextInt(900) + 100) + ")";
	}

	public static String getRealFileName(String f) {
		return f.split(" ")[0];
	}
}
