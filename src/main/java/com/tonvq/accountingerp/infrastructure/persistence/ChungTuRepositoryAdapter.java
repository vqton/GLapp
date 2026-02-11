package com.tonvq.accountingerp.infrastructure.persistence;

import com.tonvq.accountingerp.domain.model.ChungTu;
import com.tonvq.accountingerp.domain.repository.ChungTuRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ChungTu Repository Adapter
 * 
 * Implements Domain Repository using Spring Data JPA
 * Translates Domain calls to JPA Repository calls
 * 
 * @author Ton VQ
 */
@Component
public class ChungTuRepositoryAdapter implements ChungTuRepository {
    
    private final JpaChungTuRepository jpaRepository;
    
    public ChungTuRepositoryAdapter(JpaChungTuRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Optional<ChungTu> findById(Long id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public Optional<ChungTu> findByMaChungTu(String maChungTu) {
        return jpaRepository.findByMaChungTu(maChungTu);
    }
    
    @Override
    public List<ChungTu> findAll() {
        return jpaRepository.findAll();
    }
    
    @Override
    public List<ChungTu> findByLoaiChungTu(String loaiChungTu) {
        return jpaRepository.findByLoaiChungTu(loaiChungTu);
    }
    
    @Override
    public List<ChungTu> findByTrangThai(String trangThai) {
        return jpaRepository.findByTrangThai(trangThai);
    }
    
    @Override
    public List<ChungTu> findByNgayChungTuBetween(LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByNgayChungTuBetween(startDate, endDate);
    }
    
    @Override
    public List<ChungTu> findByLoaiChungTuAndTrangThai(String loaiChungTu, String trangThai) {
        return jpaRepository.findByLoaiChungTuAndTrangThai(loaiChungTu, trangThai);
    }
    
    @Override
    public ChungTu save(ChungTu chungTu) {
        return jpaRepository.save(chungTu);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
    
    @Override
    public boolean existsByMaChungTu(String maChungTu) {
        return jpaRepository.existsByMaChungTu(maChungTu);
    }
    
    @Override
    public long countByTrangThai(String trangThai) {
        return jpaRepository.countByTrangThai(trangThai);
    }
}
