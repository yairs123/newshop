package com.coinmarket.finance.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.finance.dto.*;
import com.coinmarket.finance.entity.BankAccount;
import com.coinmarket.finance.entity.BankTransfer;
import com.coinmarket.finance.repository.BankAccountRepository;
import com.coinmarket.finance.repository.BankTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankService {

    private final BankAccountRepository bankAccountRepository;
    private final BankTransferRepository bankTransferRepository;

    @Transactional(readOnly = true)
    public List<BankAccountResponse> listAccounts() {
        return bankAccountRepository.findByIsActiveTrueOrderBySortOrder().stream()
                .map(BankAccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BankAccountResponse getAccount(Long id) {
        return bankAccountRepository.findById(id)
                .map(BankAccountResponse::from)
                .orElseThrow(() -> new BusinessException("银行账户不存在"));
    }

    @Transactional
    public BankAccountResponse createAccount(BankAccountRequest request) {
        BankAccount account = BankAccount.builder()
                .bankName(request.getBankName())
                .accountName(request.getAccountName())
                .accountNumber(request.getAccountNumber())
                .currency(request.getCurrency())
                .country(request.getCountry())
                .currentBalance(request.getCurrentBalance() != null ? request.getCurrentBalance() : BigDecimal.ZERO)
                .isActive(true)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .notes(request.getNotes())
                .build();
        account = bankAccountRepository.save(account);
        return BankAccountResponse.from(account);
    }

    @Transactional
    public BankAccountResponse updateAccount(Long id, BankAccountRequest request) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("银行账户不存在"));
        account.setBankName(request.getBankName());
        account.setAccountName(request.getAccountName());
        account.setAccountNumber(request.getAccountNumber());
        account.setCurrency(request.getCurrency());
        account.setCountry(request.getCountry());
        account.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        account.setNotes(request.getNotes());
        account = bankAccountRepository.save(account);
        return BankAccountResponse.from(account);
    }

    @Transactional
    public void deleteAccount(Long id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("银行账户不存在"));
        account.setIsActive(false);
        bankAccountRepository.save(account);
    }

    @Transactional
    public BankAccountResponse adjustBalance(Long id, BigDecimal amount, String notes) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("银行账户不存在"));
        account.setCurrentBalance(account.getCurrentBalance().add(amount));
        if (notes != null) account.setNotes(notes);
        account = bankAccountRepository.save(account);
        return BankAccountResponse.from(account);
    }

    @Transactional
    public BankTransferResponse transfer(BankTransferRequest request, Long userId) {
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new BusinessException("转出和转入账户不能相同");
        }

        BankAccount from = bankAccountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new BusinessException("转出账户不存在"));
        BankAccount to = bankAccountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new BusinessException("转入账户不存在"));

        BigDecimal totalDeduction = request.getAmount().add(
                request.getFee() != null ? request.getFee() : BigDecimal.ZERO);

        if (from.getCurrentBalance().compareTo(totalDeduction) < 0) {
            throw new BusinessException("余额不足：账户余额 " + from.getCurrentBalance()
                    + "，需要 " + totalDeduction);
        }

        from.setCurrentBalance(from.getCurrentBalance().subtract(totalDeduction));
        bankAccountRepository.save(from);

        to.setCurrentBalance(to.getCurrentBalance().add(request.getAmount()));
        bankAccountRepository.save(to);

        BankTransfer transfer = BankTransfer.builder()
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .fee(request.getFee() != null ? request.getFee() : BigDecimal.ZERO)
                .transferDate(request.getTransferDate())
                .referenceNo(request.getReferenceNo())
                .description(request.getDescription())
                .createdBy(userId)
                .build();
        transfer = bankTransferRepository.save(transfer);

        BankTransferResponse response = BankTransferResponse.from(transfer);
        response.setFromBankName(from.getBankName());
        response.setToBankName(to.getBankName());
        return response;
    }

    @Transactional(readOnly = true)
    public Page<BankTransferResponse> listTransfers(Long accountId, Pageable pageable) {
        Page<BankTransfer> page;
        if (accountId != null) {
            page = bankTransferRepository
                    .findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(accountId, accountId, pageable);
        } else {
            page = bankTransferRepository.findAll(pageable);
        }
        return page.map(t -> {
            BankTransferResponse r = BankTransferResponse.from(t);
            r.setFromBankName(
                bankAccountRepository.findById(t.getFromAccountId())
                    .map(BankAccount::getBankName).orElse(""));
            r.setToBankName(
                bankAccountRepository.findById(t.getToAccountId())
                    .map(BankAccount::getBankName).orElse(""));
            return r;
        });
    }
}
