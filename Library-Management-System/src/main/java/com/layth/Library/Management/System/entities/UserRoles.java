package com.layth.Library.Management.System.entities;

public enum UserRoles {
    AllRoles(-1),
    ManageLibrarians(1),
    ManageBooks(2),
    ManagePatrons(4);

    private Integer role;

    UserRoles(Integer role){
        this.role = role;
    }

    public Integer getRole() {
        return role;
    }
}
