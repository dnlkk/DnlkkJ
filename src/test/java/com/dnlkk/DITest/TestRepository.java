package com.dnlkk.DITest;

import java.math.BigDecimal;
import java.util.List;

import com.dnlkk.dependency_injector.annotations.components.Repository;
import com.dnlkk.repository.DnlkkRepository;
import com.dnlkk.repository.annotations.Param;
import com.dnlkk.repository.annotations.Query;

@Repository
public interface TestRepository extends DnlkkRepository<Integer, User>{
    List<User> findByNameAndSurnameOrId(String name, String surname, Integer id);
    List<User> findByName(String name);
    @Query("SELECT * FROM user_table WHERE id = :userId")
    List<User> testSelect(@Param("userId") Integer userId);
    Long countAll();
    Long countByName(String name);
    BigDecimal sumEarningsByName(String name);
}