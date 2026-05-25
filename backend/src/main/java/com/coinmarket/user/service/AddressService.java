package com.coinmarket.user.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.user.dto.AddressRequest;
import com.coinmarket.user.dto.AddressResponse;
import com.coinmarket.user.entity.UserAddress;
import com.coinmarket.user.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserAddressRepository addressRepository;

    public List<AddressResponse> getUserAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(AddressResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public AddressResponse getAddress(Long id, Long userId) {
        UserAddress addr = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("Address not found"));
        return AddressResponse.fromEntity(addr);
    }

    @Transactional
    public AddressResponse createAddress(Long userId, AddressRequest request) {
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultFlag(userId);
        }

        UserAddress address = UserAddress.builder()
                .userId(userId)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .country(request.getCountry())
                .city(request.getCity())
                .zipCode(request.getZipCode())
                .address(request.getAddress())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .build();

        UserAddress saved = addressRepository.save(address);
        return AddressResponse.fromEntity(saved);
    }

    @Transactional
    public AddressResponse updateAddress(Long id, Long userId, AddressRequest request) {
        UserAddress existing = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("Address not found"));

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(existing.getIsDefault())) {
            clearDefaultFlag(userId);
        }

        existing.setFullName(request.getFullName());
        existing.setPhone(request.getPhone());
        existing.setCountry(request.getCountry());
        existing.setCity(request.getCity());
        existing.setZipCode(request.getZipCode());
        existing.setAddress(request.getAddress());
        existing.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));

        UserAddress saved = addressRepository.save(existing);
        return AddressResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteAddress(Long id, Long userId) {
        UserAddress existing = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("Address not found"));
        addressRepository.delete(existing);
    }

    @Transactional
    public void setDefault(Long id, Long userId) {
        UserAddress address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("Address not found"));
        clearDefaultFlag(userId);
        address.setIsDefault(true);
        addressRepository.save(address);
    }

    private void clearDefaultFlag(Long userId) {
        List<UserAddress> defaults = addressRepository.findByUserIdAndIsDefaultTrue(userId);
        defaults.forEach(a -> a.setIsDefault(false));
    }
}
