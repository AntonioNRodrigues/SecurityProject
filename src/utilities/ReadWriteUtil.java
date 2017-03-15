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

public class ReadWriteUtil {
	private static final int VALUE = 1024;

	public static void sendFile(Path path, ObjectInputStream inStream, ObjectOutputStream outStream)
			throws IOException {
		System.out.println("SENDING FILE");
		File f = path.toFile();

		BufferedInputStream inputFileStream = new BufferedInputStream(new FileInputStream(f));
		System.out.println("A enviar tamanho do ficheiro...");
		Long sizeFile = f.length();
		// send size of file
		outStream.writeObject(sizeFile);
		System.out.println(f.getName());
		// send the filename
		outStream.writeObject(f.getName());

		byte buffer[] = new byte[VALUE];
		int n = 0;

		while ((n = inputFileStream.read(buffer, 0, VALUE)) != -1) {
			outStream.write(buffer, 0, n);
		}

		inputFileStream.close();
	}
		
	public static void sendFile(String filename, ObjectInputStream inStream, ObjectOutputStream outStream)
			throws IOException {
		System.out.println("SENDING FILE");
		File f = new File(filename);

		BufferedInputStream inputFileStream = new BufferedInputStream(new FileInputStream(f));
		Long sizeFile = f.length();
		// send size of file
		outStream.writeObject(sizeFile);
		System.out.println(f.getName());
		// send the filename
		outStream.writeObject(f.getName());

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
		System.out.println("Recebeu tamanho do ficheiro..." + sizeFile);
		String filename = (String) inStream.readObject();
		System.out.println("Recebeu nome do ficheiro..." + filename);
		System.err.println("Entrou aqui no receiveFile!!!!!!");
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
			System.out.println(".......");
		}
		bf.close();
		return fileReceived;
	}

}
