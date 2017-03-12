package enums;

public enum TypeOperation {

	PUSH, PULL, REMOVE, SHARE, AUTH;

	public static boolean contains(String op) {
		return TypeOperation.valueOf(op).toString().equals(op);
	}

}
