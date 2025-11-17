package org.finsible.backend.service;

import org.apache.coyote.BadRequestException;
import org.finsible.backend.AppConstants;
import org.finsible.backend.CustomExceptionHandler.EntityNotFoundException;
import org.finsible.backend.CustomExceptionHandler.UserNotFoundException;
import org.finsible.backend.dto.request.AccountGroupRequestDTO;
import org.finsible.backend.dto.response.AccountGroupResponseDTO;
import org.finsible.backend.entity.AccountGroup;
import org.finsible.backend.entity.User;
import org.finsible.backend.mapper.AccountGroupMapper;
import org.finsible.backend.repository.AccountGroupRepository;
import org.finsible.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AccountGroupService {
    private static final Logger logger = LoggerFactory.getLogger(AccountGroupService.class);
    private final AccountGroupRepository accountGroupRepository;
    private final AccountGroupMapper accountGroupMapper;
    private final UserRepository userRepository;

    public AccountGroupService(AccountGroupRepository accountGroupRepository, AccountGroupMapper accountGroupMapper, UserRepository userRepository) {
        this.accountGroupRepository = accountGroupRepository;
        this.accountGroupMapper = accountGroupMapper;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountGroupResponseDTO> getAccountGroups(String userId) {
        List<AccountGroup> accountGroups = accountGroupRepository.findByCreatedBy_IdOrIsSystemDefaultTrueOrderByDisplayOrder(userId);
        logger.info("Found {} accountGroups for user {}", accountGroups.size(), userId);
        return accountGroups.stream().map(accountGroupMapper::toAccountGroupResponseDTO).toList();
    }

    @Transactional
    public AccountGroupResponseDTO createAccountGroup(String userId, AccountGroupRequestDTO accountGroupRequestDTO) {
        User currentUser = userRepository.findById(userId).orElse(null);
        if(currentUser == null) {
            throw new UserNotFoundException(AppConstants.USER_NOT_FOUND_EXCEPTION);
        }
        AccountGroup accountGroup = accountGroupMapper.toAccountGroup(accountGroupRequestDTO);
        accountGroup.setCreatedBy(currentUser);
        accountGroupRepository.save(accountGroup);
        logger.info("Account group created with id: {} for user with id: {}", accountGroup.getId(), userId);
        return accountGroupMapper.toAccountGroupResponseDTO(accountGroup);
    }

    @Transactional
    public AccountGroupResponseDTO updateAccountGroup(String userId, Long accountGroupId, AccountGroupRequestDTO accountGroupRequestDTO) throws BadRequestException {
        User currentUser = userRepository.findById(userId).orElse(null);
        if(currentUser == null) {
            throw new UserNotFoundException(AppConstants.USER_NOT_FOUND_EXCEPTION);
        }
        AccountGroup existingAccountGroup = accountGroupRepository.findAccountGroupByIdAndCreatedBy(accountGroupId, currentUser);
        if(existingAccountGroup == null) {
            logger.warn("Account group with id: {} not found for update by user with id: {}", accountGroupId, userId);
            throw new EntityNotFoundException("Account group not found");
        }
        if(existingAccountGroup.getIsSystemDefault()) {
            logger.warn("Attempt to update system default account group with id: {} by user with id: {}", accountGroupId, userId);
            throw new BadRequestException("Cannot update system default account group");
        }
        accountGroupMapper.updateAccountGroupFromDto(accountGroupRequestDTO, existingAccountGroup);
        accountGroupRepository.save(existingAccountGroup);
        logger.info("Account group with id: {} updated by user with id: {}", accountGroupId, userId);
        return accountGroupMapper.toAccountGroupResponseDTO(existingAccountGroup);
    }

    @Transactional
    public void deleteAccountGroup(String userId, Long accountGroupId) throws BadRequestException {
        User currentUser = userRepository.findById(userId).orElse(null);
        if(currentUser == null) {
            throw new UserNotFoundException(AppConstants.USER_NOT_FOUND_EXCEPTION);
        }
        AccountGroup accountGroup = accountGroupRepository.findAccountGroupByIdAndCreatedBy(accountGroupId, currentUser);
        if(accountGroup == null) {
            logger.warn("Account group with id: {} not found for deletion by user with id: {}", accountGroupId, userId);
            throw new EntityNotFoundException("Account group not found");
        }
        if(accountGroup.getIsSystemDefault()) {
            logger.warn("Attempt to delete system default account group with id: {} by user with id: {}", accountGroupId, userId);
            throw new BadRequestException("Cannot delete system default account group");
        }
        accountGroupRepository.delete(accountGroup);
        logger.info("Account group with id: {} deleted by user with id: {}", accountGroupId, userId);
    }
}
