package com.dnlkk.DITest;

import java.math.BigDecimal;

import com.dnlkk.repository.annotations.entity.Column;
import com.dnlkk.repository.annotations.entity.PK;
import com.dnlkk.repository.annotations.entity.ManyToOne;
import com.dnlkk.repository.annotations.entity.Table;

import lombok.Data;

@Data
@Table("user_operation_table")
public class UserOperation {
    @PK
    @Column("operation_id")
    private Integer operationId;

    private BigDecimal amount;

    @ManyToOne("userFromOperations")
    @Column("operation_from_user_id")
    private User from;

    @ManyToOne("userToOperations")
    @Column("operation_to_user_id")
    private User to;

    @Override
    public String toString() {
        return "UserOperation [operationId=" + operationId + ", amount=" + amount + ", from=" + (from == null ? "null" : from.getId())  + ", to=" + (to == null ? "null" : to.getId()) 
                + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserOperation other = (UserOperation) obj;
        if (operationId == null) {
            if (other.operationId != null)
                return false;
        } else if (!operationId.equals(other.operationId))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operationId == null) ? 0 : operationId.hashCode());
        return result;
    }

    
}