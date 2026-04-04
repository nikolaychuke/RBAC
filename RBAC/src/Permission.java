import java.util.*;

public record Permission(String name, String resource, String description) {

    public Permission {
        ValidationUtils.requireNonEmpty(name, "name");
        ValidationUtils.requireNonEmpty(resource, "resource");
        ValidationUtils.requireNonEmpty(description, "description");

        name = name.toUpperCase();
        resource = resource.toLowerCase();
        description = ValidationUtils.normalizeString(description);
    }

    public static Permission create(String name, String resource, String description) {
        return new Permission(name, resource, description);
    }

    public String format() {
        return name + " on " + resource + ": " + description;
    }

    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatches;
        boolean resourceMatches;
        
        if (namePattern == null) {
            nameMatches = true;
        } else {
            nameMatches = name.matches(namePattern);
        }
        
        if (resourcePattern == null) {
            resourceMatches = true;
        } else {
            resourceMatches = resource.matches(resourcePattern);
        }

        return nameMatches && resourceMatches;
    }
}