package com.dnlkk.DITest;

import com.dnlkk.dependency_injector.annotations.components.Repository;
import com.dnlkk.repository.DnlkkRepository;

import lombok.Data;


public interface TestRepository extends DnlkkRepository<Long, User>{
}