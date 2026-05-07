package com.crms.domain.quality.repository;

import com.crms.domain.quality.entity.ITPScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITPScheduleItemRepository extends JpaRepository<ITPScheduleItem, Long> {

    List<ITPScheduleItem> findByScheduleId(Long scheduleId);
}
