package enums;

public enum TypeOperation {

	PUSH, PULL, REMOVE, SHARE, AUTH;
	
		public static boolean contains2(String op) {
		return TypeOperation.valueOf(op).toString().equals(op);
	}

	public static boolean contains(String operacao) {
		for (int i = 0; i < TypeOperation.values().length; i++) {
			if (TypeOperation.values()[i].toString().equals(operacao))
				return true;
		}
		return false;
	}
}
