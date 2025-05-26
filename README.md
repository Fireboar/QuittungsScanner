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
- **Dependency Injection**: Dagger Hilt (Receipt Viewmodel und Datenbank)
- **Coroutines**: Asynchrone Programmierung (Datenbank, AddReceipt Screen Fehlermeldungen, Viewmodel saveReceiptToDatabase)
- **Flow**: Reaktive Programmierung (Receipt Viewmodel variables)

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
- Externer Speicher (bis API 32)

### Features
- Beleg-Scanning mit OCR
- Automatische Produkterkennung
- Kategorisierung von Produkten
- Ausgabenanalyse und -visualisierung
- Benutzerprofilverwaltung (Nur Vorbereited)
- Offline-Funktionalität
 