package enums;

public enum TypeOperation {

	PUSH, PULL, REMOVE, SHARE, AUTH;

	public static boolean contains(String op) {
		try {
			return TypeOperation.valueOf(op).toString().equals(op);
		} catch (IllegalArgumentException e) {
			return false;
		}

	}
}
