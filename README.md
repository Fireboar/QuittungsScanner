# QuittungsScanner

Eine Android-Anwendung zur digitalen Verwaltung und Analyse von Einkaufsbelegen.

## Technische Anforderungen

### Entwicklungsumgebung
- Android Studio
- Kotlin Version 11
- Minimum SDK: 28 (Android 9.0)
- Target SDK: 35
- Compile SDK: 35

### Architektur & Frameworks
- **Jetpack Compose**: Moderne UI-Entwicklung
- **Material Design 3**: UI-Komponenten und Theming
- **MVVM-Architektur**: ViewModel und State Management
- **Dependency Injection**: Dagger Hilt
- **Coroutines**: Asynchrone Programmierung
- **Flow**: Reaktive Programmierung

### Datenbank & Persistenz
- **Room Database**: Lokale Datenspeicherung
- **DataStore**: Benutzereinstellungen
- **Type Converters**: Für komplexe Datentypen

### Netzwerk & APIs
- **Retrofit**: REST-API-Kommunikation
- **OkHttp**: HTTP-Client
- **OpenFoodFacts API**: Produktkategorisierung

### Kamera & Bildverarbeitung
- **CameraX**: Kameraintegration
- **ML Kit**: Texterkennung
- **Text Recognition**: OCR-Funktionalität

### UI-Komponenten
- **Navigation Component**: App-Navigation
- **Bottom Navigation**: Hauptnavigation
- **Custom Top Bar**: Angepasste App-Leiste
- **Charts**: Datenvisualisierung
- **Coil**: Bildladung und -caching

### Berechtigungen
- Kamera
- Internet
- Benachrichtigungen
- Externer Speicher (bis API 32)
- SMS-Lesen

### Features
- Beleg-Scanning mit OCR
- Automatische Produkterkennung
- Kategorisierung von Produkten
- Ausgabenanalyse und -visualisierung
- Benutzerprofilverwaltung
- Offline-Funktionalität
- Gamification-Elemente (Level, EXP, Gold)

### Sicherheit
- Sichere Datenspeicherung
- Verschlüsselte Kommunikation
- Berechtigungsmanagement

### Testing
- JUnit für Unit Tests
- Espresso für UI Tests
- Mockito für Mocking
- Turbine für Flow Testing

## Installation

1. Klonen Sie das Repository
2. Öffnen Sie das Projekt in Android Studio
3. Synchronisieren Sie die Gradle-Dateien
4. Führen Sie die App auf einem Emulator oder physischen Gerät aus

## Lizenz

[Lizenzinformationen hier einfügen] 