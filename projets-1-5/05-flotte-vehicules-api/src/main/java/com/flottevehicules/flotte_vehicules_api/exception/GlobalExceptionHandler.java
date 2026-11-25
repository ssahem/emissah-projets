package com.flottevehicules.flotte_vehicules_api.exception;

import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traducteur central des erreurs de l'API.
 *
 * COUCHE       : Exception
 * RÔLE         : convertir toute exception en Problem Detail (RFC 9457).
 * HÉRITAGE     : ResponseEntityExceptionHandler couvre déjà les erreurs MVC standard.
 * NE FAIT PAS  : de logique métier ; aucun détail SQL n'est exposé au client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE = "https://api.flotte-vehicules.local/problems/";


    // ── handleApiException() ─────────────────────────────────────────────────

    /**
     * Traduit une erreur métier typée en Problem Detail.
     */

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problem.setTitle(ex.getTitle());
        problem.setType(ex.getType());
        ex.getProperties().forEach(problem::setProperty);
        log.warn("Refus métier {} : {}", ex.getType(), ex.getMessage());
        return problem;
    }


    // ── handleConstraintViolation() ──────────────────────────────────────────

    /**
     * Traduit une contrainte violée sur un paramètre de requête en 400 lisible.
     */

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Paramètre de requête invalide");
        problem.setTitle("Paramètre invalide");
        problem.setType(URI.create(BASE + "parametre-invalide"));
        String parametre = ex.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getPropertyPath().toString())
                .orElse("inconnu");
        problem.setProperty("parametre", parametre);
        return problem;
    }


    // ── handleMethodArgumentNotValid() ───────────────────────────────────────

    /**
     * Traduit un corps invalide en 400 en indiquant les champs en erreur.
     */

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Le corps de la requête est invalide");
        problem.setTitle("Corps invalide");
        problem.setType(URI.create(BASE + "corps-invalide"));
        List<Map<String, String>> champs = ex.getBindingResult().getFieldErrors().stream()
                .map(erreur -> Map.of(
                        "champ", erreur.getField(),
                        "message", String.valueOf(erreur.getDefaultMessage())))
                .toList();
        problem.setProperty("champs", champs);
        log.warn("Corps invalide : {}", champs);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }


    // ── handleIntegrityViolation() ───────────────────────────────────────────

    /**
     * Traduit une violation de contrainte sans jamais exposer le message SQL.
     */

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegrityViolation(DataIntegrityViolationException ex) {
        String cause = String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase(Locale.ROOT);
        boolean contrainteDeForme = cause.contains("check constraint");
        HttpStatus statut = contrainteDeForme ? HttpStatus.BAD_REQUEST : HttpStatus.CONFLICT;
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(statut, contrainteDeForme
                ? "La valeur ne respecte pas une règle de données"
                : "Cette opération viole une contrainte de données");
        problem.setTitle(contrainteDeForme ? "Donnée invalide" : "Conflit de données");
        problem.setType(URI.create(BASE
                + (contrainteDeForme ? "donnee-invalide" : "conflit-donnees")));
        log.warn("Violation de contrainte base ({}) : {}", statut, cause);
        return problem;
    }


    // ── handleUnexpected() ───────────────────────────────────────────────────

    /**
     * Cache les détails techniques au client et les conserve dans les logs
     * serveur.
     */

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur inattendue est survenue");
        problem.setTitle("Erreur interne");
        problem.setType(URI.create(BASE + "erreur-interne"));
        log.error("Erreur inattendue", ex);
        return problem;
    }
}
