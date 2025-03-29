package tech.duhacks.duhacks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.duhacks.duhacks.model.MedicationSchedule;

import java.util.List;

@Repository
public interface MedicationScheduleRepo extends JpaRepository<MedicationSchedule, Long> {
    List<MedicationSchedule> findByHealthProductId(Long id);
}
