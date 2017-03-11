package enums;

public enum TypeOperation {
<<<<<<< HEAD
	PUSH, PULL, REMOVE, SHARE;

	public static boolean contains(String operacao) {
		for (int i = 0; i < TypeOperation.values().length; i++) {
			if (TypeOperation.values()[i].toString().equals(operacao))
				return true;
		}
		return false;
=======
	PUSH, PULL, REMOVE, SHARE, AUTH;

	public static boolean contains(String op) {
		return TypeOperation.valueOf(op).equals(op);
>>>>>>> branch 'master' of https://github.com/AntonioNRodrigues/SecurityProject.git
	}
}
