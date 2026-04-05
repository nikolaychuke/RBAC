import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AssignmentManager implements Repository<RoleAssignment> {

    private final Map<String, RoleAssignment> assignmentsById = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void add(RoleAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Назначение не может быть null");
        }

        lock.writeLock().lock();
        try {
            if (assignmentsById.containsKey(assignment.assignmentId())) {
                throw new IllegalArgumentException("Назначение с id " + assignment.assignmentId() + " уже есть");
            }

            if (hasActiveAssignment(assignment.user(), assignment.role())) {
                throw new IllegalArgumentException("У пользователя уже есть активное назначение на эту роль");
            }

            assignmentsById.put(assignment.assignmentId(), assignment);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean hasActiveAssignment(User user, Role role) {
        for (RoleAssignment a : assignmentsById.values()) {
            if (a.user().equals(user) && a.role().equals(role) && a.isActive()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) return false;
        lock.writeLock().lock();
        try {
            return assignmentsById.remove(assignment.assignmentId()) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        if (id == null) return Optional.empty();
        lock.readLock().lock();
        try {
            return Optional.ofNullable(assignmentsById.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<RoleAssignment> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(assignmentsById.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int count() {
        lock.readLock().lock();
        try {
            return assignmentsById.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            assignmentsById.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<RoleAssignment> findByUser(User user) {
        if (user == null) return Collections.emptyList();
        lock.readLock().lock();
        try {
            List<RoleAssignment> result = new ArrayList<>();
            for (RoleAssignment a : assignmentsById.values()) {
                if (a.user().equals(user)) {
                    result.add(a);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<RoleAssignment> findByRole(Role role) {
        if (role == null) return Collections.emptyList();
        lock.readLock().lock();
        try {
            List<RoleAssignment> result = new ArrayList<>();
            for (RoleAssignment a : assignmentsById.values()) {
                if (a.role().equals(role)) {
                    result.add(a);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        if (filter == null) return findAll();
        lock.readLock().lock();
        try {
            List<RoleAssignment> result = new ArrayList<>();
            for (RoleAssignment a : assignmentsById.values()) {
                if (filter.test(a)) {
                    result.add(a);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<RoleAssignment> findByFilterParallel(AssignmentFilter filter) {
        if (filter == null) return findAll();
        lock.readLock().lock();
        try {
            return assignmentsById.values().parallelStream()
                    .filter(filter::test)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        List<RoleAssignment> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public List<RoleAssignment> getActiveAssignments() {
        lock.readLock().lock();
        try {
            List<RoleAssignment> result = new ArrayList<>();
            for (RoleAssignment a : assignmentsById.values()) {
                if (a.isActive()) {
                    result.add(a);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<RoleAssignment> getExpiredAssignments() {
        lock.readLock().lock();
        try {
            List<RoleAssignment> result = new ArrayList<>();
            for (RoleAssignment a : assignmentsById.values()) {
                if (!a.isActive()) {
                    result.add(a);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean userHasRole(User user, Role role) {
        lock.readLock().lock();
        try {
            for (RoleAssignment a : assignmentsById.values()) {
                if (a.user().equals(user) && a.role().equals(role)) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        Set<Permission> userPermissions = getUserPermissions(user);
        for (Permission p : userPermissions) {
            if (p.name().equals(permissionName) && p.resource().equals(resource)) {
                return true;
            }
        }
        return false;
    }

    public Set<Permission> getUserPermissions(User user) {
        lock.readLock().lock();
        try {
            Set<Permission> permissions = new HashSet<>();
            for (RoleAssignment a : assignmentsById.values()) {
                if (a.user().equals(user)) {
                    permissions.addAll(a.role().getPermissions());
                }
            }
            return permissions;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void revokeAssignment(String assignmentId) {
        lock.writeLock().lock();
        try {
            Optional<RoleAssignment> opt = findById(assignmentId);
            if (opt.isEmpty()) {
                throw new IllegalArgumentException("Назначение с id " + assignmentId + " не найдено");
            }

            RoleAssignment assignment = opt.get();

            if (!(assignment instanceof TemporaryAssignment)) {
                throw new IllegalArgumentException("Только временные назначения могут быть отозваны");
            }

            ((TemporaryAssignment) assignment).revoke();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        lock.writeLock().lock();
        try {
            Optional<RoleAssignment> opt = findById(assignmentId);
            if (opt.isEmpty()) {
                throw new IllegalArgumentException("Назначение с id " + assignmentId + " не найдено");
            }

            RoleAssignment assignment = opt.get();

            if (!(assignment instanceof TemporaryAssignment)) {
                throw new IllegalArgumentException("Только временные назначения могут быть продлены");
            }

            if (!ValidationUtils.isValidDate(newExpirationDate)) {
                throw new IllegalArgumentException("Ошибка: Неверный формат даты. Ожидается dd.MM.yyyy HH:mm");
            }

            ((TemporaryAssignment) assignment).extend(newExpirationDate);
        } finally {
            lock.writeLock().unlock();
        }
    }
}