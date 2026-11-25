package com.suiviscolaire.suivi_scolaire_api.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.suiviscolaire.suivi_scolaire_api.dto.request.ChangeClasseRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateClasseRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateEleveRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateNoteRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.response.AlerteDifficulteResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.ClasseResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.ClotureResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.EffectifClasseResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.EleveResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.MoyenneGeneraleResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.MoyennesResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.NoteResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.PageResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.PoidsResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.ReouvertureResponse;
import com.suiviscolaire.suivi_scolaire_api.exception.ConflitMetierException;
import com.suiviscolaire.suivi_scolaire_api.exception.GlobalExceptionHandler;
import com.suiviscolaire.suivi_scolaire_api.exception.RessourceIntrouvableException;
import com.suiviscolaire.suivi_scolaire_api.service.AlertesDifficulteService;
import com.suiviscolaire.suivi_scolaire_api.service.ClasseService;
import com.suiviscolaire.suivi_scolaire_api.service.ConsultationService;
import com.suiviscolaire.suivi_scolaire_api.service.EleveService;
import com.suiviscolaire.suivi_scolaire_api.service.MatiereService;
import com.suiviscolaire.suivi_scolaire_api.service.NoteService;
import com.suiviscolaire.suivi_scolaire_api.service.ProfesseurService;
import com.suiviscolaire.suivi_scolaire_api.service.ReglesMetierService;
import com.suiviscolaire.suivi_scolaire_api.service.RestitutionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

