import java.util.*;

public record User(String username, String fullName, String email) {

    public static User create(String username, String fullName, String email) {
        if (username == null) {
            throw new IllegalArgumentException("Ошибка: username не может быть null");
        }
        if (fullName == null) {
            throw new IllegalArgumentException("Ошибка: fullName не может быть null");
        }
        if (email == null) {
            throw new IllegalArgumentException("Ошибка: email не может быть null");
        }


        if (username.trim().isEmpty()) {
            throw new IllegalArgumentException("Ошибка: username не может быть пустым");
        }
        if (fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Ошибка: fullName не может быть пустым");
        }
        if (email.trim().isEmpty()) {
            throw new IllegalArgumentException("Ошибка: email не может быть пустым");
        }


        if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
            throw new IllegalArgumentException("Ошибка: username должен быть от 3 до 20 символов и содержать только латинские буквы, цифры и подчеркивание.");
        }

        if (!email.matches("^[a-zA-Z0-9_]+@{1}[a-zA-Z]+\\.[a-zA-Z]+$")) {
            throw new IllegalArgumentException("Ошибка: email должен содержать один символ @ и точку после @.");
        }

        return new User(username, fullName, email);
    }

    public String format() {
        return username + " (" + fullName + ") <" + email + ">";
    }

    public static void main(String[] args) {
        System.out.println("ТЕСТ 1: user record (с regex)\n");

        System.out.println("\n1. Создание валидного пользователя:");
        try {
            User u = User.create("egor_nik", "Egor Nikolaychuk", "nikolaychuk@example.com");
            System.out.println(u.format());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n2. Короткий username (\"eg\"):");
        try {
            User.create("eg", "Egor Nikolaychuk", "nikolaychuk@example.com");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n3. Длинный username (\"asdfasdfasdfasfsadffsdfsdfsd\"):");
        try {
            User.create("asdfasdfasdfasfsadffsdfsdfsd", "Egor Nikolaychuk", "nikolaychuk@example.com");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n4. Недопустимый символ (\"egor_nik@\"):");
        try {
            User.create("egor_nik@", "Egor Nikolaychuk", "nikolaychuk@example.com");

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        
        System.out.println("\n5. Русская буква (\"egor_nikА\"):");
        try {
            User.create("egor_nikА", "Egor Nikolaychuk", "nikolaychuk@example.com");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n6. Email без @ (\"nikolaychukexample.com\"):");
        try {
            User.create("egor_nik", "Egor Nikolaychuk", "nikolaychukexample.com");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n7. Email без точки (\"nikolaychuk@examplecom\"):");
        try {
            User.create("egor_nik", "Egor Nikolaychuk", "nikolaychuk@examplecom");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n8. Email с двумя @ (\"nikolaychuk@@example.com\"):");
        try {
            User.create("egor_nik", "Egor Nikolaychuk", "nikolaychuk@@example.com");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n9. Все поля null:");
        try {
            User.create(null, null, null);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n10. Все поля пустые (\"\", \"\", \"\"):");
        try {
            User.create("", "", "");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        User user1 = User.create("egor_nik", "Egor Nikolaychuk", "nikolaychuk@example.com");
        User user2 = User.create("egor_nik", "Egor Nikolaychuk", "nikolaychuk@example.com");
        User user3 = User.create("User", "User Test", "user@example.com");

        System.out.println("\n\nТЕСТ 2: equals\n");
        System.out.println("user1.equals(user2): " + user1.equals(user2));
        System.out.println("user1.equals(user3): " + user1.equals(user3));

        System.out.println("\nТЕСТ 3: hashCode\n");
        System.out.println("hashCode user1: " + user1.hashCode());
        System.out.println("hashCode user2: " + user2.hashCode());
        System.out.println("hashCode user3: " + user3.hashCode());

    }
}
