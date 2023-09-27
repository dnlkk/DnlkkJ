package com.dnlkk.WebTest.model;

import java.util.List;

import com.dnlkk.repository.annotations.entity.PK;
import com.dnlkk.repository.annotations.entity.FK;
import com.dnlkk.repository.annotations.entity.OneToMany;
import com.dnlkk.repository.annotations.entity.OneToOne;
import com.dnlkk.repository.annotations.entity.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;

@Table("user_table")
@Data
@JsonIdentityInfo(
        generator= ObjectIdGenerators.PropertyGenerator.class,
        property="id")
public class User {
    @PK
    private Integer id;
    private String name;
    private String surname;
    private Integer age;

    @OneToMany
    @FK
    @JsonIdentityReference(alwaysAsId=true)
    private List<UserOperation> userFromOperations;

    @OneToMany
    @FK
    @JsonIdentityReference(alwaysAsId=true)
    private List<UserOperation> userToOperations;

    @OneToMany
    @FK
    @JsonIdentityReference(alwaysAsId=true)
    private List<UserDoing> userDoings;

    @OneToOne()
    @FK
    @JsonIdentityReference(alwaysAsId=true)
    private UserDetails userDetails;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (surname == null) {
            if (other.surname != null)
                return false;
        } else if (!surname.equals(other.surname))
            return false;
        if (age == null) {
            if (other.age != null)
                return false;
        } else if (!age.equals(other.age))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((surname == null) ? 0 : surname.hashCode());
        result = prime * result + ((age == null) ? 0 : age.hashCode());
        return result;
    }

    
}