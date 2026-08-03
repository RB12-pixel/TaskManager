# 📱 Android Task Manager

Un'applicazione Android nativa sviluppata in Java per il monitoraggio in tempo reale delle prestazioni di sistema e della memoria RAM.

## 🚀 Caratteristiche
* **Monitor RAM:** Visualizzazione dinamica con percentuale d'uso e barra di avanzamento (`ProgressBar`).
* **Elenco App Attive:** Tracciamento delle applicazioni in background tramite `UsageStatsManager`.
* **Filtro Avanzato:** Checkbox per filtrare e nascondere le app di sistema mostrando solo quelle installate dall'utente.
* **Icone Reali:** Caricamento dinamico delle icone ufficiali di ogni app.
* **Gestione Processi:** * Reindirizzamento alle impostazioni di sistema per l'Arresto Forzato.
  * Pulizia rapida della memoria RAM in background (`killBackgroundProcesses`).

## 🛠️ Requisiti
* Android API Level 21 (Android 5.0 Lollipop) o superiore.
* Permesso speciale `PACKAGE_USAGE_STATS` (richiesto automaticamente all'avvio).
* Permesso `KILL_BACKGROUND_PROCESSES`.

## 🧰 Licenza
Questo progetto è open source e distribuito sotto licenza **MIT**.
