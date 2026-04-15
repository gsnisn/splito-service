package com.splito.service;

import com.splito.dto.request.UpdateUserRequest;
import com.splito.exception.UnauthorizedException;
import com.splito.model.SplitoUser;
import com.splito.repository.ExpenseRepository;
import com.splito.repository.GroupRepository;
import com.splito.repository.SettlementRepository;
import com.splito.repository.UserRepository;
import com.splito.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateMe_shouldUpdateNameAndPhoneOfAuthenticatedUser() {
        // given
        CustomUserDetails principal = new CustomUserDetails(1L, "shravan@test.com", "hashed-pwd");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        SplitoUser existingUser = new SplitoUser();
        existingUser.setId(1L);
        existingUser.setName("Old Name");
        existingUser.setEmail("shravan@test.com");
        existingUser.setPhone("9999999999");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Shravan Gupta");
        request.setPhone("+919876543210");

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(existingUser));
        when(userRepository.save(any(SplitoUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SplitoUser updated = userService.updateMe(request);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getName()).isEqualTo("Shravan Gupta");
        assertThat(updated.getPhone()).isEqualTo("+919876543210");
        assertThat(updated.getEmail()).isEqualTo("shravan@test.com");

        verify(userRepository).findById(1L);
        verify(userRepository).save(existingUser);
    }

    @Test
    void me_shouldThrowUnauthorizedWhenNoAuthenticationPresent() {
        // given
        SecurityContextHolder.clearContext();

        // when / then
        assertThatThrownBy(() -> userService.me())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Unauthenticated");
    }
}