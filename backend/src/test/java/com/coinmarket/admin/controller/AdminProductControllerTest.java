package com.coinmarket.admin.controller;

import com.coinmarket.admin.service.AdminProductService;
import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.common.security.JwtAuthenticationFilter;
import com.coinmarket.common.security.JwtTokenProvider;
import com.coinmarket.product.dto.ProductCreateRequest;
import com.coinmarket.product.dto.ProductResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminProductController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminProductService adminProductService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void listProductsShouldReturn200() throws Exception {
        ProductResponse product = ProductResponse.builder()
                .id(1L)
                .title("Ancient Coin")
                .price(new BigDecimal("100.00"))
                .currency("USD")
                .status("ACTIVE")
                .stock(10)
                .build();

        Page<ProductResponse> page = new PageImpl<>(List.of(product));
        given(adminProductService.listProductsFiltered(
                isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .willReturn(page);

        mockMvc.perform(get("/api/admin/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Ancient Coin"))
                .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void listProductsShouldReturn200WithFilters() throws Exception {
        ProductResponse product = ProductResponse.builder()
                .id(2L)
                .title("Silver Coin")
                .price(new BigDecimal("50.00"))
                .currency("USD")
                .status("ACTIVE")
                .build();

        Page<ProductResponse> page = new PageImpl<>(List.of(product));
        given(adminProductService.listProductsFiltered(
                eq("ACTIVE"), eq(true), any(), any(), any(Pageable.class)))
                .willReturn(page);

        mockMvc.perform(get("/api/admin/products")
                        .param("status", "ACTIVE")
                        .param("printed", "true")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].title").value("Silver Coin"))
                .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"));
    }

    @Test
    void listProductsShouldReturn200WithDateFilters() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(List.of());
        given(adminProductService.listProductsFiltered(
                isNull(), isNull(), eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 12, 31)), any(Pageable.class)))
                .willReturn(page);

        mockMvc.perform(get("/api/admin/products")
                        .param("dateFrom", "2024-01-01")
                        .param("dateTo", "2024-12-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    @Test
    void listByDateRangeShouldReturn200() throws Exception {
        ProductResponse product = ProductResponse.builder()
                .id(1L)
                .title("Dated Coin")
                .price(new BigDecimal("75.00"))
                .currency("USD")
                .build();

        given(adminProductService.listByDateRange(
                eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 12, 31)), eq(true)))
                .willReturn(List.of(product));

        mockMvc.perform(get("/api/admin/products/by-date")
                        .param("dateFrom", "2024-01-01")
                        .param("dateTo", "2024-12-31")
                        .param("printed", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("Dated Coin"));
    }

    @Test
    void getByBarcodeShouldReturn200() throws Exception {
        ProductResponse product = ProductResponse.builder()
                .id(1L)
                .title("Barcode Coin")
                .barcode("ABC123")
                .status("ACTIVE")
                .build();

        given(adminProductService.findByBarcode("ABC123")).willReturn(product);

        mockMvc.perform(get("/api/admin/products/barcode/ABC123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Barcode Coin"))
                .andExpect(jsonPath("$.data.barcode").value("ABC123"));
    }

    @Test
    void getByBarcodeShouldReturn404WhenNotFound() throws Exception {
        given(adminProductService.findByBarcode("INVALID"))
                .willThrow(new BusinessException(404, "Product not found with barcode: INVALID"));

        mockMvc.perform(get("/api/admin/products/barcode/INVALID")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with barcode: INVALID"));
    }

    @Test
    void createProductShouldReturn200() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .title("New Coin")
                .price(new BigDecimal("99.99"))
                .currency("USD")
                .stock(5)
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(10L)
                .title("New Coin")
                .sellerId(5L)
                .price(new BigDecimal("99.99"))
                .currency("USD")
                .status("ACTIVE")
                .stock(5)
                .build();

        given(adminProductService.createProduct(eq(5L), any(ProductCreateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/admin/products")
                        .param("sellerId", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.title").value("New Coin"))
                .andExpect(jsonPath("$.data.sellerId").value(5))
                .andExpect(jsonPath("$.data.price").value(99.99));
    }

    @Test
    void createProductShouldReturn400WhenValidationFails() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .title("")
                .price(null)
                .currency("")
                .build();

        mockMvc.perform(post("/api/admin/products")
                        .param("sellerId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void searchProductsShouldReturn200() throws Exception {
        ProductResponse product = ProductResponse.builder()
                .id(1L)
                .title("Search Result Coin")
                .price(new BigDecimal("50.00"))
                .currency("USD")
                .build();

        Page<ProductResponse> page = new PageImpl<>(List.of(product));
        given(adminProductService.searchByKeyword(eq("coin"), any(Pageable.class)))
                .willReturn(page);

        mockMvc.perform(get("/api/admin/products/search")
                        .param("q", "coin")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].title").value("Search Result Coin"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void updateProductShouldReturn200() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .title("Updated Coin")
                .price(new BigDecimal("120.00"))
                .currency("USD")
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .title("Updated Coin")
                .price(new BigDecimal("120.00"))
                .currency("USD")
                .status("ACTIVE")
                .build();

        given(adminProductService.updateProduct(eq(1L), any(ProductCreateRequest.class))).willReturn(response);

        mockMvc.perform(put("/api/admin/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Updated Coin"))
                .andExpect(jsonPath("$.data.price").value(120.00));
    }

    @Test
    void updateProductStatusShouldReturn200() throws Exception {
        mockMvc.perform(put("/api/admin/products/1/status")
                        .param("status", "INACTIVE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    void markAsPrintedShouldReturn200() throws Exception {
        mockMvc.perform(post("/api/admin/products/1/mark-printed")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    void markBatchAsPrintedShouldReturn200() throws Exception {
        List<Long> ids = List.of(1L, 2L, 3L);

        mockMvc.perform(post("/api/admin/products/mark-printed-batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));
    }
}
