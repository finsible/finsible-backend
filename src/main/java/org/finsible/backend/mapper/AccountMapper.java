package org.finsible.backend.mapper;

import org.finsible.backend.dto.request.AccountRequestDTO;
import org.finsible.backend.dto.request.CreditCardAccountRequestDTO;
import org.finsible.backend.dto.request.DebitCardAccountRequestDTO;
import org.finsible.backend.dto.response.AccountResponseDTO;
import org.finsible.backend.entity.Account;
import org.finsible.backend.entity.CreditCardDetail;
import org.finsible.backend.entity.DebitCardDetail;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account toAccount(AccountRequestDTO accountRequestDTO);

    Account toAccount(DebitCardAccountRequestDTO debitCardAccountRequestDTO);
    DebitCardDetail toDebitCardDetail(DebitCardAccountRequestDTO debitCardAccountRequestDTO);

    Account toAccount(CreditCardAccountRequestDTO creditCardAccountRequestDTO);
    CreditCardDetail toCreditCardDetail(CreditCardAccountRequestDTO creditCardAccountRequestDTO);

    @Mapping(source = "account.accountGroup.id", target = "accountGroupId")
    @Mapping(source = "account.currency.code", target = "currencyCode")
    AccountResponseDTO toAccountResponseDTO(Account account);

    @Mapping(source = "autoPayFromAccount.id", target = "autoPayFromAccountId")
    void CreditCardAccountResponse(CreditCardDetail creditCardDetail, @MappingTarget AccountResponseDTO accountResponseDTO);

    @Mapping(source = "linkedBankAccount.id", target = "linkedBankAccountId")
    void DebitCardAccountResponse(DebitCardDetail debitCardDetail, @MappingTarget AccountResponseDTO accountResponseDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAccountFromDto(AccountRequestDTO dto, @MappingTarget Account account);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDebitCardDetailFromDto(DebitCardAccountRequestDTO dto, @MappingTarget DebitCardDetail debitCardDetail);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAccountFromDto(DebitCardAccountRequestDTO dto, @MappingTarget Account account);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAccountFromDto(CreditCardAccountRequestDTO dto, @MappingTarget Account account);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCreditCardDetailFromDto(CreditCardAccountRequestDTO dto, @MappingTarget CreditCardDetail creditCardDetail);
}
