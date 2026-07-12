package com.coinmarket.finance.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.finance.dto.*;
import com.coinmarket.finance.entity.PersonnelExpense;
import com.coinmarket.finance.repository.PersonnelExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonnelService {

    private final PersonnelExpenseRepository personnelExpenseRepository;

    @Transactional(readOnly = true)
    public Page<PersonnelExpenseResponse> listPersonnel(Pageable pageable) {
        return personnelExpenseRepository.findAllByOrderByPayDateDesc(pageable)
                .map(PersonnelExpenseResponse::from);
    }

    @Transactional
    public PersonnelExpenseResponse createPersonnel(PersonnelExpenseRequest request, Long userId) {
        PersonnelExpense p = PersonnelExpense.builder()
                .employeeName(request.getEmployeeName())
                .position(request.getPosition())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "SGD")
                .payDate(request.getPayDate())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .notes(request.getNotes())
                .paymentMethod(request.getPaymentMethod())
                .bankAccountId(request.getBankAccountId())
                .createdBy(userId)
                .build();
        p = personnelExpenseRepository.save(p);
        return PersonnelExpenseResponse.from(p);
    }

    @Transactional
    public PersonnelExpenseResponse updatePersonnel(Long id, PersonnelExpenseRequest request) {
        PersonnelExpense p = personnelExpenseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("人员开支记录不存在"));
        p.setEmployeeName(request.getEmployeeName());
        p.setPosition(request.getPosition());
        p.setAmount(request.getAmount());
        p.setCurrency(request.getCurrency() != null ? request.getCurrency() : "SGD");
        p.setPayDate(request.getPayDate());
        p.setPeriodStart(request.getPeriodStart());
        p.setPeriodEnd(request.getPeriodEnd());
        p.setNotes(request.getNotes());
        p.setPaymentMethod(request.getPaymentMethod());
        p.setBankAccountId(request.getBankAccountId());
        p = personnelExpenseRepository.save(p);
        return PersonnelExpenseResponse.from(p);
    }

    @Transactional
    public void deletePersonnel(Long id) {
        if (!personnelExpenseRepository.existsById(id)) {
            throw new BusinessException("人员开支记录不存在");
        }
        personnelExpenseRepository.deleteById(id);
    }
}
