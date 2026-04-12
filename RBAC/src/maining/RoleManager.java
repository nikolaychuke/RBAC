package maining;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RoleManager implements Repository<Role> {

    private final Map<String, Role> rolesById = new ConcurrentHashMap<>();
    private final Map<String, Role> rolesByName = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void add(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Роль не может быть null");
        }
        lock.writeLock().lock();
        try {
            if (rolesById.containsKey(role.getId())) {
                throw new IllegalArgumentException("Роль с id " + role.getId() + " уже есть");
            }
            if (rolesByName.containsKey(role.getName())) {
                throw new IllegalArgumentException("Роль с именем " + role.getName() + " уже есть");
            }
            rolesById.put(role.getId(), role);
            rolesByName.put(role.getName(), role);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(Role role) {
        if (role == null) return false;
        lock.writeLock().lock();
        try {
            Role removed = rolesById.remove(role.getId());
            if (removed != null) {
                rolesByName.remove(role.getName());
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Role> findById(String id) {
        if (id == null) return Optional.empty();
        lock.readLock().lock();
        try {
            return Optional.ofNullable(rolesById.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Role> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(rolesById.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int count() {
        lock.readLock().lock();
        try {
            return rolesById.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            rolesById.clear();
            rolesByName.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<Role> findByName(String name) {
        if (name == null) return Optional.empty();
        lock.readLock().lock();
        try {
            return Optional.ofNullable(rolesByName.get(name));
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean exists(String name) {
        lock.readLock().lock();
        try {
            return rolesByName.containsKey(name);
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Role> findByFilter(RoleFilter filter) {
        if (filter == null) return findAll();
        lock.readLock().lock();
        try {
            List<Role> result = new ArrayList<>();
            for (Role role : rolesById.values()) {
                if (filter.test(role)) {
                    result.add(role);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Role> findByFilterParallel(RoleFilter filter) {
        if (filter == null) return findAll();
        lock.readLock().lock();
        try {
            return rolesById.values().parallelStream()
                    .filter(filter::test)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        List<Role> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        lock.writeLock().lock();
        try {
            Role role = rolesByName.get(roleName);
            if (role == null) {
                throw new IllegalArgumentException("Роль с таким именем " + roleName + " не найдена");
            }
            role.addPermission(permission);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        lock.writeLock().lock();
        try {
            Role role = rolesByName.get(roleName);
            if (role == null) {
                throw new IllegalArgumentException("Роль с таким именем " + roleName + " не найдена");
            }
            role.removePermission(permission);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        lock.readLock().lock();
        try {
            List<Role> result = new ArrayList<>();
            for (Role role : rolesById.values()) {
                if (role.hasPermission(permissionName, resource)) {
                    result.add(role);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }
}