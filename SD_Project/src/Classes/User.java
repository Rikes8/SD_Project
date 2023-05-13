package src.Classes;

import java.io.Serializable;

/**
 * Classe usada para guardar as informções do Clientes logados/registados
 */
public class User implements Serializable {
    private String name;
    private String password;
    private int id;

    /**
     * Construtor da classe User que recebe como paramentros o nome,password e id do cliente.
     * @param name nome
     * @param password password
     * @param id cliente id
     */
    public User(String name, String password, int id) {
        this.name = name;
        this.password = password;
        this.id = id;
    }

    /**
     * Getter que retorna o nome
     * @return nome do User
     */
    public String getName() {
        return name;
    }

    /**
     * Getter que retorna a password
     * @return password do User
     */
    public String getPassword() {
        return password;
    }

    /**
     * Getter que retorna o id
     * @return id do User
     */
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "User{" + "name=" + name + ", id=" + id + '}';
    }

}
