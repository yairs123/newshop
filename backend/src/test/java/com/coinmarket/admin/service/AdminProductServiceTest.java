package com.coinmarket.admin.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.product.dto.ProductCreateRequest;
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
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private AdminProductService adminProductService;

    private Product activeProduct;
    private Product inventoryProduct;
    private Product printedProduct;
    private ProductCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        activeProduct = Product.builder()
                .sellerId(1L)
                .title("1895 Morgan Silver Dollar")
                .description("Rare collectible coin")
                .price(new BigDecimal("1299.99"))
                .currency("USD")
                .stock(5)
                .status("ACTIVE")
                .country("USA")
                .year(1895)
                .material("Silver")
                .ratingCompany("PCGS")
                .ratingGrade("MS63")
                .barcode("7620222022022")
                .viewCount(0)
                .salesCount(0)
                .build();
        activeProduct.setId(1L);

        inventoryProduct = Product.builder()
                .sellerId(1L)
                .title("1900 Gold Coin")
                .price(BigDecimal.ZERO)
                .currency("USD")
                .stock(10)
                .status("INVENTORY")
                .barcode("7620222022023")
                .viewCount(0)
                .salesCount(0)
                .build();
        inventoryProduct.setId(2L);

        printedProduct = Product.builder()
                .sellerId(1L)
                .title("1921 Peace Dollar")
                .price(new BigDecimal("599.99"))
                .currency("USD")
                .stock(3)
                .status("ACTIVE")
                .barcode("7620222022024")
                .printedAt(LocalDateTime.of(2026, 7, 1, 10, 0))
                .viewCount(0)
                .salesCount(0)
                .build();
        printedProduct.setId(3L);

        createRequest = ProductCreateRequest.builder()
                .title("New Coin")
                .description("Brand new coin")
                .price(new BigDecimal("499.99"))
                .currency("USD")
                .stock(10)
                .country("USA")
                .year(2026)
                .material("Gold")
                .build();
    }

    @Nested
    @DisplayName("商品列表")
    class ListProducts {

        @Test
        @DisplayName("返回分页商品列表")
        void returnsPagedProducts() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(activeProduct), pageable, 1);
            given(productRepository.findAll(pageable)).willReturn(productPage);

            Page<ProductResponse> result = adminProductService.listProducts(pageable);

            assertThat(result).isNotEmpty();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("1895 Morgan Silver Dollar");
            assertThat(result.getContent().get(0).getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("无商品时返回空列表")
        void noProducts_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            given(productRepository.findAll(pageable)).willReturn(emptyPage);

            Page<ProductResponse> result = adminProductService.listProducts(pageable);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("筛选商品列表")
    class ListProductsFiltered {

        @Test
        @DisplayName("按状态筛选")
        void filtersByStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> filteredPage = new PageImpl<>(List.of(inventoryProduct), pageable, 1);
            given(productRepository.findAll(any(Specification.class), eqPageable(pageable)))
                    .willReturn(filteredPage);

            Page<ProductResponse> result = adminProductService.listProductsFiltered("INVENTORY", null, null, null, pageable);

            assertThat(result).isNotEmpty();
            assertThat(result.getContent().get(0).getStatus()).isEqualTo("INVENTORY");
        }

        @Test
        @DisplayName("按已打印状态筛选")
        void filtersByPrinted() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> filteredPage = new PageImpl<>(List.of(printedProduct), pageable, 1);
            given(productRepository.findAll(any(Specification.class), eqPageable(pageable)))
                    .willReturn(filteredPage);

            Page<ProductResponse> result = adminProductService.listProductsFiltered(null, true, null, null, pageable);

            assertThat(result).isNotEmpty();
            assertThat(result.getContent().get(0).getPrintedAt()).isNotNull();
        }

        @Test
        @DisplayName("按日期范围筛选")
        void filtersByDateRange() {
            Pageable pageable = PageRequest.of(0, 10);
            LocalDate from = LocalDate.of(2026, 1, 1);
            LocalDate to = LocalDate.of(2026, 12, 31);
            Page<Product> filteredPage = new PageImpl<>(List.of(activeProduct), pageable, 1);
            given(productRepository.findAll(any(Specification.class), eqPageable(pageable)))
                    .willReturn(filteredPage);

            Page<ProductResponse> result = adminProductService.listProductsFiltered(null, null, from, to, pageable);

            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("创建商品")
    class CreateProduct {

        @Test
        @DisplayName("创建普通商品成功")
        void validRequest_createsProduct() {
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                saved.setId(10L);
                return saved;
            });

            ProductResponse response = adminProductService.createProduct(1L, createRequest);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("New Coin");
            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("499.99"));
            assertThat(response.getStatus()).isEqualTo("ACTIVE");
            assertThat(response.getSellerId()).isEqualTo(1L);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("创建商品使用默认货币")
        void missingCurrency_usesDefault() {
            ProductCreateRequest request = ProductCreateRequest.builder()
                    .title("Test Coin")
                    .price(new BigDecimal("99.99"))
                    .stock(1)
                    .build();

            given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                saved.setId(11L);
                return saved;
            });

            ProductResponse response = adminProductService.createProduct(1L, request);

            assertThat(response.getCurrency()).isEqualTo("USD");
        }
    }

    @Nested
    @DisplayName("更新商品")
    class UpdateProduct {

        @Test
        @DisplayName("更新商品成功")
        void existingProduct_updatesSuccessfully() {
            ProductCreateRequest updateReq = ProductCreateRequest.builder()
                    .title("Updated Title")
                    .description("Updated desc")
                    .price(new BigDecimal("1499.99"))
                    .stock(10)
                    .build();

            given(productRepository.findById(1L)).willReturn(Optional.of(activeProduct));
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

            ProductResponse response = adminProductService.updateProduct(1L, updateReq);

            assertThat(response.getTitle()).isEqualTo("Updated Title");
            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("1499.99"));
            assertThat(response.getStock()).isEqualTo(10);
        }

        @Test
        @DisplayName("更新不存在的商品抛出异常")
        void unknownProduct_throwsBusinessException() {
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminProductService.updateProduct(999L, createRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("商品不存在");
        }
    }

    @Nested
    @DisplayName("更新商品状态")
    class UpdateProductStatus {

        @Test
        @DisplayName("更新状态成功")
        void updatesStatus() {
            given(productRepository.findById(1L)).willReturn(Optional.of(activeProduct));
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

            adminProductService.updateProductStatus(1L, "INACTIVE");

            assertThat(activeProduct.getStatus()).isEqualTo("INACTIVE");
        }

        @Test
        @DisplayName("商品不存在抛出异常")
        void unknownProduct_throwsBusinessException() {
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminProductService.updateProductStatus(999L, "INACTIVE"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("商品不存在");
        }
    }

    @Nested
    @DisplayName("标记已打印")
    class MarkAsPrinted {

        @Test
        @DisplayName("按ID标记打印成功")
        void byId_marksAsPrinted() {
            given(productRepository.findById(1L)).willReturn(Optional.of(activeProduct));
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

            adminProductService.markAsPrinted(1L);

            assertThat(activeProduct.getPrintedAt()).isNotNull();
        }

        @Test
        @DisplayName("按ID标记打印-商品不存在抛出异常")
        void byId_unknownProduct_throwsBusinessException() {
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminProductService.markAsPrinted(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("商品不存在");
        }

        @Test
        @DisplayName("按条码标记打印成功")
        void byBarcode_marksAsPrinted() {
            given(productRepository.findByBarcode("7620222022022")).willReturn(Optional.of(activeProduct));
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

            adminProductService.markAsPrintedByBarcode("7620222022022");

            assertThat(activeProduct.getPrintedAt()).isNotNull();
        }

        @Test
        @DisplayName("按条码标记打印-商品不存在抛出异常")
        void byBarcode_unknownProduct_throwsBusinessException() {
            given(productRepository.findByBarcode("UNKNOWN")).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminProductService.markAsPrintedByBarcode("UNKNOWN"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("商品不存在: UNKNOWN");
        }

        @Test
        @DisplayName("批量标记打印成功")
        void batch_marksAllAsPrinted() {
            List<Long> ids = List.of(1L, 2L, 3L);
            given(productRepository.findAllById(ids)).willReturn(List.of(activeProduct, inventoryProduct, printedProduct));
            given(productRepository.saveAll(any(List.class))).willAnswer(invocation -> invocation.getArgument(0));

            adminProductService.markBatchAsPrinted(ids);

            verify(productRepository).saveAll(any(List.class));
            assertThat(activeProduct.getPrintedAt()).isNotNull();
            assertThat(inventoryProduct.getPrintedAt()).isNotNull();
            assertThat(printedProduct.getPrintedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("按条码查询")
    class FindByBarcode {

        @Test
        @DisplayName("找到商品返回响应")
        void existingBarcode_returnsProduct() {
            given(productRepository.findByBarcode("7620222022022")).willReturn(Optional.of(activeProduct));

            ProductResponse response = adminProductService.findByBarcode("7620222022022");

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("1895 Morgan Silver Dollar");
        }

        @Test
        @DisplayName("未找到返回null")
        void unknownBarcode_returnsNull() {
            given(productRepository.findByBarcode("UNKNOWN")).willReturn(Optional.empty());

            ProductResponse response = adminProductService.findByBarcode("UNKNOWN");

            assertThat(response).isNull();
        }
    }

    @Nested
    @DisplayName("关键词搜索")
    class SearchByKeyword {

        @Test
        @DisplayName("按标题搜索返回结果")
        void keywordSearch_returnsResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> searchPage = new PageImpl<>(List.of(activeProduct), pageable, 1);
            given(productRepository.findAll(any(Specification.class), eqPageable(pageable)))
                    .willReturn(searchPage);

            Page<ProductResponse> result = adminProductService.searchByKeyword("Morgan", pageable);

            assertThat(result).isNotEmpty();
            assertThat(result.getContent().get(0).getTitle()).contains("Morgan");
        }

        @Test
        @DisplayName("无匹配结果返回空")
        void noMatch_returnsEmpty() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            given(productRepository.findAll(any(Specification.class), eqPageable(pageable)))
                    .willReturn(emptyPage);

            Page<ProductResponse> result = adminProductService.searchByKeyword("NonExistent", pageable);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("按日期范围列表")
    class ListByDateRange {

        @Test
        @DisplayName("按日期范围返回列表")
        void dateRange_returnsResults() {
            LocalDate from = LocalDate.of(2026, 1, 1);
            LocalDate to = LocalDate.of(2026, 12, 31);
            given(productRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
                    .willReturn(List.of(activeProduct, inventoryProduct));

            List<ProductResponse> result = adminProductService.listByDateRange(from, to, null);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("无匹配时返回空列表")
        void noMatch_returnsEmptyList() {
            given(productRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
                    .willReturn(List.of());

            List<ProductResponse> result = adminProductService.listByDateRange(null, null, null);

            assertThat(result).isEmpty();
        }
    }

    // Helper to match Pageable argument in Mockito
    private static Pageable eqPageable(Pageable pageable) {
        return org.mockito.ArgumentMatchers.argThat(p ->
                p.getPageNumber() == pageable.getPageNumber()
                        && p.getPageSize() == pageable.getPageSize()
                        && p.getSort().equals(pageable.getSort())
        );
    }
}
