package vn.edu.fpt.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.bookstore.entity.Address;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findAllByUser_IdAndActiveTrueOrderByDefaultAddressDescCreatedAtDesc(UUID userId);
    Optional<Address> findByIdAndUser_IdAndActiveTrue(UUID id, UUID userId);
}
