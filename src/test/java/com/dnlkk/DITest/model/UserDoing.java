package com.dnlkk.DITest.model;

import com.dnlkk.repository.annotations.entity.Column;
import com.dnlkk.repository.annotations.entity.PK;
import com.dnlkk.repository.annotations.entity.ManyToOne;
import com.dnlkk.repository.annotations.entity.Table;

import lombok.Data;

@Table("user_doings_table")
@Data
public class UserDoing {
    @PK
    @Column("doings_id")
    private Integer id;
    private String doing;

    @ManyToOne("userDoings")
    @Column("doings_user_id")
    private User user;

    @Override
    public String toString() {
        return "UserDoing [id=" + id + ", doing=" + doing + ", user=" + (user == null ? "null" : user.getId()) + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserDoing other = (UserDoing) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    
}
