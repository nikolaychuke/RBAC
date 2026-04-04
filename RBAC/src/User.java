import java.util.*;

public record User(String username, String fullName, String email) {

    public static User create(String username, String fullName, String email) {
        ValidationUtils.requireNonEmpty(username, "username");
        ValidationUtils.requireNonEmpty(fullName, "fullName");
        ValidationUtils.requireNonEmpty(email, "email");

        if (!ValidationUtils.isValidUsername(username)) {
            throw new IllegalArgumentException("Ошибка: username должен быть от 3 до 20 символов и содержать только латинские буквы, цифры и подчеркивание.");
        }

        if (!ValidationUtils.isValidEmail(email)) {
            throw new IllegalArgumentException("Ошибка: email должен содержать один символ @ и точку после @.");
        }

        String normalizedFullName = ValidationUtils.normalizeString(fullName);
        String normalizedEmail = ValidationUtils.normalizeString(email);

        return new User(username, normalizedFullName, normalizedEmail);
    }

    public String format() {
        return username + " (" + fullName + ") <" + email + ">";
    }

}
