package com.tonvq.accountingerp.domain.repository;

import com.tonvq.accountingerp.domain.model.entity.DonHang;
import com.tonvq.accountingerp.domain.model.valueobject.TrangThaiDonHang;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface: DonHang Repository
 * 
 * Pure domain interface.
 * 
 * @author Ton VQ
 */
public interface DonHangRepository {

    /**
     * Lưu đơn hàng
     */
    DonHang save(DonHang donHang);

    /**
     * Lưu nhiều đơn hàng
     */
    void saveAll(List<DonHang> donHangList);

    /**
     * Tìm đơn hàng theo ID
     */
    Optional<DonHang> findById(String id);

    /**
     * Tìm đơn hàng theo mã
     */
    Optional<DonHang> findByMaDonHang(String maDonHang);

    /**
     * Tìm tất cả đơn hàng
     */
    List<DonHang> findAll();

    /**
     * Tìm đơn hàng theo trạng thái
     */
    List<DonHang> findByTrangThai(TrangThaiDonHang trangThai);

    /**
     * Tìm đơn hàng của khách hàng
     */
    List<DonHang> findByMaKhachHang(String maKhachHang);

    /**
     * Tìm đơn hàng trong khoảng thời gian
     */
    List<DonHang> findByNgayTaoBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Tìm đơn hàng chưa thanh toán
     */
    List<DonHang> findUnpaidOrders();

    /**
     * Xóa đơn hàng
     */
    void delete(DonHang donHang);

    /**
     * Xóa đơn hàng theo ID
     */
    void deleteById(String id);

    /**
     * Đếm đơn hàng
     */
    long count();

    /**
     * Kiểm tra đơn hàng có tồn tại
     */
    boolean existsById(String id);

    /**
     * Lấy mã đơn hàng tiếp theo
     */
    String getNextMaDonHang();
}
