package com.billetterie.billetterie_api.exception;

import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.List;
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
 * RÔLE         : convertir toute exception métier en Problem Detail (RFC 9457).
 * HÉRITAGE     : ResponseEntityExceptionHandler couvre déjà les erreurs MVC standard (400, 405…).
 * NE FAIT PAS  : de logique métier ; il ne fait que formater l'erreur.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── handleApiException() — ApiException → statut porté par l'exception ───

    /** Traduit une erreur métier typée en Problem Detail. */
    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex) {
        // ── Étape 1/3 : créer le problème avec le statut et le détail ──
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());

        // ── Étape 2/3 : renseigner le titre et le type d'erreur ──
        problem.setTitle(ex.getTitle());
        problem.setType(ex.getType());

        // ── Étape 3/3 : ajouter les données métier utiles et tracer sans pile ──
        ex.getProperties().forEach(problem::setProperty);
        log.warn("Refus métier {} : {}", ex.getType(), ex.getMessage());
        return problem;
    }

    // ── handleConstraintViolation() — paramètre invalide → 400 ───────────────

    /** Traduit une contrainte violée sur un paramètre de requête en 400 lisible. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        // ── Étape 1/3 : créer le problème avec le statut et le détail ──
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Paramètre de requête invalide");

        // ── Étape 2/3 : titrer et typer ──
        problem.setTitle("Paramètre invalide");
        problem.setType(URI.create("https://api.billetterie.local/problems/parametre-invalide"));

        // ── Étape 3/3 : indiquer le paramètre fautif, sans exposer la pile ──
        String parametre = ex.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath().toString())
                .orElse("inconnu");
        problem.setProperty("parametre", parametre);
        log.warn("Paramètre invalide : {}", parametre);
        return problem;
    }

    // ── handleMethodArgumentNotValid() — corps invalide → 400 avec les champs ─

    /** Traduit un corps invalide en 400 en indiquant les champs en erreur. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        // ── Étape 1/3 : créer le problème avec le statut et le détail ──
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Le corps de la requête est invalide");

        // ── Étape 2/3 : titrer et typer ──
        problem.setTitle("Corps invalide");
        problem.setType(URI.create("https://api.billetterie.local/problems/corps-invalide"));

        // ── Étape 3/3 : lister les champs fautifs, sans exposer la pile ──
        List<Map<String, String>> champs = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> Map.of(
                        "champ", e.getField(),
                        "message", String.valueOf(e.getDefaultMessage())))
                .toList();
        problem.setProperty("champs", champs);
        log.warn("Corps invalide : {}", champs);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    // ── handleIntegrityViolation() — conflit de contrainte → 409 ─────────────

    /** Traduit une violation d'unicité en conflit métier, sans exposer le SQL. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegrityViolation(DataIntegrityViolationException ex) {
        // ── Étape 1/3 : créer le problème ──
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "Cette opération viole une contrainte de données");

        // ── Étape 2/3 : titrer et typer ──
        problem.setTitle("Conflit de données");
        problem.setType(URI.create("https://api.billetterie.local/problems/conflit-donnees"));

        // ── Étape 3/3 : journaliser la cause technique côté serveur uniquement ──
        log.warn("Violation de contrainte base : {}", ex.getMostSpecificCause().getMessage());
        return problem;
    }

    // ── handleUnexpected() — erreur inattendue → 500 générique ───────────────

    /** Cache les détails techniques au client et les conserve dans les logs serveur. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        // ── Étape 1/3 : créer un problème générique ──
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur inattendue est survenue");

        // ── Étape 2/3 : titrer et typer sans détail interne ──
        problem.setTitle("Erreur interne");
        problem.setType(URI.create("https://api.billetterie.local/problems/erreur-interne"));

        // ── Étape 3/3 : tracer la cause côté serveur seulement ──
        log.error("Erreur inattendue", ex);
        return problem;
    }
}
