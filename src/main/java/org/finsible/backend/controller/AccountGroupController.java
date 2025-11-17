package org.finsible.backend.controller;

import org.apache.coyote.BadRequestException;
import org.finsible.backend.BaseResponse;
import org.finsible.backend.dto.request.AccountGroupRequestDTO;
import org.finsible.backend.dto.request.groups.Create;
import org.finsible.backend.dto.request.groups.Update;
import org.finsible.backend.dto.response.AccountGroupResponseDTO;
import org.finsible.backend.service.AccountGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/account-groups")
public class AccountGroupController {
    private final AccountGroupService accountGroupService;

    public AccountGroupController(AccountGroupService accountGroupService) {
        this.accountGroupService = accountGroupService;
    }

    @GetMapping("/all")
    public ResponseEntity<BaseResponse<List<AccountGroupResponseDTO>>> getAccountGroups(@RequestAttribute String userId) {
        List<AccountGroupResponseDTO> accountGroups = accountGroupService.getAccountGroups(userId);
        return ResponseEntity.ok(new BaseResponse<>("Account groups fetched successfully", true, accountGroups));
    }

    @PostMapping("/")
    public ResponseEntity<BaseResponse<AccountGroupResponseDTO>> createAccountGroup(@RequestAttribute String userId,
                                                                                    @Validated(Create.class) @RequestBody AccountGroupRequestDTO accountGroupRequestDTO) {
        AccountGroupResponseDTO responseDTO = accountGroupService.createAccountGroup(userId, accountGroupRequestDTO);
        return ResponseEntity.ok(
                new BaseResponse<>("Account group created successfully", true,
                        responseDTO)
        );
    }

    @PutMapping("/{accountGroupId}")
    public ResponseEntity<BaseResponse<AccountGroupResponseDTO>> updateAccountGroup(@RequestAttribute String userId, @PathVariable Long accountGroupId,
                                                                                    @Validated(Update.class) @RequestBody AccountGroupRequestDTO accountGroupRequestDTO)
            throws BadRequestException {
        AccountGroupResponseDTO responseDTO = accountGroupService.updateAccountGroup(userId, accountGroupId, accountGroupRequestDTO);
        return ResponseEntity.ok(
                new BaseResponse<>("Account group updated successfully", true,
                        responseDTO)
        );
    }

    @DeleteMapping("/{accountGroupId}")
    public ResponseEntity<BaseResponse<Void>> deleteAccountGroup(@RequestAttribute String userId, @PathVariable Long accountGroupId) throws BadRequestException {
        accountGroupService.deleteAccountGroup(userId, accountGroupId);
        return ResponseEntity.ok(new BaseResponse<>("Account group deleted successfully", true, null));
    }
}
