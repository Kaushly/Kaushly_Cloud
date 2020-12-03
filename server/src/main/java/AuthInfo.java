
public class AuthInfo {

    private String login;
    private String password;

    public String getName() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public AuthInfo(String [] args) {
        login = args[0];
        password = args[1];
    }
}
