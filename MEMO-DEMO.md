# 🎤 Mémo démo — Séance 4 (8 minutes devant le formateur)

> Données **vérifiées en base** le 15/09/2026. Re-vérifier juste avant la démo (commande en bas de page).

## Comptes et IDs

| Compte | user_id | Mot de passe | Rôle |
|---|---|---|---|
| `admin` | 1 | `admin123` | Admin |
| `bibliothecaire` | 3 | `biblio123` | BIBLIOTHECAIRE |
| `adherent1` | **4** | `adherent123` | ADHERENT |
| `adherent2` | **5** | `adherent456` | ADHERENT |

| reservation_id | Propriétaire | Livre (book_id) | Statut |
|---|---|---|---|
| **1** | adherent1 (4) | test1 (11) | EN_ATTENTE |
| **2** | adherent2 (5) | dauphin (13) | EN_ATTENTE |
| 3 | adherent1 (4) | book1 (17) | ANNULEE |
| 12 | adherent1 (4) | test book (25) | EN_ATTENTE |

## 0. Préparer les tokens (une fois, en début de démo)

```bash
B=http://localhost:8080
ADH=$(curl -s -X POST $B/authenticate -H "Content-Type: application/json" \
  -d '{"username":"adherent1","password":"adherent123"}' | jq -r .jwtToken)
ADH2=$(curl -s -X POST $B/authenticate -H "Content-Type: application/json" \
  -d '{"username":"adherent2","password":"adherent456"}' | jq -r .jwtToken)
BIB=$(curl -s -X POST $B/authenticate -H "Content-Type: application/json" \
  -d '{"username":"bibliothecaire","password":"biblio123"}' | jq -r .jwtToken)
ADMIN=$(curl -s -X POST $B/authenticate -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r .jwtToken)
```

## 1. RS-01 — sans token → 401 (4 pts)

```bash
curl -si $B/api/reservations | head -1        # HTTP/1.1 401
curl -s $B/api/reservations | jq .message     # "Authentification requise..."
```

## 2. 401 vs 403 — token invalide → toujours 401 (2 pts)

```bash
curl -s -o /dev/null -w "%{http_code}\n" $B/api/reservations \
  -H "Authorization: Bearer token.invalide"   # 401 (reason: INVALID)
```

## 3. RS-05 — chacun ne voit que les siennes (5 pts)

```bash
curl -s $B/api/reservations -H "Authorization: Bearer $ADH"  | jq '[.[].userId]'  # que des 4
curl -s $B/api/reservations -H "Authorization: Bearer $ADH2" | jq '[.[].userId]'  # que des 5
```

## 4. RS-03 — la résa d'un autre → 403 (5 pts)

```bash
# La réservation 2 appartient à adherent2 :
curl -s -o /dev/null -w "%{http_code}\n" $B/api/reservations/2 \
  -H "Authorization: Bearer $ADH"                                     # 403 (lecture)
curl -s -o /dev/null -w "%{http_code}\n" -X PATCH $B/api/reservations/2/annuler \
  -H "Authorization: Bearer $ADH"                                     # 403 (annulation)
```

## 5. RS-02 — action de biblio par un adhérent → 403 (5 pts)

```bash
curl -s -o /dev/null -w "%{http_code}\n" -X DELETE $B/api/reservations/1 \
  -H "Authorization: Bearer $ADH"                                     # 403
```

## 6. RS-04 ⭐ — l'identité vient du token, pas du corps (4 pts)

```bash
# userId: 5 falsifié dans le corps… mais la résa est créée pour adherent1 (4) :
curl -s -X POST $B/api/reservations -H "Authorization: Bearer $ADH" \
  -H "Content-Type: application/json" \
  -d '{"bookId":13,"userId":5}' | jq '{reservationId, userId, status}'
# → userId affiché = 4  ✅  (phrase clé : "l'identité vient du token")
# ℹ️ sûr : livre 13 "dauphin" = 0 exemplaire (pas de 409) et
#    adherent1 n'a que 2 résas actives (pas de refus quota RG-03)
```

## 7. Le bibliothécaire voit tout et supprime

```bash
curl -s $B/api/reservations -H "Authorization: Bearer $BIB" | jq 'length'   # toutes les résas
curl -s -o /dev/null -w "%{http_code}\n" -X DELETE $B/api/reservations/99 \
  -H "Authorization: Bearer $BIB"                                           # 404 (id libre) / 204 si existante
```

## 8. Les tests (en vert)

```bash
cd bibliotheque-backend && ./mvnw test        # Tests run: 49, Failures: 0 — BUILD SUCCESS
python3 generate-test-report.py               # rapport HTML : target/test-report.html
# Frontend : cd ../bibliotheque-frontend && ng test --watch=false   # 64 SUCCESS
```

## Nettoyage après la démo (si étape 6 a créé une résa)

```bash
# Récupérer l'ID créé puis, en BIBLIOTHECAIRE :
curl -s -o /dev/null -w "%{http_code}\n" -X DELETE $B/api/reservations/<ID> \
  -H "Authorization: Bearer $BIB"
```

## Re-vérification des données juste avant la démo

```bash
docker exec -it bibliotheque-db psql -U bibliotheque -d bibliotheque \
  -c "SELECT reservation_id, user_id, book_id, statut FROM reservation ORDER BY 1"
```

## Phrases de vente

- **401** = « je ne sais pas qui vous êtes » / **403** = « je sais qui vous êtes, mais non ».
- **RS-04** : « même si le client falsifie le userId, le backend écrase avec l'identité du token. »
- Les refus sont **journalisés** (bonus) : `WARN JwtAccessDeniedHandler`, `RS-03/RS-04` dans les logs.
