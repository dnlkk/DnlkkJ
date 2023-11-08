package com.dnlkk.repository;

import java.util.List;

public interface DnlkkRepository<K, V> {
    V findById(K id);
    List<V> findAll();
    V save(V entity);
    List<V> saveAll(List<V> entity);
    void deleteById(K id);
    Long countAll();
}
