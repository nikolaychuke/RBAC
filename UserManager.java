import java.util.*;

public class UserManager implements Repository<User> {

    private final Map<String, User> usersByUsername = new HashMap<>();

    @Override
    public void add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User не может быть null");
        }
        if (usersByUsername.containsKey(user.username())) {
            throw new IllegalArgumentException("User с именем " + user.username() + " уже есть");
        }
        usersByUsername.put(user.username(), user);
    }

    @Override
    public boolean remove(User user) {
        if (user == null) return false;
        return usersByUsername.remove(user.username()) != null;
    }

    @Override
    public Optional<User> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(usersByUsername.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(usersByUsername.values());
    }

    @Override
    public int count() {
        return usersByUsername.size();
    }

    @Override
    public void clear() {
        usersByUsername.clear();
    }

    public Optional<User> findByUsername(String username) {
        return findById(username);
    }

    public Optional<User> findByEmail(String email) {
        return usersByUsername.values().stream()
                .filter(user -> user.email().equals(email))
                .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        if (filter == null) return findAll();

        List<User> result = new ArrayList<>();
        for (User user : usersByUsername.values()) {
            if (filter.test(user)) {
                result.add(user);
            }
        }
        return result;
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        List<User> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public boolean exists(String username) {
        return usersByUsername.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail) {
        User existingUser = usersByUsername.get(username);
        if (existingUser == null) {
            throw new IllegalArgumentException("User с именем " + username + " не найден");
        }

        User updatedUser = User.create(username, newFullName, newEmail);
        usersByUsername.put(username, updatedUser);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserManager that = (UserManager) o;
        return Objects.equals(usersByUsername, that.usersByUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usersByUsername);
    }
}