package files;

import java.io.File;
import java.io.Serializable;
import java.sql.Timestamp;

public class Ficheiro implements Comparable<Timestamp>, Serializable {
	private static final long serialVersionUID = 1L;
	private String nameFile;
	private Timestamp timestamp;
	private File file;

	public String getNameFile() {
		return nameFile;
	}

	public void setNameFile(String nameFile) {
		this.nameFile = nameFile;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	@Override
	public int compareTo(Timestamp o) {
		return this.timestamp.compareTo(o);
	}

}
