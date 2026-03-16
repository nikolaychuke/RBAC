import java.util.*;

public class AssignmentManager implements Repository<RoleAssignment> {

    private final Map<String, RoleAssignment> assignmentsById = new HashMap<>();

    @Override
    public void add(RoleAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Назначение не может быть  null");
        }

        if (assignmentsById.containsKey(assignment.assignmentId())) {
            throw new IllegalArgumentException("Назначение с id " + assignment.assignmentId() + " уже есть");
        }

        if (hasActiveAssignment(assignment.user(), assignment.role())) {
            throw new IllegalArgumentException("У пользователя уже есть активное назначение на эту роль");
        }

        assignmentsById.put(assignment.assignmentId(), assignment);
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
        return assignmentsById.remove(assignment.assignmentId()) != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(assignmentsById.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignmentsById.values());
    }

    @Override
    public int count() {
        return assignmentsById.size();
    }

    @Override
    public void clear() {
        assignmentsById.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        if (user == null) return Collections.emptyList();

        List<RoleAssignment> result = new ArrayList<>();
        for (RoleAssignment a : assignmentsById.values()) {
            if (a.user().equals(user)) {
                result.add(a);
            }
        }
        return result;
    }

    public List<RoleAssignment> findByRole(Role role) {
        if (role == null) return Collections.emptyList();

        List<RoleAssignment> result = new ArrayList<>();
        for (RoleAssignment a : assignmentsById.values()) {
            if (a.role().equals(role)) {
                result.add(a);
            }
        }
        return result;
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        if (filter == null) return findAll();

        List<RoleAssignment> result = new ArrayList<>();
        for (RoleAssignment a : assignmentsById.values()) {
            if (filter.test(a)) {
                result.add(a);
            }
        }
        return result;
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        List<RoleAssignment> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public List<RoleAssignment> getActiveAssignments() {
        List<RoleAssignment> result = new ArrayList<>();
        for (RoleAssignment a : assignmentsById.values()) {
            if (a.isActive()) {
                result.add(a);
            }
        }
        return result;
    }

    public List<RoleAssignment> getExpiredAssignments() {
        List<RoleAssignment> result = new ArrayList<>();
        for (RoleAssignment a : assignmentsById.values()) {
            if (!a.isActive()) {
                result.add(a);
            }
        }
        return result;
    }

    public boolean userHasRole(User user, Role role) {
        for (RoleAssignment a : assignmentsById.values()) {
            if (a.user().equals(user) && a.role().equals(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        Set<Permission> userPermissions = getUserPermissions(user);
        for (Permission p : userPermissions) {
            if (p.name().equals(permissionName) &&
                    p.resource().equals(resource)) {
                return true;
            }
        }
        return false;
    }

    public Set<Permission> getUserPermissions(User user) {
        Set<Permission> permissions = new HashSet<>();
        for (RoleAssignment a : assignmentsById.values()) {
            if (a.user().equals(user)) {
                permissions.addAll(a.role().getPermissions());
            }
        }
        return permissions;
    }

    public void revokeAssignment(String assignmentId) {
        Optional<RoleAssignment> opt = findById(assignmentId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Назначение с id " + assignmentId + " не найдено");
        }

        RoleAssignment assignment = opt.get();

        if (!(assignment instanceof TemporaryAssignment)) {
            throw new IllegalArgumentException("Только временные назначения могут быть отозваны");
        }

        ((TemporaryAssignment) assignment).revoke();
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        Optional<RoleAssignment> opt = findById(assignmentId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Назначение с id " + assignmentId + " не найдено");
        }

        RoleAssignment assignment = opt.get();

        if (!(assignment instanceof TemporaryAssignment)) {
            throw new IllegalArgumentException("Только временные назначения могут быть продлены");
        }

        ((TemporaryAssignment) assignment).extend(newExpirationDate);
    }
}