package online.hengtian.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 陆子恒
 * @version 1.0
 * @date 2020/4/15 9:30
 */
public class User implements Serializable {

    private static final long serialVersionUID = -5792446203444901437L;
    private Integer id;
    private String username;
    private String email;
    //存储各个字段的长度，先暂时这样
    private List<Integer> lengths;
    public User() {
        lengths=new ArrayList<>();
    }

    public User(Integer id) {
        this.id = id;
    }

    public User(Integer id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Integer> getLengths() {
        return lengths;
    }

    public void setLengths(List<Integer> lengths) {
        this.lengths = lengths;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
