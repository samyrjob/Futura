package admin;

@FunctionalInterface
public interface AdminCommand {
    void execute(String message, AdminContext context);
}