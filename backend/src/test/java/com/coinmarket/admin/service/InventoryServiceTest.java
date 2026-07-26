package com.coinmarket.admin.service;

import com.coinmarket.admin.dto.InventoryEntryRequest;
import com.coinmarket.admin.dto.InventoryEntryResponse;
import com.coinmarket.admin.entity.InventoryBatch;
import com.coinmarket.admin.repository.BarcodeCodeRepository;
import com.coinmarket.admin.repository.InventoryBatchRepository;
import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.product.dto.ProductResponse;
import com.coinmarket.product.entity.Product;
import com.coinmarket.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryBatchRepository inventoryBatchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BarcodeCodeRepository barcodeCodeRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private InventoryService inventoryService;

    private Product existingProduct;
    private InventoryBatch inventoryBatch;

    @BeforeEach
    void setUp() {
        existingProduct = Product.builder()
                .sellerId(1L)
                .title("1895 Morgan Silver Dollar")
                .description("Rare coin")
                .price(BigDecimal.ZERO)
                .currency("USD")
                .stock(10)
                .status("INVENTORY")
                .country("USA")
                .barcode("7620222022022")
                .purchasePrice(new BigDecimal("800.00"))
                .purchaseCurrency("USD")
                .supplier("CoinSupply Co")
                .sourceInvoice("INV-2026-001")
                .viewCount(0)
                .salesCount(0)
                .build();
        existingProduct.setId(1L);

        inventoryBatch = InventoryBatch.builder()
                .productId(1L)
                .quantity(10)
                .purchasePrice(new BigDecimal("800.00"))
                .currency("USD")
                .supplier("CoinSupply Co")
                .invoiceNo("INV-2026-001")
                .batchDate(LocalDate.of(2026, 7, 1))
                .operatorId(1L)
                .build();
        inventoryBatch.setId(1L);
    }

    private InventoryEntryRequest createRequest(String barcode, String title, Integer quantity,
                                                 BigDecimal price, String supplier, String invoiceNo) {
        InventoryEntryRequest req = new InventoryEntryRequest();
        req.setBarcode(barcode);
        req.setTitle(title);
        req.setQuantity(quantity);
        req.setPurchasePrice(price);
        req.setCurrency("USD");
        req.setSupplier(supplier);
        req.setInvoiceNo(invoiceNo);
        req.setBatchDate(LocalDate.of(2026, 7, 1));
        return req;
    }

    @Nested
    @DisplayName("创建入库记录")
    class CreateEntry {

        @Test
        @DisplayName("新商品入库成功")
        void newProduct_createsEntry() {
            InventoryEntryRequest request = createRequest("7620222022022", "1895 Morgan Silver Dollar",
                    10, new BigDecimal("800.00"), "CoinSupply Co", "INV-2026-001");

            given(productRepository.findByBarcode("7620222022022")).willReturn(Optional.empty());
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                saved.setId(2L);
                return saved;
            });
            given(inventoryBatchRepository.save(any(InventoryBatch.class))).willAnswer(invocation -> {
                InventoryBatch saved = invocation.getArgument(0);
                saved.setId(2L);
                return saved;
            });

            ProductResponse response = inventoryService.createEntry(request, 1L, "Admin");

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("1895 Morgan Silver Dollar");
            assertThat(response.getBarcode()).isEqualTo("7620222022022");
            verify(productRepository).save(any(Product.class));
            verify(inventoryBatchRepository).save(any(InventoryBatch.class));
            verify(auditService).logCreate("PRODUCT", 2L, 1L, "Admin");
            verify(auditService).logCreate("INVENTORY_BATCH", 2L, 1L, "Admin");
        }

        @Test
        @DisplayName("已有商品增加库存")
        void existingProduct_updatesStock() {
            InventoryEntryRequest request = createRequest("7620222022022", "1895 Morgan Silver Dollar",
                    10, new BigDecimal("800.00"), "CoinSupply Co", "INV-2026-001");

            given(productRepository.findByBarcode("7620222022022")).willReturn(Optional.of(existingProduct));
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(inventoryBatchRepository.save(any(InventoryBatch.class))).willAnswer(invocation -> {
                InventoryBatch saved = invocation.getArgument(0);
                saved.setId(3L);
                return saved;
            });

            inventoryService.createEntry(request, 1L, "Admin");

            assertThat(existingProduct.getStock()).isEqualTo(20); // 10 + 10
            verify(productRepository).save(existingProduct);
            verify(inventoryBatchRepository).save(any(InventoryBatch.class));
            verify(auditService).logCreate("INVENTORY_BATCH", 3L, 1L, "Admin");
        }

        @Test
        @DisplayName("使用条码作为标题（当标题缺失时）")
        void barcodeAsTitle_whenTitleMissing() {
            InventoryEntryRequest request = createRequest("7620222022022", null, 1, null, null, null);

            given(productRepository.findByBarcode("7620222022022")).willReturn(Optional.empty());
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                saved.setId(3L);
                return saved;
            });
            given(inventoryBatchRepository.save(any(InventoryBatch.class))).willAnswer(invocation -> {
                InventoryBatch saved = invocation.getArgument(0);
                saved.setId(3L);
                return saved;
            });

            ProductResponse response = inventoryService.createEntry(request, 1L, "Admin");

            assertThat(response.getTitle()).isEqualTo("7620222022022");
        }

        @Test
        @DisplayName("入库数量默认为1")
        void defaultQuantity_whenNull() {
            InventoryEntryRequest request = createRequest("7620222022022", "Test Coin", null, null, null, null);

            given(productRepository.findByBarcode("7620222022022")).willReturn(Optional.empty());
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                saved.setId(4L);
                return saved;
            });
            given(inventoryBatchRepository.save(any(InventoryBatch.class))).willAnswer(invocation -> {
                InventoryBatch saved = invocation.getArgument(0);
                saved.setId(4L);
                return saved;
            });

            ProductResponse response = inventoryService.createEntry(request, 1L, "Admin");

            assertThat(response.getStock()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("生成条码")
    class GenerateBarcode {

        @Test
        @DisplayName("根据编码生成有效13位条码")
        void validCodes_generatesBarcode() {
            InventoryEntryRequest request = new InventoryEntryRequest();
            request.setCountryCode("US");
            request.setCategoryCode("C");
            request.setDenominationCode("001");
            request.setEraCode("202");
            request.setGradeCode("MS");

            String barcode = inventoryService.generateBarcode(request);

            assertThat(barcode).hasSize(13);
            assertThat(barcode).startsWith("0US");
            // Luhn check digit for "0USC001202MS" -> "0USC001202MS" digits: 0,8,5,3,0,0,1,2,0,2,1,3
            // Compute: digits are 0,8,5,3,0,0,1,2,0,2,1,3
        }

        @Test
        @DisplayName("从年份生成时期码")
        void yearBased_eraCode() {
            InventoryEntryRequest request = new InventoryEntryRequest();
            request.setCountryCode("US");
            request.setCategoryCode("C");
            request.setDenominationCode("001");
            request.setYear(1895);
            request.setGradeCode("MS");

            String barcode = inventoryService.generateBarcode(request);

            assertThat(barcode).hasSize(13);
            // year 1895 % 1000 = 895 -> padded to "895"
            assertThat(barcode).contains("895");
        }

        @Test
        @DisplayName("空编码填充为0")
        void nullCodes_fillsWithZeros() {
            InventoryEntryRequest request = new InventoryEntryRequest();

            String barcode = inventoryService.generateBarcode(request);

            assertThat(barcode).hasSize(13);
            assertThat(barcode).isEqualTo("0000000000000");
        }
    }

    @Nested
    @DisplayName("入库列表")
    class ListEntries {

        @Test
        @DisplayName("无查询条件返回所有记录")
        void noQuery_returnsAllEntries() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<InventoryBatch> batchPage = new PageImpl<>(List.of(inventoryBatch), pageable, 1);
            given(inventoryBatchRepository.findAllByOrderByBatchDateDesc(pageable)).willReturn(batchPage);
            given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));

            Page<InventoryEntryResponse> result = inventoryService.listEntries(null, pageable);

            assertThat(result).isNotEmpty();
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("1895 Morgan Silver Dollar");
        }

        @Test
        @DisplayName("空字符串查询返回所有记录")
        void blankQuery_returnsAllEntries() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<InventoryBatch> batchPage = new PageImpl<>(List.of(), pageable, 0);
            given(inventoryBatchRepository.findAllByOrderByBatchDateDesc(pageable)).willReturn(batchPage);

            Page<InventoryEntryResponse> result = inventoryService.listEntries("", pageable);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("上架销售")
    class ListForSale {

        @Test
        @DisplayName("库存商品上架成功")
        void inventoryProduct_listsForSale() {
            given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

            ProductResponse response = inventoryService.listForSale(1L, new BigDecimal("999.99"), 1L, "Admin");

            assertThat(response.getStatus()).isEqualTo("ACTIVE");
            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
            verify(auditService).log(anyString(), anyLong(), anyString(), anyString(), anyString(), anyString(),
                    anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("商品不存在抛出异常")
        void unknownProduct_throwsBusinessException() {
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.listForSale(999L, BigDecimal.TEN, 1L, "Admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("商品不存在");
        }

        @Test
        @DisplayName("非库存状态不能上架")
        void nonInventoryStatus_throwsBusinessException() {
            existingProduct.setStatus("ACTIVE");
            given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));

            assertThatThrownBy(() -> inventoryService.listForSale(1L, BigDecimal.TEN, 1L, "Admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("该商品不在库存状态");
        }
    }

    @Nested
    @DisplayName("库存出库")
    class StockOut {

        @Test
        @DisplayName("出库成功减少库存")
        void validQuantity_reducesStock() {
            given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(inventoryBatchRepository.save(any(InventoryBatch.class))).willAnswer(invocation -> invocation.getArgument(0));

            ProductResponse response = inventoryService.stockOut(1L, 3, "破损出库", 1L, "Admin");

            assertThat(response.getStock()).isEqualTo(7); // 10 - 3
        }

        @Test
        @DisplayName("出库数量不能为0或负数")
        void nonPositiveQuantity_throwsBusinessException() {
            assertThatThrownBy(() -> inventoryService.stockOut(1L, 0, "test", 1L, "Admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("出库数量必须大于0");

            assertThatThrownBy(() -> inventoryService.stockOut(1L, -1, "test", 1L, "Admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("出库数量必须大于0");
        }

        @Test
        @DisplayName("出库数量超过库存抛出异常")
        void insufficientStock_throwsBusinessException() {
            given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));

            assertThatThrownBy(() -> inventoryService.stockOut(1L, 999, "test", 1L, "Admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("出库数量不能超过当前库存");
        }

        @Test
        @DisplayName("商品不存在抛出异常")
        void unknownProduct_throwsBusinessException() {
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.stockOut(999L, 1, "test", 1L, "Admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("商品不存在");
        }
    }

    @Nested
    @DisplayName("更新批次")
    class UpdateBatch {

        @Test
        @DisplayName("更新批次信息成功")
        void existingBatch_updatesSuccessfully() {
            InventoryEntryRequest request = new InventoryEntryRequest();
            request.setPurchasePrice(new BigDecimal("850.00"));
            request.setCurrency("USD");
            request.setSupplier("NewSupplier");
            request.setInvoiceNo("INV-2026-002");

            given(inventoryBatchRepository.findById(1L)).willReturn(Optional.of(inventoryBatch));
            given(inventoryBatchRepository.save(any(InventoryBatch.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));

            inventoryService.updateBatch(1L, request);

            assertThat(inventoryBatch.getPurchasePrice()).isEqualByComparingTo(new BigDecimal("850.00"));
            assertThat(inventoryBatch.getSupplier()).isEqualTo("NewSupplier");
            assertThat(inventoryBatch.getInvoiceNo()).isEqualTo("INV-2026-002");
        }

        @Test
        @DisplayName("批次不存在抛出异常")
        void unknownBatch_throwsBusinessException() {
            InventoryEntryRequest request = new InventoryEntryRequest();
            request.setSupplier("NewSupplier");

            given(inventoryBatchRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.updateBatch(999L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("批次不存在");
        }
    }
}
