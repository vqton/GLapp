package com.tonvq.accountingerp.domain.repository;

import com.tonvq.accountingerp.domain.model.entity.TonKho;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface: TonKho Repository
 * 
 * Pure domain interface.
 * 
 * @author Ton VQ
 */
public interface TonKhoRepository {

    /**
     * Lưu tồn kho
     */
    TonKho save(TonKho tonKho);

    /**
     * Lưu nhiều tồn kho
     */
    void saveAll(List<TonKho> tonKhoList);

    /**
     * Tìm tồn kho theo ID
     */
    Optional<TonKho> findById(String id);

    /**
     * Tìm tồn kho theo mã sản phẩm
     */
    Optional<TonKho> findByMaSanPham(String maSanPham);

    /**
     * Tìm tất cả tồn kho
     */
    List<TonKho> findAll();

    /**
     * Tìm tồn kho theo kho
     */
    List<TonKho> findByMaKho(String maKho);

    /**
     * Tìm sản phẩm đang có tồn kho
     */
    List<TonKho> findActiveProducts();

    /**
     * Tìm sản phẩm hết hàng
     */
    List<TonKho> findOutOfStockProducts();

    /**
     * Xóa tồn kho
     */
    void delete(TonKho tonKho);

    /**
     * Xóa tồn kho theo ID
     */
    void deleteById(String id);

    /**
     * Đếm tồn kho
     */
    long count();

    /**
     * Kiểm tra tồn kho có tồn tại
     */
    boolean existsById(String id);
}
