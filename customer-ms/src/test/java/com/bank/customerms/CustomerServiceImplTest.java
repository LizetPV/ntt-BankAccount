package com.bank.customerms;

import com.bank.customerms.model.Customer;
import com.bank.customerms.repository.CustomerRepository;
import com.bank.customerms.service.AccountClient;
import com.bank.customerms.service.dto.CustomerRequest;
import com.bank.customerms.service.dto.CustomerResponse;
import com.bank.customerms.service.exception.BusinessException;
import com.bank.customerms.service.exception.NotFoundException;
import com.bank.customerms.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests para CustomerServiceImpl
 * - AAA: Cada test está en Arrange / Act / Assert
 * - FIRST: Rápidos, Independientes, Repetibles, Autovalidados, A tiempo
 * - YAGNI/KISS/DRY: Solo casos necesarios, simples y sin duplicar lógica
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock private CustomerRepository repository;
    @Mock private AccountClient accountClient;

    @InjectMocks
    private CustomerServiceImpl service; // SUT

    private CustomerRequest req;
    private Customer entity;

    @BeforeEach
    void setUp() {
        req = CustomerRequest.builder()
                .firstName("Yesi")
                .lastName("Peche")
                .dni("99990001")
                .email("yesi@test.com")
                .build();

        entity = Customer.builder()
                .id(1L)
                .firstName("Yesi")
                .lastName("Peche")
                .dni("99990001")
                .email("yesi@test.com")
                .build();
    }

    @Test
    void shouldCreateCustomer_whenDniNotExists() {
        // Arrange
        when(repository.existsByDni(req.getDni())).thenReturn(false);
        when(repository.save(any(Customer.class)))
                .thenAnswer(inv -> {
                    Customer c = inv.getArgument(0);
                    c.setId(1L);
                    return c;
                });

        // Act
        CustomerResponse resp = service.create(req);

        // Assert
        assertNotNull(resp.getId());
        assertEquals(req.getFirstName(), resp.getFirstName());
        assertEquals(req.getLastName(), resp.getLastName());
        assertEquals(req.getDni(), resp.getDni());
        assertEquals(req.getEmail(), resp.getEmail());
        verify(repository).existsByDni("99990001");
        verify(repository).save(any(Customer.class));
        verifyNoMoreInteractions(repository, accountClient);
    }

    @Test
    void shouldThrowConflict_whenDniAlreadyExists_onCreate() {
        // Arrange
        when(repository.existsByDni(req.getDni())).thenReturn(true);

        // Act + Assert
        assertThrows(DataIntegrityViolationException.class, () -> service.create(req));
        verify(repository).existsByDni("99990001");
        verifyNoMoreInteractions(repository, accountClient);
    }

    @Test
    void shouldFindById_whenExists() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        // Act
        CustomerResponse resp = service.findById(1L);

        // Assert
        assertEquals(1L, resp.getId());
        assertEquals("Yesi", resp.getFirstName());
        verify(repository).findById(1L);
        verifyNoMoreInteractions(repository, accountClient);
    }

    @Test
    void shouldThrowNotFound_whenFindByIdMissing() {
        // Arrange
        when(repository.findById(77L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> service.findById(77L));
        verify(repository).findById(77L);
        verifyNoMoreInteractions(repository, accountClient);
    }

    @Test
    void shouldListAllCustomers_mappedToDto() {
        // Arrange
        var e2 = Customer.builder().id(2L).firstName("Ana").lastName("Paz").dni("12345678").email("ana@test.com").build();
        when(repository.findAll()).thenReturn(List.of(entity, e2));

        // Act
        List<CustomerResponse> list = service.findAll();

        // Assert
        assertEquals(2, list.size());
        assertEquals("Yesi", list.get(0).getFirstName());
        assertEquals("Ana", list.get(1).getFirstName());
        verify(repository).findAll();
        verifyNoMoreInteractions(repository, accountClient);
    }

    @Test
    void shouldUpdateCustomer_whenExists() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        var updateReq = CustomerRequest.builder()
                .firstName("Yessi")
                .lastName("H.")
                .dni("99990001")
                .email("new@test.com")
                .build();

        // Act
        CustomerResponse resp = service.update(1L, updateReq);

        // Assert
        assertEquals("Yessi", resp.getFirstName());
        assertEquals("H.", resp.getLastName());
        assertEquals("new@test.com", resp.getEmail());
        verify(repository).findById(1L);
        verify(repository).save(any(Customer.class));
        verifyNoMoreInteractions(repository, accountClient);
    }

    @Test
    void shouldDelete_whenNoActiveAccounts() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(accountClient.hasActiveAccounts(1L)).thenReturn(CompletableFuture.completedFuture(false));

        // Act
        service.delete(1L);

        // Assert
        verify(repository).findById(1L);
        verify(accountClient).hasActiveAccounts(1L);
        verify(repository).delete(entity);
        verifyNoMoreInteractions(repository, accountClient);
    }

    @Test
    void shouldFailDelete_whenHasActiveAccounts() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(accountClient.hasActiveAccounts(1L)).thenReturn(CompletableFuture.completedFuture(true));

        // Act + Assert
        var ex = assertThrows(BusinessException.class, () -> service.delete(1L));
        assertTrue(ex.getMessage().toLowerCase().contains("cannot delete"));
        verify(repository).findById(1L);
        verify(accountClient).hasActiveAccounts(1L);
        verify(repository, never()).delete(any());
        verifyNoMoreInteractions(repository, accountClient);
    }

    @Test
    void shouldPropagateBusiness_whenAccountServiceUnavailable_onDelete() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        // Simula fallo del AccountMs (timeout / 500 etc.)
        var failed = new CompletableFuture<Boolean>();
        failed.completeExceptionally(new RuntimeException("boom"));
        when(accountClient.hasActiveAccounts(1L)).thenReturn(failed);

        // Act + Assert
        assertThrows(BusinessException.class, () -> service.delete(1L));
        verify(repository).findById(1L);
        verify(accountClient).hasActiveAccounts(1L);
        verify(repository, never()).delete(any());
        verifyNoMoreInteractions(repository, accountClient);
    }
}
