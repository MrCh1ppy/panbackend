package com.example.panbackend.dao.jpa;

import com.example.panbackend.entity.po.HistoryPo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryDao extends JpaRepository<HistoryPo, Integer> {
}
