package com.layth.Library.Management.System.entities;

import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false,unique = true)
    private String userName;
    @Column(nullable = false,length = 50)
    private String password;
    @Column(nullable = false)
    private Integer roles;
    @OneToMany(mappedBy = "addedByUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Book> books;
    public User() {
    }

    public User(Integer id, String userName, String password, Integer roles) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.roles = roles;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getRoles() {
        return roles;
    }

    public void setRoles(Integer roles) {
        this.roles = roles;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public boolean hasAccessTo(UserRoles role){
        if(this.roles == -1){
            return true;
        }
        if((role.getRole() & this.roles) == role.getRole()){
            return true;
        }
        return false;
    }
}
