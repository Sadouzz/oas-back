package sn.oas.facturation.features.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.notification.data.entity.Notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByClientIdOrderByDateCreationDescIdDesc(Long clientId, Pageable pageable);
    long countByClientIdAndLuFalse(Long clientId);

    @Modifying
    @Query("update Notification n set n.lu = true where n.client.id = :clientId and n.lu = false")
    int markAllAsReadForClient(@Param("clientId") Long clientId);
}
