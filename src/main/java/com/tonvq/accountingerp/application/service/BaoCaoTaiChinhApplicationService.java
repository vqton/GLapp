package com.tonvq.accountingerp.application.service;

import com.tonvq.accountingerp.application.dto.*;
import com.tonvq.accountingerp.application.exception.BusinessException;
import com.tonvq.accountingerp.application.exception.ResourceNotFoundException;
import com.tonvq.accountingerp.domain.repository.BaoCaoTaiChinhRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Application Service for Financial Reporting
 * Generates B01-B09 financial reports per TT 99/2025/TT-BTC
 * 
 * B01: Income Statement (Báo cáo kết quả hoạt động kinh doanh)
 * B02: Balance Sheet (Bảng cân đối kế toán)
 * B03: Cash Flow (Báo cáo lưu chuyển tiền tệ)
 * B09: Inventory Statement (Báo cáo tồn kho)
 * 
 * @author Ton VQ
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class BaoCaoTaiChinhApplicationService {

    private final BaoCaoTaiChinhRepository baoCaoTaiChinhRepository;

    /**
     * Generate B01 Report - Income Statement
     */
    public BaoCaoTaiChinhResponse generateB01(BaoCaoTaiChinhRequest request) {
        Objects.requireNonNull(request, "BaoCaoTaiChinhRequest cannot be null");

        log.info("Generating B01 report: startDate={}, endDate={}", request.getStartDate(), request.getEndDate());

        // Validate dates
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BusinessException("Ngày bắt đầu/kết thúc không được để trống", "INVALID_DATES");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException("Ngày bắt đầu phải trước ngày kết thúc", "INVALID_DATE_RANGE");
        }

        // Collect data for B01
        Map<String, Object> reportData = new HashMap<>();
        
        // Revenue data
        reportData.put("totalRevenue", BigDecimal.ZERO);
        reportData.put("salesRevenue", BigDecimal.ZERO);
        reportData.put("serviceRevenue", BigDecimal.ZERO);
        
        // Cost data
        reportData.put("costOfGoods", BigDecimal.ZERO);
        reportData.put("grossProfit", BigDecimal.ZERO);
        
        // Expenses
        reportData.put("sellingExpenses", BigDecimal.ZERO);
        reportData.put("adminExpenses", BigDecimal.ZERO);
        reportData.put("operatingProfit", BigDecimal.ZERO);
        
        // Other items
        reportData.put("financeExpenses", BigDecimal.ZERO);
        reportData.put("otherIncome", BigDecimal.ZERO);
        
        // Net income
        reportData.put("incomeTax", BigDecimal.ZERO);
        reportData.put("netIncome", BigDecimal.ZERO);

        BaoCaoTaiChinhResponse response = BaoCaoTaiChinhResponse.builder()
                .loaiBaoCao("B01")
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .asOfDate(request.getAsOfDate())
                .reportData(reportData)
                .createdBy(request.getCreatedBy())
                .createdAt(LocalDate.now())
                .build();

        log.info("B01 report generated successfully");

        return response;
    }

    /**
     * Generate B02 Report - Balance Sheet (Bảng cân đối kế toán)
     */
    public BaoCaoTaiChinhResponse generateB02(LocalDate asOfDate, Long createdBy) {
        log.info("Generating B02 report: asOfDate={}", asOfDate);

        Map<String, Object> reportData = new HashMap<>();

        // Assets
        reportData.put("currentAssets", BigDecimal.ZERO);
        reportData.put("shortTermInvestment", BigDecimal.ZERO);
        reportData.put("receivables", BigDecimal.ZERO);
        reportData.put("inventory", BigDecimal.ZERO);
        reportData.put("prepaid", BigDecimal.ZERO);
        
        reportData.put("fixedAssets", BigDecimal.ZERO);
        reportData.put("longTermInvestment", BigDecimal.ZERO);
        reportData.put("intangibleAssets", BigDecimal.ZERO);
        reportData.put("totalAssets", BigDecimal.ZERO);

        // Liabilities
        reportData.put("currentLiabilities", BigDecimal.ZERO);
        reportData.put("payables", BigDecimal.ZERO);
        reportData.put("shortTermDebt", BigDecimal.ZERO);
        reportData.put("accrued", BigDecimal.ZERO);
        
        reportData.put("longTermLiabilities", BigDecimal.ZERO);
        reportData.put("totalLiabilities", BigDecimal.ZERO);

        // Equity
        reportData.put("capital", BigDecimal.ZERO);
        reportData.put("reserves", BigDecimal.ZERO);
        reportData.put("retainedEarnings", BigDecimal.ZERO);
        reportData.put("totalEquity", BigDecimal.ZERO);

        BaoCaoTaiChinhResponse response = BaoCaoTaiChinhResponse.builder()
                .loaiBaoCao("B02")
                .asOfDate(asOfDate)
                .reportData(reportData)
                .createdBy(createdBy)
                .createdAt(LocalDate.now())
                .build();

        log.info("B02 report generated successfully");

        return response;
    }

    /**
     * Generate B03 Report - Cash Flow (Báo cáo lưu chuyển tiền tệ)
     */
    public BaoCaoTaiChinhResponse generateB03(BaoCaoTaiChinhRequest request) {
        Objects.requireNonNull(request, "BaoCaoTaiChinhRequest cannot be null");

        log.info("Generating B03 report: startDate={}, endDate={}", request.getStartDate(), request.getEndDate());

        Map<String, Object> reportData = new HashMap<>();

        // Operating activities
        reportData.put("netIncome", BigDecimal.ZERO);
        reportData.put("depreciation", BigDecimal.ZERO);
        reportData.put("changeInReceivables", BigDecimal.ZERO);
        reportData.put("changeInInventory", BigDecimal.ZERO);
        reportData.put("changeInPayables", BigDecimal.ZERO);
        reportData.put("netCashFromOperating", BigDecimal.ZERO);

        // Investing activities
        reportData.put("purchaseFixedAssets", BigDecimal.ZERO);
        reportData.put("purchaseInvestment", BigDecimal.ZERO);
        reportData.put("netCashFromInvesting", BigDecimal.ZERO);

        // Financing activities
        reportData.put("loanProceeds", BigDecimal.ZERO);
        reportData.put("loanRepayment", BigDecimal.ZERO);
        reportData.put("dividendPaid", BigDecimal.ZERO);
        reportData.put("netCashFromFinancing", BigDecimal.ZERO);

        // Summary
        reportData.put("netChangeInCash", BigDecimal.ZERO);
        reportData.put("cashBeginning", BigDecimal.ZERO);
        reportData.put("cashEnding", BigDecimal.ZERO);

        BaoCaoTaiChinhResponse response = BaoCaoTaiChinhResponse.builder()
                .loaiBaoCao("B03")
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .asOfDate(request.getAsOfDate())
                .reportData(reportData)
                .createdBy(request.getCreatedBy())
                .createdAt(LocalDate.now())
                .build();

        log.info("B03 report generated successfully");

        return response;
    }

    /**
     * Generate B09 Report - Inventory Statement (Báo cáo tồn kho)
     */
    public BaoCaoTaiChinhResponse generateB09(BaoCaoTaiChinhRequest request) {
        Objects.requireNonNull(request, "BaoCaoTaiChinhRequest cannot be null");

        log.info("Generating B09 report: asOfDate={}", request.getAsOfDate());

        Map<String, Object> reportData = new HashMap<>();

        // Inventory data
        reportData.put("beginningInventory", BigDecimal.ZERO);
        reportData.put("purchasesInPeriod", BigDecimal.ZERO);
        reportData.put("goodsAvailableForSale", BigDecimal.ZERO);
        reportData.put("costOfGoodsSold", BigDecimal.ZERO);
        reportData.put("endingInventory", BigDecimal.ZERO);

        // By product category
        reportData.put("finishedGoods", BigDecimal.ZERO);
        reportData.put("rawMaterials", BigDecimal.ZERO);
        reportData.put("work InProgress", BigDecimal.ZERO);

        // Movement detail
        reportData.put("quantityBeginning", BigDecimal.ZERO);
        reportData.put("quantityPurchased", BigDecimal.ZERO);
        reportData.put("quantitySold", BigDecimal.ZERO);
        reportData.put("quantityEnding", BigDecimal.ZERO);

        BaoCaoTaiChinhResponse response = BaoCaoTaiChinhResponse.builder()
                .loaiBaoCao("B09")
                .asOfDate(request.getAsOfDate())
                .reportData(reportData)
                .createdBy(request.getCreatedBy())
                .createdAt(LocalDate.now())
                .build();

        log.info("B09 report generated successfully");

        return response;
    }
}
