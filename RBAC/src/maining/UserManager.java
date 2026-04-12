package maining;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserManager implements Repository<User> {

    private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User не может быть null");
        }
        lock.writeLock().lock();
        try {
            if (usersByUsername.containsKey(user.username())) {
                throw new IllegalArgumentException("User с именем " + user.username() + " уже есть");
            }
            usersByUsername.put(user.username(), user);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(User user) {
        if (user == null) return false;
        lock.writeLock().lock();
        try {
            return usersByUsername.remove(user.username()) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<User> findById(String id) {
        if (id == null) return Optional.empty();
        lock.readLock().lock();
        try {
            return Optional.ofNullable(usersByUsername.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<User> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(usersByUsername.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int count() {
        lock.readLock().lock();
        try {
            return usersByUsername.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            usersByUsername.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<User> findByUsername(String username) {
        return findById(username);
    }

    public Optional<User> findByEmail(String email) {
        lock.readLock().lock();
        try {
            return usersByUsername.values().stream()
                    .filter(user -> user.email().equals(email))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<User> findByFilter(UserFilter filter) {
        if (filter == null) return findAll();
        lock.readLock().lock();
        try {
            List<User> result = new ArrayList<>();
            for (User user : usersByUsername.values()) {
                if (filter.test(user)) {
                    result.add(user);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<User> findByFilterParallel(UserFilter filter) {
        if (filter == null) return findAll();
        lock.readLock().lock();
        try {
            return usersByUsername.values().parallelStream()
                    .filter(filter::test)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        List<User> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public boolean exists(String username) {
        lock.readLock().lock();
        try {
            return usersByUsername.containsKey(username);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void update(String username, String newFullName, String newEmail) {
        lock.writeLock().lock();
        try {
            User existingUser = usersByUsername.get(username);
            if (existingUser == null) {
                throw new IllegalArgumentException("User с именем " + username + " не найден");
            }

            ValidationUtils.requireNonEmpty(newFullName, "fullName");
            ValidationUtils.requireNonEmpty(newEmail, "email");

            if (!ValidationUtils.isValidEmail(newEmail)) {
                throw new IllegalArgumentException("Ошибка: Неверный формат email");
            }

            String normalizedFullName = ValidationUtils.normalizeString(newFullName);
            String normalizedEmail = ValidationUtils.normalizeString(newEmail);

            User updatedUser = User.create(username, normalizedFullName, normalizedEmail);
            usersByUsername.put(username, updatedUser);
        } finally {
            lock.writeLock().unlock();
        }
    }
}