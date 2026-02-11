package com.tonvq.accountingerp.domain.repository;

import com.tonvq.accountingerp.domain.model.entity.KhachHang;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface: KhachHang Repository
 * 
 * Pure domain interface.
 * 
 * @author Ton VQ
 */
public interface KhachHangRepository {

    /**
     * Lưu khách hàng
     */
    KhachHang save(KhachHang khachHang);

    /**
     * Lưu nhiều khách hàng
     */
    void saveAll(List<KhachHang> khachHangList);

    /**
     * Tìm khách hàng theo ID
     */
    Optional<KhachHang> findById(String id);

    /**
     * Tìm khách hàng theo mã
     */
    Optional<KhachHang> findByMaKhachHang(String maKhachHang);

    /**
     * Tìm tất cả khách hàng
     */
    List<KhachHang> findAll();

    /**
     * Tìm khách hàng theo loại
     */
    List<KhachHang> findByLoaiKhachHang(String loaiKhachHang);

    /**
     * Tìm khách hàng hoạt động
     */
    List<KhachHang> findAllActive();

    /**
     * Tìm khách hàng có nợ
     */
    List<KhachHang> findAllWithDebt();

    /**
     * Xóa khách hàng
     */
    void delete(KhachHang khachHang);

    /**
     * Xóa khách hàng theo ID
     */
    void deleteById(String id);

    /**
     * Đếm khách hàng
     */
    long count();

    /**
     * Kiểm tra khách hàng có tồn tại
     */
    boolean existsById(String id);
}
