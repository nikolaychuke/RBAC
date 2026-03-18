import java.util.*;

public class Main {
    public static void main(String[] args) {
        RBACSystem system = new RBACSystem();
        system.initialize();
        System.out.println(system.generateStatistics());
    }
}