/**
 * Contrat HTTP de l'API publique : URLs, codes, Problem Details et absence d'authentification.
 *
 * COUCHE       : Service
 * RÔLE         : Contrat HTTP de l'API publique : URLs, codes, Problem Details et absence d'authentification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@WebMvcTest
@Import(GlobalExceptionHandler.class)
class ControllerWebTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper objectMapper;

    @MockitoBean private ClasseService classeService;
    @MockitoBean private EleveService eleveService;
    @MockitoBean private MatiereService matiereService;
    @MockitoBean private ProfesseurService professeurService;
    @MockitoBean private NoteService noteService;
    @MockitoBean private ConsultationService consultationService;
    @MockitoBean private ReglesMetierService reglesMetierService;
    @MockitoBean private AlertesDifficulteService alertesService;

    // ── createClasseReturns201() ─────────────────────────────────────────────

    @MockitoBean private RestitutionService restitutionService;

    @Test
    @DisplayName("POST classe/create renvoie 201 et un en-tête Location")
    void createClasseReturns201() throws Exception {
        when(classeService.create(any(CreateClasseRequest.class)))
                .thenReturn(new ClasseResponse(1, "6A", 2, 1, 30, 0));

        mockMvc.perform(post("/suivi-scolaire/classe/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nom", "6A", "idAnnee", 2, "idNiveau", 1, "effectifMax", 30))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/suivi-scolaire/classe/get/1"))
                .andExpect(jsonPath("$.nom").value("6A"));
    }

    // ── listClassesReturns200() ──────────────────────────────────────────────

    @Test
    @DisplayName("GET classe/list renvoie les classes")
    void listClassesReturns200() throws Exception {
        when(classeService.findAll()).thenReturn(List.of(new ClasseResponse(1, "6A", 2, 1, 30, 4)));

        mockMvc.perform(get("/suivi-scolaire/classe/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].effectifActuel").value(4));
    }

    // ── elevesClasseReturns200() ─────────────────────────────────────────────

    @Test
    @DisplayName("GET classe/eleves/{id} renvoie l'effectif détaillé")
    void elevesClasseReturns200() throws Exception {
        when(consultationService.elevesDeClasse(1)).thenReturn(new EffectifClasseResponse(
                1, "6A", 1, 30, List.of(eleve())));

        mockMvc.perform(get("/suivi-scolaire/classe/eleves/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomClasse").value("6A"))
                .andExpect(jsonPath("$.eleves[0].nom").value("Kouassi"));
    }

    // ── deleteClasseReturns204() ─────────────────────────────────────────────

    @Test
    @DisplayName("DELETE classe/delete/{id} renvoie 204")
    void deleteClasseReturns204() throws Exception {
        mockMvc.perform(delete("/suivi-scolaire/classe/delete/1"))
                .andExpect(status().isNoContent());
        verify(classeService).delete(1);
    }

    // ── createEleveReturns201() ──────────────────────────────────────────────

    @Test
    @DisplayName("POST eleve/create renvoie 201")
    void createEleveReturns201() throws Exception {
        when(eleveService.create(any(CreateEleveRequest.class))).thenReturn(eleve());

        mockMvc.perform(post("/suivi-scolaire/eleve/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nom", "Kouassi", "prenom", "Aya",
                                "dateNaissance", "2011-02-14", "idClasse", 1))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/suivi-scolaire/eleve/get/1"));
    }

    // ── listElevesReturns200() ───────────────────────────────────────────────

    @Test
    @DisplayName("GET eleve/list sans paramètre appelle la liste complète")
    void listElevesReturns200() throws Exception {
        when(eleveService.findAll()).thenReturn(List.of(eleve()));

        mockMvc.perform(get("/suivi-scolaire/eleve/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].prenom").value("Aya"));
        verify(eleveService).findAll();
    }

    // ── searchElevesReturns200() ─────────────────────────────────────────────

    @Test
    @DisplayName("GET eleve/list?nom= appelle la recherche partielle")
    void searchElevesReturns200() throws Exception {
        when(consultationService.rechercher("kou")).thenReturn(List.of(eleve()));

        mockMvc.perform(get("/suivi-scolaire/eleve/list").param("nom", "kou"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Kouassi"));
        verify(consultationService).rechercher("kou");
    }

    // ── changerClasseReturns201() ────────────────────────────────────────────

    @Test
    @DisplayName("POST eleve/affectation/{id} renvoie 201")
    void changerClasseReturns201() throws Exception {
        when(eleveService.changerClasse(eq(1), any(ChangeClasseRequest.class)))
                .thenReturn(new com.suiviscolaire.suivi_scolaire_api.dto.response
                        .ChangementClasseResponse(1, 1, 2, LocalDate.of(2026, 11, 24),
                        LocalDate.of(2026, 11, 24), 2));

        mockMvc.perform(post("/suivi-scolaire/eleve/affectation/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idClasseDestination\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nouvelleClasse").value(2));
    }

    // ── affecterProfesseurReturns201() ───────────────────────────────────────

    @Test
    @DisplayName("POST professeur/affectations/{id} renvoie 201")
    void affecterProfesseurReturns201() throws Exception {
        when(professeurService.affecter(eq(2), any()))
                .thenReturn(new com.suiviscolaire.suivi_scolaire_api.dto.response
                        .AffectationResponse(9, 2, 1, 1));

        mockMvc.perform(post("/suivi-scolaire/professeur/affectations/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idMatiere\":1,\"idClasse\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEnseignement").value(9));
    }

    // ── createNoteReturns201() ───────────────────────────────────────────────

    @Test
    @DisplayName("POST note/create renvoie 201")
    void createNoteReturns201() throws Exception {
        when(noteService.create(any(CreateNoteRequest.class))).thenReturn(note());
        when(noteService.location(20)).thenReturn(
                java.net.URI.create("/suivi-scolaire/note/get/20"));

        mockMvc.perform(post("/suivi-scolaire/note/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idEleve", 1, "idMatiere", 1, "valeur", 14.5,
                                "trimestre", 2, "idTypeEval", 1))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/suivi-scolaire/note/get/20"))
                .andExpect(jsonPath("$.valeur").value(14.5));
    }

    // ── listNotesGroupeesReturns200() ────────────────────────────────────────

    @Test
    @DisplayName("GET note/list sans paramètre renvoie les notes groupées par matière")
    void listNotesGroupeesReturns200() throws Exception {
        when(noteService.findAll()).thenReturn(List.of(
                new com.suiviscolaire.suivi_scolaire_api.dto.response
                        .NotesMatiereResponse(1, "Mathematiques", List.of(note()))));

        mockMvc.perform(get("/suivi-scolaire/note/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].libelleMatiere").value("Mathematiques"))
                .andExpect(jsonPath("$[0].notes[0].idNote").value(20));
    }

    // ── listNotesPagineesReturns200() ────────────────────────────────────────

    @Test
    @DisplayName("GET note/list?page=... renvoie une page")
    void listNotesPagineesReturns200() throws Exception {
        when(consultationService.notesPaginees(0, 2, "valeur,desc"))
                .thenReturn(new PageResponse<>(List.of(note()), 0, 2, 15, 8));

        mockMvc.perform(get("/suivi-scolaire/note/list")
                        .param("page", "0").param("size", "2").param("sort", "valeur,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.content[0].idNote").value(20));
    }

    // ── modifierPoidsReturns200() ────────────────────────────────────────────

    @Test
    @DisplayName("PUT type-evaluation/poids/DEV renvoie le nouveau poids")
    void modifierPoidsReturns200() throws Exception {
        when(reglesMetierService.modifierPoids("DEV", new BigDecimal("2.5")))
                .thenReturn(new PoidsResponse("DEV", "Devoir", new BigDecimal("2.5")));

        mockMvc.perform(put("/suivi-scolaire/type-evaluation/poids/DEV")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"poids\":2.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Devoir"))
                .andExpect(jsonPath("$.poids").value(2.5));
    }

    // ── cloturerTrimestreReturns200() ────────────────────────────────────────

    @Test
    @DisplayName("POST trimestre/{numero}/cloture renvoie la date de clôture")
    void cloturerTrimestreReturns200() throws Exception {
        when(reglesMetierService.cloturer(1)).thenReturn(new ClotureResponse(
                1, LocalDate.of(2026, 11, 24), 12));

        mockMvc.perform(post("/suivi-scolaire/trimestre/1/cloture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateCloture").value("2026-11-24"))
                .andExpect(jsonPath("$.nombreEnseignementsVerifies").value(12));
    }

    // ── reouvrirTrimestreReturns200() ────────────────────────────────────────

    @Test
    @DisplayName("POST trimestre/{numero}/reouverture journalise motif et auteur")
    void reouvrirTrimestreReturns200() throws Exception {
        when(reglesMetierService.reouvrir(3, MOTIF, "Secrétariat")).thenReturn(
                new ReouvertureResponse(4, 3, LocalDate.of(2026, 11, 24), MOTIF, "Secrétariat"));

        mockMvc.perform(post("/suivi-scolaire/trimestre/3/reouverture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "motif", MOTIF, "auteur", "Secrétariat"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idHistorique").value(4))
                .andExpect(jsonPath("$.auteur").value("Secrétariat"));
    }

    // ── listAlertesReturns200() ──────────────────────────────────────────────

    @Test
    @DisplayName("GET alerte/list?trimestre= renvoie les alertes")
    void listAlertesReturns200() throws Exception {
        when(alertesService.consulter(2)).thenReturn(List.of(new AlerteDifficulteResponse(
                7, 10, 1, "Kouassi", 1, "6A", 2, new BigDecimal("7.00"),
                new BigDecimal("2.00"), "Moyenne faible", LocalDate.of(2026, 11, 24))));

        mockMvc.perform(get("/suivi-scolaire/alerte/list").param("trimestre", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomEleve").value("Kouassi"))
                .andExpect(jsonPath("$[0].trimestre").value(2));
    }

    // ── restitutionMoyennesReturns200() ──────────────────────────────────────

    @Test
    @DisplayName("GET eleve/moyennes et moyenne-generale renvoient les restitutions F17/F18")
    void restitutionMoyennesReturns200() throws Exception {
        when(restitutionService.moyennesParMatiere(1, 2)).thenReturn(new MoyennesResponse(1, 2, List.of()));
        when(restitutionService.moyenneGenerale(1, 2))
                .thenReturn(new MoyenneGeneraleResponse(1, 2, new BigDecimal("13.20"), List.of()));

        mockMvc.perform(get("/suivi-scolaire/eleve/moyennes/1").param("trimestre", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEleve").value(1));
        mockMvc.perform(get("/suivi-scolaire/eleve/moyenne-generale/1").param("trimestre", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moyenneGenerale").value(13.20));
    }

    // ── invalidBodyReturns400() ──────────────────────────────────────────────

    @Test
    @DisplayName("Un corps invalide renvoie 400 avec la liste des champs")
    void invalidBodyReturns400() throws Exception {
        mockMvc.perform(post("/suivi-scolaire/classe/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"\",\"idAnnee\":null,\"idNiveau\":null,\"effectifMax\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Corps invalide"))
                .andExpect(jsonPath("$.champs").isArray());
    }

    // ── introuvableReturns404() ──────────────────────────────────────────────

    @Test
    @DisplayName("Une ressource absente renvoie 404 en Problem Details")
    void introuvableReturns404() throws Exception {
        when(classeService.findById(99)).thenThrow(new RessourceIntrouvableException("Classe", 99));

        mockMvc.perform(get("/suivi-scolaire/classe/get/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Ressource introuvable"))
                .andExpect(jsonPath("$.type").value(
                        "https://api.suivi-scolaire.local/problems/ressource-introuvable"));
    }

    // ── conflitReturns409() ──────────────────────────────────────────────────

    @Test
    @DisplayName("Un conflit métier renvoie 409 avec son type de problème")
    void conflitReturns409() throws Exception {
        when(classeService.create(any(CreateClasseRequest.class)))
                .thenThrow(new ConflitMetierException("classe-nom-deja-utilise", "Nom pris"));

        mockMvc.perform(post("/suivi-scolaire/classe/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nom", "6A", "idAnnee", 2, "idNiveau", 1, "effectifMax", 30))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(
                        "https://api.suivi-scolaire.local/problems/classe-nom-deja-utilise"));
    }

    private static final String MOTIF = "Correction d'une erreur de saisie avérée";

    /** Réponse élève nominative. */

    // ── eleve() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour eleve().
     */

    private EleveResponse eleve() {
        return new EleveResponse(1, "Kouassi", "Aya", LocalDate.of(2011, 2, 14), 1, "6A");
    }

    /** Réponse note nominative. */

    // ── note() ───────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour note().
     */

    private NoteResponse note() {
        return new NoteResponse(20, 1, "Kouassi", 1, "Mathematiques", 1, "6A",
                new BigDecimal("14.5"), 2, 1, "Devoir", BigDecimal.ONE,
                LocalDate.of(2026, 11, 1));
    }
}
