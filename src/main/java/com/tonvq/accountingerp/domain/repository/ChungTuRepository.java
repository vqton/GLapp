package com.tonvq.accountingerp.domain.repository;

import com.tonvq.accountingerp.domain.model.ChungTu;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface for ChungTu (Voucher/Document)
 * 
 * Định nghĩa các phương thức truy cập dữ liệu từ Domain Layer
 * Thực hiện cụ thể trong Infrastructure Layer (JPA)
 * 
 * @author Ton VQ
 */
public interface ChungTuRepository {
    
    /**
     * Tìm chứng từ theo ID
     */
    Optional<ChungTu> findById(Long id);
    
    /**
     * Tìm chứng từ theo mã
     */
    Optional<ChungTu> findByMaChungTu(String maChungTu);
    
    /**
     * Lấy tất cả chứng từ
     */
    List<ChungTu> findAll();
    
    /**
     * Lấy chứng từ theo loại
     */
    List<ChungTu> findByLoaiChungTu(String loaiChungTu);
    
    /**
     * Lấy chứng từ theo trạng thái
     */
    List<ChungTu> findByTrangThai(String trangThai);
    
    /**
     * Lấy chứng từ trong khoảng ngày
     */
    List<ChungTu> findByNgayChungTuBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Lấy chứng từ theo loại và trạng thái
     */
    List<ChungTu> findByLoaiChungTuAndTrangThai(String loaiChungTu, String trangThai);
    
    /**
     * Lưu chứng từ (Create hoặc Update)
     */
    ChungTu save(ChungTu chungTu);
    
    /**
     * Xóa chứng từ theo ID
     */
    void deleteById(Long id);
    
    /**
     * Kiểm tra chứng từ có tồn tại không
     */
    boolean existsById(Long id);
    
    /**
     * Kiểm tra mã chứng từ đã tồn tại chưa
     */
    boolean existsByMaChungTu(String maChungTu);
    
    /**
     * Đếm số lượng chứng từ theo trạng thái
     */
    long countByTrangThai(String trangThai);
}
