package com.dnlkk.DITest;

import com.dnlkk.dependency_injector.annotations.components.Repository;
import com.dnlkk.repository.DnlkkRepository;

import lombok.Data;

@Repository
public interface TestRepository extends DnlkkRepository<Integer, User>{
}