package vn.edu.fpt.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.bookstore.entity.PurchaseOrderDetail;

import java.util.UUID;

public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, UUID> {
}
