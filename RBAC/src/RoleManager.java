import java.util.*;

public class RoleManager implements Repository<Role> {

    private final Map<String, Role> rolesById = new HashMap<>();
    private final Map<String, Role> rolesByName = new HashMap<>();

    @Override
    public void add(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Роль не может быть null");
        }
        if (rolesById.containsKey(role.getId())) {
            throw new IllegalArgumentException("Роль с id " + role.getId() + " уже есть");
        }
        if (rolesByName.containsKey(role.getName())) {
            throw new IllegalArgumentException("Роль с именем " + role.getName() + " уже есть");
        }

        rolesById.put(role.getId(), role);
        rolesByName.put(role.getName(), role);
    }

    @Override
    public boolean remove(Role role) {
        if (role == null) return false;

        Role removed = rolesById.remove(role.getId());
        if (removed != null) {
            rolesByName.remove(role.getName());
            return true;
        }
        return false;
    }

    @Override
    public Optional<Role> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(rolesById.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    public Optional<Role> findByName(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(rolesByName.get(name));
    }

    public boolean exists(String name) {
        return rolesByName.containsKey(name);
    }

    public List<Role> findByFilter(RoleFilter filter) {
        if (filter == null) return findAll();

        List<Role> result = new ArrayList<>();
        for (Role role : rolesById.values()) {
            if (filter.test(role)) {
                result.add(role);
            }
        }
        return result;
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        List<Role> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Роль с таким именем " + roleName + " не найдена");
        }
        role.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Роль с таким именем " + roleName + " не найдена");
        }
        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        List<Role> result = new ArrayList<>();
        for (Role role : rolesById.values()) {
            if (role.hasPermission(permissionName, resource)) {
                result.add(role);
            }
        }
        return result;
    }
}