package enums;

public enum TypeOperation {
	PUSH, PULL, REMOVE, SHARE;

	public static boolean contains(String op) {
		return TypeOperation.valueOf(op).equals(op);
	}
}
