package src.Classes;

public class User {
    private String name;
    private String password;
    private boolean state;

    public User(String name, String password, boolean state) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public boolean isState() {
        return state;
    }

    @Override
    public String toString() {
        return "User{" + "name=" + name + ", state=" + state + '}';
    }

}
