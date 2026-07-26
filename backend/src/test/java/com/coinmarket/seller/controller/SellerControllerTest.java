package com.coinmarket.seller.controller;

import com.coinmarket.common.security.JwtAuthenticationFilter;
import com.coinmarket.common.security.JwtTokenProvider;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.seller.dto.SellerApplicationRequest;
import com.coinmarket.seller.dto.SellerDashboardResponse;
import com.coinmarket.seller.dto.SellerStatusResponse;
import com.coinmarket.seller.service.SellerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SellerController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class SellerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SellerService sellerService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        var principal = new UserPrincipal(1L, "testuser",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void applyShouldReturn200() throws Exception {
        SellerApplicationRequest request = SellerApplicationRequest.builder()
                .shopName("My Coin Shop")
                .shopDescription("Selling rare coins since 2024")
                .idDocumentUrl("https://example.com/id.pdf")
                .idDocumentType("PASSPORT")
                .build();

        mockMvc.perform(post("/api/seller/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(sellerService).submitApplication(eq(1L), any(SellerApplicationRequest.class));
    }

    @Test
    void applyShouldReturn400WhenValidationFails() throws Exception {
        SellerApplicationRequest request = SellerApplicationRequest.builder()
                .shopName("")
                .shopDescription("")
                .idDocumentUrl("")
                .build();

        mockMvc.perform(post("/api/seller/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void getStatusShouldReturn200() throws Exception {
        SellerStatusResponse statusResponse = SellerStatusResponse.builder()
                .hasApplied(true)
                .status("APPROVED")
                .hasProfile(true)
                .locked(false)
                .shopName("My Coin Shop")
                .shopDescription("Selling rare coins")
                .build();

        given(sellerService.getStatus(1L)).willReturn(statusResponse);

        mockMvc.perform(get("/api/seller/status")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.hasApplied").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.shopName").value("My Coin Shop"))
                .andExpect(jsonPath("$.data.locked").value(false));
    }

    @Test
    void getStatusShouldReturnPendingWhenNotApplied() throws Exception {
        SellerStatusResponse statusResponse = SellerStatusResponse.builder()
                .hasApplied(false)
                .status("NONE")
                .hasProfile(false)
                .locked(false)
                .build();

        given(sellerService.getStatus(1L)).willReturn(statusResponse);

        mockMvc.perform(get("/api/seller/status")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.hasApplied").value(false))
                .andExpect(jsonPath("$.data.status").value("NONE"));
    }

    @Test
    void getDashboardShouldReturn200() throws Exception {
        SellerDashboardResponse dashboardResponse = SellerDashboardResponse.builder()
                .productCount(25L)
                .pendingOrders(3L)
                .monthlySales(new BigDecimal("1500.00"))
                .averageRating(4.5)
                .lowStockCount(2L)
                .toShipCount(5L)
                .build();

        given(sellerService.getDashboardStats(1L)).willReturn(dashboardResponse);

        mockMvc.perform(get("/api/seller/dashboard")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.productCount").value(25))
                .andExpect(jsonPath("$.data.pendingOrders").value(3))
                .andExpect(jsonPath("$.data.monthlySales").value(1500.00))
                .andExpect(jsonPath("$.data.averageRating").value(4.5))
                .andExpect(jsonPath("$.data.lowStockCount").value(2))
                .andExpect(jsonPath("$.data.toShipCount").value(5));
    }

    @Test
    void updateProfileShouldReturn200() throws Exception {
        Map<String, String> body = Map.of(
                "shopName", "Updated Shop Name",
                "shopDescription", "Updated description"
        );

        mockMvc.perform(put("/api/seller/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(sellerService).updateProfile(eq(1L), eq("Updated Shop Name"), eq("Updated description"));
    }
}
