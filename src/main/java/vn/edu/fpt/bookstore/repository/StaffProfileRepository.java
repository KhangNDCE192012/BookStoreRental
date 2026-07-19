package vn.edu.fpt.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.bookstore.entity.StaffProfile;

import java.util.Optional;
import java.util.UUID;

public interface StaffProfileRepository extends JpaRepository<StaffProfile, UUID> {
    Optional<StaffProfile> findByUser_Id(UUID userId);
    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);
}
