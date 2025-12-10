package org.finsible.backend.mapper;

import org.finsible.backend.dto.request.TransactionRequestDTO;
import org.finsible.backend.dto.response.TransactionResponseDTO;
import org.finsible.backend.entity.Transaction;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "space", ignore = true)
    @Mapping(target = "fromAccount", ignore = true)
    @Mapping(target = "toAccount", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(source = "isSplit", target = "isSplit", defaultValue = "false")
    Transaction toTransaction(TransactionRequestDTO transactionRequestDTO);

    @Mapping(source = "space.id", target = "spaceId")
    @Mapping(source = "fromAccount.id", target = "fromAccountId")
    @Mapping(source = "toAccount.id", target = "toAccountId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "currency.code", target = "currency")
    @Mapping(source = "isSplit", target = "isSplit", defaultValue = "false")
    TransactionResponseDTO toTransactionResponseDTO(Transaction transaction);

    @Mapping(target = "space", ignore = true)
    @Mapping(target = "fromAccount", ignore = true)
    @Mapping(target = "toAccount", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "isSplit", target = "isSplit", defaultValue = "false")
    void updateTransaction(TransactionRequestDTO transactionRequestDTO, @MappingTarget Transaction transaction);
}
