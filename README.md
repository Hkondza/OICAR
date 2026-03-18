# Booksy 🏠

> Aplikacija za iznajmljivanje apartmana

Projekt izrađen u sklopu kolegija **Oblikovanje i izrada cjelovitog aplikacijskog rješenja**, 3. godina, Programsko inženjerstvo.

---

## Tim

| Ime | Uloga |
|-----|-------|
| Hrvoje Kondža | Projekt menadžer / Backend |
| Tomislav Dronjić | Backend |
| Leonardo Nardiello | Android developer |
| Ivan Jerković | Web developer |

---

## O projektu

Booksy je full-stack aplikacija koja korisnicima omogućuje pretraživanje i rezervaciju apartmana, a vlasnicima upravljanje smještajem i rezervacijama. Sustav se sastoji od REST API backenda, Android mobilne aplikacije i web admin sučelja.

---

## Funkcionalnosti

### Gost (mobilna aplikacija)
- Registracija i prijava (JWT autentifikacija)
- Pretraga apartmana po lokaciji, datumima i broju gostiju
- Pregled detalja smještaja (slike, opis, ameniteti, cijene)
- Rezervacija s odabirom datuma
- Pregled i otkazivanje rezervacija
- Pisanje recenzija i ocjena
- Push notifikacije (potvrda, podsjetnik, otkazivanje)

### Vlasnik / Admin (web aplikacija)
- Registracija i prijava (JWT autentifikacija)
- Dodavanje, uređivanje i brisanje oglasa
- Upravljanje kalendarom dostupnosti i cijenama
- Pregled i upravljanje rezervacijama
- Statistika prihoda i popunjenosti
- Upravljanje korisnicima (blokiranje, uloge)

---

## Tech stack

### Backend
| Tehnologija | Svrha |
|-------------|-------|
| Java + Spring Boot | REST API |
| PostgreSQL (Railway) | Baza podataka |
| Spring Data JPA + Hibernate | ORM |
| JWT (JSON Web Token) | Autentifikacija |
| Firebase Cloud Messaging | Push notifikacije |
| Swagger / OpenAPI | Dokumentacija API-ja |

### Mobilna aplikacija
| Tehnologija | Svrha |
|-------------|-------|
| Kotlin | Programski jezik |
| Android Studio | IDE |
| Retrofit | HTTP klijent za REST API |

### Web aplikacija
| Tehnologija | Svrha |
|-------------|-------|
| Spring Boot (static files) | Serviranje web sučelja |
| HTML / CSS / JavaScript | Frontend |

### DevOps
| Alat | Svrha |
|------|-------|
| GitHub | Verzioniranje koda |
| Docker | Kontejnerizacija |
| GitHub Actions | CI/CD pipeline |
| Postman | Testiranje API-ja |
| Railway | Hosting PostgreSQL baze |

---

## Arhitektura sustava

```
┌─────────────────┐        ┌──────────────────────┐
│  Android App    │        │  Web Admin           │
│  (Kotlin)       │        │  (HTML/CSS/JS)       │
└────────┬────────┘        └──────────┬───────────┘
         │                            │
         │         REST API           │
         └──────────────┬─────────────┘
                        │
               ┌────────▼────────┐
               │  Spring Boot    │
               │  Backend        │
               └────────┬────────┘
                        │
               ┌────────▼────────┐
               │  PostgreSQL     │
               │  (Railway)      │
               └─────────────────┘
```

---

## Entiteti baze podataka

- **User** — korisnik sustava (gost, vlasnik, admin)
- **Property** — smještajni objekt / apartman
- **Room** — soba unutar objekta
- **Booking** — rezervacija s datumima i statusom
- **Review** — recenzija vezana uz rezervaciju
- **Notification** — obavijest vezana uz promjenu rezervacije

---

## Pokretanje projekta

### Preduvjeti
- Java 17+
- Android Studio (za mobilnu app)
- Docker (opcionalno)
- PostgreSQL connection string s Railway-a

### Backend

```bash
git clone https://github.com/vas-username/booksy-backend.git
cd booksy-backend
```

Dodaj Railway connection string u `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://<railway-host>:<port>/<db>
spring.datasource.username=<username>
spring.datasource.password=<password>
spring.jpa.hibernate.ddl-auto=update
```

Pokreni:

```bash
./mvnw spring-boot:run
```

Swagger dokumentacija dostupna na: `http://localhost:8080/swagger-ui.html`

### Android aplikacija

Otvori projekt u Android Studiu i postavi base URL u Retrofit konfiguraciji:

```kotlin
const val BASE_URL = "http://localhost:8080/api/"
```

---

## Repozitoriji

| Repozitorij | Opis |
|-------------|------|
| `booksy-backend` | Spring Boot backend + web sučelje |
| `booksy-android` | Android mobilna aplikacija |

---

## Status projekta

🚧 U razvoju — kolegij ak. god. 2024./2025.

---

*Kolegij: Oblikovanje i izrada cjelovitog aplikacijskog rješenja | Fakultet organizacije i informatike*
