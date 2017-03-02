package utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ReadWriteUtil {
	private static final int VALUE = 1024;

	public static void sendFile(String filename, ObjectInputStream inStream, ObjectOutputStream outStream)
			throws IOException {
		System.out.println("SENDING FILE");
		File f = new File(filename);

		BufferedInputStream inputFileStream = new BufferedInputStream(new FileInputStream(f));
		Long sizeFile = f.length();
		// send size of file
		outStream.writeObject((Object) sizeFile);

		// send the filename
		outStream.writeObject((Object) f.getAbsolutePath());

		byte buffer[] = new byte[VALUE];
		int n = 0;

		while ((n = inputFileStream.read(buffer, 0, VALUE)) != -1) {
			outStream.write(buffer, 0, n);
		}

		inputFileStream.close();
	}

	public static File receiveFile(ObjectInputStream inStream, ObjectOutputStream outStream)
			throws ClassNotFoundException, IOException {
		System.out.println("RECEIVING FILE");
		Long sizeFile = (Long) inStream.readObject();
		String filename = (String) inStream.readObject();
		File fileReceived = new File(filename+"received");
		System.out.println(filename);
		byte[] buffer = new byte[1024];
		// change the fileoutputstream to buffreadOutoutStream
		FileOutputStream out_fileOutoutS = new FileOutputStream(fileReceived);
		int count = 0;
		int n = 0;
		long resto = 0;

		while ((n != -1) && (count <= sizeFile)) {
			resto = ((sizeFile - count) > 1024) ? 1024 : (sizeFile - count);
			n = inStream.read(buffer, 0, (int) resto);

			if (n == -1) {
				out_fileOutoutS.write(buffer, 0, (int) resto);
			} else {
				out_fileOutoutS.write(buffer, 0, n);
			}
			count += n;
		}
		out_fileOutoutS.close();
		return fileReceived;
	}

}
