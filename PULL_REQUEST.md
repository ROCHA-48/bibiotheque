# 🔐 Sécurisation et tests du module Réservation

## Résumé

Fermeture de l'API de réservation (ouverte à tous auparavant) avec Spring Security + JWT :
authentification obligatoire, autorisations par rôle, cloisonnement par adhérent, identité
issue du token. Prouvé par des tests automatisés.

## Sécurité — une phrase par règle (RS-01 à RS-05)

- **RS-01** — Sans token, tout endpoint de réservation renvoie **401** : implémenté dans
  `WebSecurityConfiguration` (`anyRequest().authenticated()`) avec `JwtAuthenticationEntryPoint`
  qui renvoie `SC_UNAUTHORIZED`.

- **RS-02** — Un ADHERENT qui tente une action réservée au bibliothécaire reçoit **403** :
  implémenté par `@PreAuthorize("hasRole('BIBLIOTHECAIRE')")` sur `DELETE /api/reservations/{id}`
  dans `ReservationController`.

- **RS-03** — Un ADHERENT qui accède à la réservation d'un autre reçoit **403** : implémenté dans
  `ReservationService` (`getReservation` et `annulerReservation`) via la vérification de propriété
  `isOwner()`, qui lève une `SecurityException` traduite en 403 par le contrôleur.

- **RS-04** — Un ADHERENT ne peut pas créer une réservation au nom d'un autre : implémenté dans
  `ReservationService.createReservation`, qui **écrase** le `userId` du corps de la requête par
  l'identité extraite du token (`getCurrentUserId()` via `SecurityContextHolder`).

- **RS-05** — Un `GET /api/reservations` par un ADHERENT ne retourne que ses propres réservations :
  implémenté dans `ReservationService.getAllReservations` avec
  `reservationRepository.findByUserId(currentUserId)` (le bibliothécaire voit tout).

**Distinction 401 / 403** — Le 401 est renvoyé par l'entry point (utilisateur non identifié) ;
le 403 par la sécurité méthodique Spring (identifié mais non autorisé). Les deux cas sont couverts
par les tests d'intégration.

## Tests

Commandes : `mvn test` (backend) et `ng test --watch=false` (frontend)

- **Backend : Tests run: 49, Failures: 0, Errors: 0 — BUILD SUCCESS**
- **Frontend : TOTAL: 64 SUCCESS** (Karma/Chrome headless, 29 fichiers spec)
- Rapport HTML : `bibliotheque-backend/target/test-report.html` (généré par `python3 generate-test-report.py`)

### Test unitaire RG-03 (repository mocké, aucune base)
`ReservationServiceUnitTest` (Mockito) :
- `shouldAllowThirdReservationWhenUserHasTwoActiveReservations` — 2 actives → 3ᵉ créée (201)
- `shouldRejectFourthReservationWhenUserHasThreeActiveReservations` — 3 actives → refus (409)

### Tests bonus
- `shouldRejectReservationWhenBookIsAvailable_RG01` — livre disponible → refus (409)
- `shouldIgnoreUserIdFromRequestBody_RS04` — `userId: 5` dans le corps, token adherent1 (id 4)
  → réservation créée pour l'id **4**

### Test d'intégration — endpoint sécurisé
`ReservationSecurityIntegrationTest` (MockMvc, SpringBootTest) :
- `shouldReturn401WhenNoTokenIsProvided` — sans token → **401**
- `shouldReturn200WhenAdherentListOwnReservations` — token ADHERENT → **200**
- `shouldReturn403WhenAdherentAccessOtherUserReservation` — réservation d'un autre → **403**
- + couverture DELETE (403 adhérent / 204 bibliothécaire / 401 anonyme)

### Correctif de sécurité annexe — catalogue public
- `WebSecurityConfiguration` : la règle permitAll couvrait `/admin/books/` (avec slash final)
  alors que le frontend appelle `/admin/books` sans slash → **401 injustifié** sur l'URL canonique.
- Corrigé : les deux formes sont désormais publiques, régression couverte par
  `PublicBooksCatalogSecurityTest` (2 tests, avec et sans slash).

### Capture des tests
> 📸 *Insérer ici la capture du résultat `mvn test` (ou du rapport HTML `test-report.html`)*

### Données de démonstration (prérequis du brief)
- `sql/test-data.sql` crée désormais **une réservation EN_ATTENTE par adhérent**
  (adherent1 → « Le Petit Prince », adherent2 → « Les Misérables »), comme exigé
  pour la démo devant le formateur.

## Hors périmètre sécurité (accompagnement UI)

- La page Réservations Angular est accessible aux rôles ADHERENT/BIBLIOTHECAIRE
  (correction du `roleMatch` et des routes).
- Un ADHERENT ne voit pas de sélecteur d'adhérent dans le formulaire : la réservation est
  créée à son nom (traduction UI de RS-04).
- `Users.password` sérialisé en `WRITE_ONLY` : plus aucune fuite de hash via l'API.

## Bonus implémentés

### Expiration du token et message associé
- `JwtRequestFilter` détecte la cause exacte de l'échec (`EXPIRED`, `INVALID`, `MISSING`) et la
  transmet à l'entry point via un attribut de requête.
- `JwtAuthenticationEntryPoint` renvoie un 401 JSON avec un message spécifique :
  - token expiré → *« Votre session a expiré. Veuillez vous reconnecter. »*
  - token invalide → *« Token invalide. Authentification refusée. »*
  - token absent → *« Authentification requise. Veuillez fournir un token valide. »*
- Couvert par `SecurityErrorResponsesTest`.

### Journalisation des tentatives d'accès refusées
- 401 : `JwtAuthenticationEntryPoint` journalise méthode + URI + cause (`MISSING`/`INVALID`/`EXPIRED`).
- 403 : `JwtAccessDeniedHandler` (nouveau, enregistré dans `WebSecurityConfiguration`) journalise
  l'**identifiant de l'utilisateur**, la méthode et l'URI refusés, et renvoie un 403 JSON.
- Règles métier : `ReservationService` journalise les tentatives **RS-03** (consultation/annulation
  de la réservation d'un autre) et **RS-04** (création au nom d'un autre, identité du token imposée).

```
WARN  JwtAccessDeniedHandler : Accès refusé (403) : utilisateur 'adherent1' sur DELETE /api/reservations/9105
WARN  ReservationService    : Accès refusé (RS-03) : 'adherent1' a tenté de consulter la réservation 9106 d'un autre adhérent.
INFO  JwtRequestFilter      : Tentative avec un token expiré sur GET /api/reservations
```

## Vérification manuelle (Swagger + UI)

| Scénario | Résultat |
|---|---|
| `GET /api/reservations` sans token | 401 |
| adherent1 → sa liste | 200 (1 réservation, la sienne) |
| adherent1 → réservation d'adherent2 | 403 |
| adherent1 → `DELETE` | 403 |
| adherent1 → POST avec `userId` d'un autre | créée à son nom (token) |
| bibliothécaire → liste complète + DELETE | 200 / 204 |
