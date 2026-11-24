package com.gestionlocative.gestion_locative_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.gestionlocative.gestion_locative_api.entity.Bail;
import com.gestionlocative.gestion_locative_api.entity.HistoriqueBail;
import com.gestionlocative.gestion_locative_api.enums.StatutBail;
import com.gestionlocative.gestion_locative_api.repository.BailRepository;
import com.gestionlocative.gestion_locative_api.repository.HistoriqueBailRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests unitaires de la clôture automatique des baux échus (F16).
 *
 * TYPE       : Unitaire (Mockito)
 * STRATÉGIE  : repositories simulés, horloge fixe au 15 juin 2026.
 * CONVENTION : Préparation → Action → Vérification.
 */
@ExtendWith(MockitoExtension.class)
class BailClotureServiceTest {

    @Mock private BailRepository bailRepository;
    @Mock private HistoriqueBailRepository historiqueBailRepository;

    private BailClotureService service;

    @BeforeEach
    void setUp() {
        Clock horlogeFixe = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);
        service = new BailClotureService(bailRepository, historiqueBailRepository, horlogeFixe);
    }

    @Test
    @DisplayName("F16 — un bail échu passe TERMINE et sa clôture est tracée")
    void cloturerBauxEchus_clotureEtTrace() {
        // ── Préparation ──
        Bail echu = new Bail(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 31),
                BigDecimal.ZERO, BigDecimal.ZERO, 1, new BigDecimal("850.00"));
        ReflectionTestUtils.setField(echu, "idBail", 3);
        when(bailRepository.findByStatutAndDateFinBefore(
                StatutBail.EN_COURS, LocalDate.of(2026, 6, 15))).thenReturn(List.of(echu));

        // ── Action ──
        int nombre = service.cloturerBauxEchus();

        // ── Vérification ──
        assertThat(nombre).isEqualTo(1);
        assertThat(echu.getStatut()).isEqualTo(StatutBail.TERMINE);
        assertThat(echu.getDateCloture()).isEqualTo(LocalDate.of(2026, 6, 15));
        verify(historiqueBailRepository).saveAndFlush(any(HistoriqueBail.class));
    }

    @Test
    @DisplayName("F16 — sans bail échu, aucune écriture n'est effectuée")
    void cloturerBauxEchus_sansEchuNEcritRien() {
        // ── Préparation ──
        when(bailRepository.findByStatutAndDateFinBefore(
                StatutBail.EN_COURS, LocalDate.of(2026, 6, 15))).thenReturn(List.of());

        // ── Action + Vérification ──
        assertThat(service.cloturerBauxEchus()).isZero();
        verifyNoInteractions(historiqueBailRepository);
    }
}
