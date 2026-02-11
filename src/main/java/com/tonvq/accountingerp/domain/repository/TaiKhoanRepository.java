package com.tonvq.accountingerp.domain.repository;

import com.tonvq.accountingerp.domain.model.entity.TaiKhoan;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface: TaiKhoan Repository
 * 
 * Pure domain interface.
 * 
 * @author Ton VQ
 */
public interface TaiKhoanRepository {

    /**
     * Lưu tài khoản
     */
    TaiKhoan save(TaiKhoan taiKhoan);

    /**
     * Lưu nhiều tài khoản
     */
    void saveAll(List<TaiKhoan> taiKhoanList);

    /**
     * Tìm tài khoản theo ID
     */
    Optional<TaiKhoan> findById(String id);

    /**
     * Tìm tài khoản theo mã
     */
    Optional<TaiKhoan> findByMaTaiKhoan(String maTaiKhoan);

    /**
     * Tìm tất cả tài khoản
     */
    List<TaiKhoan> findAll();

    /**
     * Tìm tài khoản theo loại
     */
    List<TaiKhoan> findByLoaiTaiKhoan(String loaiTaiKhoan);

    /**
     * Tìm tài khoản cấp 1 (TK bộ trưởng)
     */
    List<TaiKhoan> findAllCapMotMuc();

    /**
     * Tìm tài khoản con của tài khoản cha
     */
    List<TaiKhoan> findByTaiKhoanCha(String taiKhoanCha);

    /**
     * Tìm tài khoản hoạt động
     */
    List<TaiKhoan> findAllActive();

    /**
     * Xóa tài khoản
     */
    void delete(TaiKhoan taiKhoan);

    /**
     * Xóa tài khoản theo ID
     */
    void deleteById(String id);

    /**
     * Đếm tài khoản
     */
    long count();

    /**
     * Kiểm tra tài khoản có tồn tại
     */
    boolean existsById(String id);
}
