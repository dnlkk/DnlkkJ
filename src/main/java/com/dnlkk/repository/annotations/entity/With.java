package com.dnlkk.repository.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/*
    @With("SELECT COUNT(DISTINCT message_table.id)
            FROM message_table
            WHERE thread_table.id = message_table.thread_id
              AND message_table.created_date >= (CURRENT_TIMESTAMP - INTERVAL '1 day')")
    private Integer countToday;
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface With {
    String value();
    String[] include() default {};
}
