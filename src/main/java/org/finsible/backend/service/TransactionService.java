package org.finsible.backend.service;

import org.apache.coyote.BadRequestException;
import org.finsible.backend.CustomExceptionHandler.EntityNotFoundException;
import org.finsible.backend.dto.request.TransactionRequestDTO;
import org.finsible.backend.dto.response.DailyTransactionSummaryDTO;
import org.finsible.backend.dto.response.TransactionResponseDTO;
import org.finsible.backend.entity.*;
import org.finsible.backend.mapper.TransactionMapper;
import org.finsible.backend.repository.AccountRepository;
import org.finsible.backend.repository.CategoryRepository;
import org.finsible.backend.repository.SupportedCurrencyRepository;
import org.finsible.backend.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private static final int BULK_DELETE_CHUNK_SIZE = 500;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final SupportedCurrencyRepository supportedCurrencyRepository;
    private final TransactionMapper transactionMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository,
                            CategoryRepository categoryRepository, TransactionMapper transactionMapper, SupportedCurrencyRepository supportedCurrencyRepository) {
        this.supportedCurrencyRepository = supportedCurrencyRepository;
        this.transactionMapper = transactionMapper;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> getAllTransactions(String userId, Long startDate, Long endDate, Long accountId, Long accountGroupId, Long categoryId,
                                                           Type type, BigDecimal minAmount, BigDecimal maxAmount, String sortBy, String search, int page, int size) {
        Sort sort = getSortFromSortBy(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Transaction> transactionPage = transactionRepository.findAllWithFilters(
                userId, type, categoryId, accountId, accountGroupId, startDate, endDate, minAmount, maxAmount, search, pageable
        );

        return transactionPage.map(transactionMapper::toTransactionResponseDTO);
    }

    private Sort getSortFromSortBy(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "transactionDate", "id");
        }
        return switch (sortBy.toLowerCase()) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "transactionDate", "id");
            case "amount_high" -> Sort.by(Sort.Direction.DESC, "totalAmount", "transactionDate", "id");
            case "amount_low" -> Sort.by(Sort.Direction.ASC, "totalAmount", "transactionDate", "id");
            default -> Sort.by(Sort.Direction.DESC, "transactionDate", "id"); // "newest" or default
        };
    }

    @Transactional(readOnly = true)
    public List<DailyTransactionSummaryDTO> getDailyTransactionsSummary(String userId, Long startDate, Long endDate, Long accountId, Long accountGroupId,
                                                                        Long categoryId, Type type) {
        return transactionRepository.findDailyTransactionSummary(
                userId, type, categoryId, accountId, accountGroupId, startDate, endDate
        );
    }

    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransactionById(String userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndCreatedBy(transactionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));
        return transactionMapper.toTransactionResponseDTO(transaction);
    }

    @Transactional
    public TransactionResponseDTO createTransaction(String userId, TransactionRequestDTO requestDTO) throws BadRequestException {
        Type type = requestDTO.getType();
        Transaction transaction;

        switch (type) {
            case INCOME -> transaction = createIncomeTransaction(userId, requestDTO);
            case EXPENSE -> transaction = createExpenseTransaction(userId, requestDTO);
            case TRANSFER -> transaction = createTransferTransaction(userId, requestDTO);
            default -> throw new BadRequestException("Invalid transaction type: " + type);
        }

        transactionRepository.save(transaction);
        logger.info("Created {} transaction with id: {}", type, transaction.getId());
        return transactionMapper.toTransactionResponseDTO(transaction);
    }

    private Transaction createIncomeTransaction(String userId, TransactionRequestDTO requestDTO) throws BadRequestException {
        Account toAccount = getAccount(userId, requestDTO.getToAccountId(), "To account");
        Category category = getCategory(userId, requestDTO.getCategoryId(), Type.INCOME);

        Transaction transaction = buildTransaction(requestDTO, toAccount, null, category);
        handleCurrency(toAccount, transaction, requestDTO);
        updateAccountBalance(toAccount, requestDTO.getTotalAmount(), true);

        return transaction;
    }

    private Transaction createExpenseTransaction(String userId, TransactionRequestDTO requestDTO) throws BadRequestException {
        Account fromAccount = getAccount(userId, requestDTO.getFromAccountId(), "From account");
        Category category = getCategory(userId, requestDTO.getCategoryId(), Type.EXPENSE);

        Transaction transaction = buildTransaction(requestDTO, null, fromAccount, category);
        handleCurrency(fromAccount, transaction, requestDTO);
        updateAccountBalance(fromAccount, requestDTO.getTotalAmount(), false);

        return transaction;
    }

    private Transaction createTransferTransaction(String userId, TransactionRequestDTO requestDTO) throws BadRequestException {
        Account toAccount = getAccount(userId, requestDTO.getToAccountId(), "To account");
        Account fromAccount = getAccount(userId, requestDTO.getFromAccountId(), "From account");
        if (toAccount.getId().equals(fromAccount.getId())) {
            throw new BadRequestException("To account and From account cannot be the same for transfer transaction");
        }
        Category category = getCategory(userId, requestDTO.getCategoryId(), Type.TRANSFER);

        Transaction transaction = buildTransaction(requestDTO, toAccount, fromAccount, category);
        handleCurrency(fromAccount, transaction, requestDTO);
        updateAccountBalance(fromAccount, requestDTO.getTotalAmount(), false);
        updateAccountBalance(toAccount, requestDTO.getTotalAmount(), true);

        return transaction;
    }

    @Transactional
    public TransactionResponseDTO updateTransaction(String userId, Long transactionId,
                                                    TransactionRequestDTO requestDTO) throws BadRequestException {
        Transaction transaction = transactionRepository.findByIdAndCreatedBy(transactionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));

        // 1. Validate type cannot change
        if (requestDTO.getType() != null && !requestDTO.getType().equals(transaction.getType())) {
            throw new BadRequestException("Transaction type cannot be updated");
        }

        Type type = transaction.getType();

        // 2. Handle category update
        if (requestDTO.getCategoryId() != null &&
           !requestDTO.getCategoryId().equals(transaction.getCategory().getId())) {
            Category category = getCategory(userId, requestDTO.getCategoryId(), type);
            transaction.setCategory(category);
        }

        // 3. Handle amount change BEFORE account changes
        if (requestDTO.getTotalAmount() != null &&
           !requestDTO.getTotalAmount().equals(transaction.getTotalAmount())) {

            BigDecimal amountDifference = requestDTO.getTotalAmount()
                .subtract(transaction.getTotalAmount());

            handleAmountChange(transaction, amountDifference);
        }
        BigDecimal latestAmount = requestDTO.getTotalAmount() != null ? requestDTO.getTotalAmount() : transaction.getTotalAmount();

        // Ensure for TRANSFER type, fromAccount and toAccount are not the same
        if (type == Type.TRANSFER &&
                ( requestDTO.getFromAccountId() != null && requestDTO.getToAccountId() == null && requestDTO.getFromAccountId().equals(transaction.getToAccount().getId())) ||
                ( requestDTO.getToAccountId() != null && requestDTO.getFromAccountId() == null && requestDTO.getToAccountId().equals(transaction.getFromAccount().getId())) ||
                ( requestDTO.getFromAccountId() != null && requestDTO.getToAccountId() != null && requestDTO.getFromAccountId().equals(requestDTO.getToAccountId()))) {
            throw new BadRequestException("For TRANSFER transactions, fromAccount and toAccount must be different");
        }

        // 4. Handle toAccount change (INCOME/TRANSFER)
        if (requestDTO.getToAccountId() != null &&
           (type == Type.INCOME || type == Type.TRANSFER) && !requestDTO.getToAccountId().equals(transaction.getToAccount().getId())) {

            updateAccountBalance(transaction.getToAccount(), latestAmount, false);
            Account newToAccount = getAccount(userId, requestDTO.getToAccountId(), "To account");
            handleCurrency(newToAccount, transaction, requestDTO);
            updateAccountBalance(newToAccount, latestAmount, true);
            transaction.setToAccount(newToAccount);
        }

        // 5. Handle fromAccount change (EXPENSE/TRANSFER)
        if (requestDTO.getFromAccountId() != null &&
           (type == Type.EXPENSE || type == Type.TRANSFER) && !requestDTO.getFromAccountId().equals(transaction.getFromAccount().getId())) {

            updateAccountBalance(transaction.getFromAccount(), latestAmount, true);
            Account newFromAccount = getAccount(userId, requestDTO.getFromAccountId(), "From account");
            handleCurrency(newFromAccount, transaction, requestDTO);
            updateAccountBalance(newFromAccount, latestAmount, false);
            transaction.setFromAccount(newFromAccount);
        }

        // 6. Update other fields
        transactionMapper.updateTransaction(requestDTO, transaction);

        transactionRepository.save(transaction);
        logger.info("Updated transaction with id: {}", transactionId);
        return transactionMapper.toTransactionResponseDTO(transaction);
    }

    @Transactional
    public void deleteTransactions(String userId, List<Long> transactionIds) {
        if (transactionIds == null || transactionIds.isEmpty()) {
            logger.info("Bulk delete requested with empty transaction id list for user {}", userId);
            return;
        }

        List<Long> normalizedIds = transactionIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), ArrayList::new));

        if (normalizedIds.isEmpty()) {
            logger.info("Bulk delete requested with only null ids for user {}", userId);
            return;
        }

        int deletedCount = 0;
        for (int start = 0; start < normalizedIds.size(); start += BULK_DELETE_CHUNK_SIZE) {
            int end = Math.min(start + BULK_DELETE_CHUNK_SIZE, normalizedIds.size());
            List<Long> chunkIds = normalizedIds.subList(start, end);

            List<Transaction> transactions = transactionRepository.findAllByIdInAndCreatedByWithAccounts(chunkIds, userId);
            if (transactions.isEmpty()) {
                continue;
            }

            int missingCount = chunkIds.size() - transactions.size();
            if (missingCount > 0) {
                logger.warn("Some transactions not found for deletion for user {}. Not found ids count: {}", userId, missingCount);
            }

            applyDeletionBalanceDeltas(transactions);
            transactionRepository.deleteAllInBatch(transactions);

            // Keep persistence context bounded for large bulk requests.
            entityManager.flush();
            entityManager.clear();

            deletedCount += transactions.size();
        }

        logger.info("Bulk delete completed for user {}. Requested: {}, Deleted: {}",
                userId, normalizedIds.size(), deletedCount);
    }

    @Transactional
    public void deleteTransaction(String userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndCreatedBy(transactionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));

        applyDeletionBalanceDeltas(List.of(transaction));
        transactionRepository.delete(transaction);
        logger.info("Deleted transaction with id: {}", transactionId);
    }

    private void applyDeletionBalanceDeltas(List<Transaction> transactions) {
        Map<Long, BigDecimal> deltaByAccountId = new HashMap<>();
        Map<Long, Account> accountById = new HashMap<>();

        for (Transaction transaction : transactions) {
            addDeletionDelta(transaction, deltaByAccountId);
            if (transaction.getFromAccount() != null && transaction.getFromAccount().getId() != null) {
                accountById.putIfAbsent(transaction.getFromAccount().getId(), transaction.getFromAccount());
            }
            if (transaction.getToAccount() != null && transaction.getToAccount().getId() != null) {
                accountById.putIfAbsent(transaction.getToAccount().getId(), transaction.getToAccount());
            }
        }

        if (deltaByAccountId.isEmpty()) {
            return;
        }

        List<Account> accountsToUpdate = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : deltaByAccountId.entrySet()) {
            Long accountId = entry.getKey();
            BigDecimal delta = entry.getValue();
            // Skip accounts whose net delta is zero to avoid unnecessary DB writes
            if (delta == null || delta.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            Account account = accountById.get(accountId);
            BigDecimal oldBalance = account.getBalance();
            BigDecimal newBalance = oldBalance.add(delta);
            account.setBalance(newBalance);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0)
                logger.warn("Account {} balance will go negative ({} -> {}). Consider reviewing overdraft policy.",
                        accountId, oldBalance, newBalance);
            accountsToUpdate.add(account);
        }
        if (accountsToUpdate.isEmpty()) {
            return;
        }
        accountRepository.saveAll(accountsToUpdate);
    }

    private void addDeletionDelta(Transaction transaction, Map<Long, BigDecimal> deltaByAccountId) {
        BigDecimal amount = transaction.getTotalAmount();
        switch (transaction.getType()) {
            case INCOME -> {
                Long toAccountId = getRequiredAccountId(transaction.getToAccount(), "toAccount", transaction.getId());
                mergeDelta(deltaByAccountId, toAccountId, amount.negate());
            }
            case EXPENSE -> {
                Long fromAccountId = getRequiredAccountId(transaction.getFromAccount(), "fromAccount", transaction.getId());
                mergeDelta(deltaByAccountId, fromAccountId, amount);
            }
            case TRANSFER -> {
                Long fromAccountId = getRequiredAccountId(transaction.getFromAccount(), "fromAccount", transaction.getId());
                Long toAccountId = getRequiredAccountId(transaction.getToAccount(), "toAccount", transaction.getId());
                mergeDelta(deltaByAccountId, fromAccountId, amount);
                mergeDelta(deltaByAccountId, toAccountId, amount.negate());
            }
        }
    }

    private Long getRequiredAccountId(Account account, String fieldName, Long transactionId) {
        if (account == null || account.getId() == null) {
            throw new EntityNotFoundException("Invalid transaction data. Missing " + fieldName + " for transaction id: " + transactionId);
        }
        return account.getId();
    }

    private void mergeDelta(Map<Long, BigDecimal> deltaByAccountId, Long accountId, BigDecimal delta) {
        deltaByAccountId.merge(accountId, delta, BigDecimal::add);
    }

    private void handleAmountChange(Transaction transaction, BigDecimal amountDifference) {
        switch (transaction.getType()) {
            case INCOME -> updateAccountBalance(transaction.getToAccount(), amountDifference, true);
            case EXPENSE -> updateAccountBalance(transaction.getFromAccount(), amountDifference, false);
            case TRANSFER -> {
                updateAccountBalance(transaction.getFromAccount(), amountDifference, false);
                updateAccountBalance(transaction.getToAccount(), amountDifference, true);
            }
        }
    }

    private Account getAccount(String userId, Long accountId, String accountType) throws BadRequestException {
        if (accountId == null) {
            throw new BadRequestException(accountType + " id is null");
        }
        return accountRepository.findByIdAndUser_Id(accountId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + accountId));
    }

    private Category getCategory(String userId, Long categoryId, Type type) {
        Category category = categoryRepository.findCategoryByIdAndType(categoryId, type);
        if (category == null || (category.getCreatedBy() != null && !Objects.equals(category.getCreatedBy().getId(), userId))) {
            throw new EntityNotFoundException("Category not found with id: " + categoryId + " and type: " + type);
        }
        return category;
    }

    private Transaction buildTransaction(TransactionRequestDTO requestDTO, Account toAccount,
                                       Account fromAccount, Category category) {
        Transaction transaction = transactionMapper.toTransaction(requestDTO);
        transaction.setToAccount(toAccount);
        transaction.setFromAccount(fromAccount);
        transaction.setCategory(category);
        return transaction;
    }

    private void updateAccountBalance(Account account, BigDecimal amount, boolean isCredit) {
        BigDecimal newBalance = isCredit
            ? account.getBalance().add(amount)
            : account.getBalance().subtract(amount);

        account.setBalance(newBalance);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            logger.warn("Account {} balance will go negative ({} -> {}). Consider reviewing overdraft policy.",
                    account.getId(), account.getBalance(), newBalance);
        }
        accountRepository.save(account);
        logger.info("Updated balance in account: {}, New balance: {}", account.getId(), newBalance);
    }

    private void handleCurrency(Account account, Transaction transaction, TransactionRequestDTO requestDTO) {
        if (account.getCurrency() == null){
            throw new EntityNotFoundException("Account currency not set for account id: " + account.getId());
        }
        if (requestDTO.getCurrency() == null) {
            transaction.setCurrency(account.getCurrency());
        } else {
            SupportedCurrency currency = supportedCurrencyRepository.findByCode(requestDTO.getCurrency());
            if (currency == null) {
                throw new EntityNotFoundException("Currency not supported: " + requestDTO.getCurrency());
            }
            transaction.setCurrency(currency);
        }
    }
}
