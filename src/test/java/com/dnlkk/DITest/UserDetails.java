package com.dnlkk.DITest;

import com.dnlkk.repository.annotations.entity.Column;
import com.dnlkk.repository.annotations.entity.Id;
import com.dnlkk.repository.annotations.entity.OneToOne;
import com.dnlkk.repository.annotations.entity.Table;

import lombok.Data;

@Data
@Table("user_details_table")
public class UserDetails {
    @Id
    @Column("details_id")
    private long id;

    private String email;

    @OneToOne("userDetails")
    @Column("details_user_id")
    private User user;

    @Override
    public String toString() {
        return "UserDetails [id=" + id + ", email=" + email + ", user=" + (user == null ? "null" : user.getId()) + "]";
    }

    
}