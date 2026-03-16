import java.util.*;

public abstract class AbstractRoleAssignment implements RoleAssignment {

    protected final String assignmentId;
    protected final User user;
    protected final Role role;
    protected final AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        this.assignmentId = UUID.randomUUID().toString();
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override
    public String assignmentId() {
        return this.assignmentId;
    }

    @Override
    public User user() {
        return this.user;
    }

    @Override
    public Role role() {
        return this.role;
    }

    @Override
    public AssignmentMetadata metadata() {
        return this.metadata;
    }

    @Override
    public abstract boolean isActive();

    @Override
    public abstract String assignmentType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoleAssignment that = (AbstractRoleAssignment) o;
        return Objects.equals(assignmentId, that.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + assignmentId +
                ", user=" + user.username() + ", role=" + role.getName() + "}";
    }

    public String summary() {
        String status = isActive() ? "ACTIVE" : "INACTIVE";
        String type = assignmentType();
        String username = user.username();
        String roleName = role.getName();
        String assignedBy = metadata.assignedBy();
        String assignedAt = metadata.assignedAt();
        String reason = metadata.reason();

        return "[" + type + "] " + roleName + " assigned to " + username +
                " by " + assignedBy + " at " + assignedAt +
                "\nReason: " + (reason != null ? reason : "не указана") +
                "\nStatus: " + status;
    }
}