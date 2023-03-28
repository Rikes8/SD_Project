package src.Classes;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String password;
    private int id;

    public User(String name, String password, int id) {
        this.name = name;
        this.password = password;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "User{" + "name=" + name + ", id=" + id + '}';
    }

}
