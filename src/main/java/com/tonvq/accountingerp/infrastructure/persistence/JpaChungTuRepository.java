package com.tonvq.accountingerp.infrastructure.persistence;

import com.tonvq.accountingerp.domain.model.ChungTu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for ChungTu
 * 
 * Implements the Domain Repository interface
 * Provides database operations using Spring Data JPA
 * 
 * @author Ton VQ
 */
@Repository
public interface JpaChungTuRepository extends JpaRepository<ChungTu, Long> {
    
    Optional<ChungTu> findByMaChungTu(String maChungTu);
    
    List<ChungTu> findByLoaiChungTu(String loaiChungTu);
    
    List<ChungTu> findByTrangThai(String trangThai);
    
    List<ChungTu> findByNgayChungTuBetween(LocalDate startDate, LocalDate endDate);
    
    List<ChungTu> findByLoaiChungTuAndTrangThai(String loaiChungTu, String trangThai);
    
    boolean existsByMaChungTu(String maChungTu);
    
    long countByTrangThai(String trangThai);
    
    @Query("SELECT ct FROM ChungTu ct WHERE ct.trangThai = 'APPROVED' ORDER BY ct.ngayChungTu DESC")
    List<ChungTu> findApprovedVouchers();
}
