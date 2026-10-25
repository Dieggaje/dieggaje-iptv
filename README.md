# IPTV Player TV

Reproductor de listas IPTV para Android TV / TV box (Kotlin + Media3 ExoPlayer).

## Funciones
- Login Xtream Codes (URL + usuario + contraseña) o lista M3U
- Categorías, buscador, logos
- Favoritos: mantén pulsado OK sobre un canal
- Reproductor a pantalla completa: ↑ / ↓ (o CH+/CH−) cambian de canal, OK muestra el nombre
- Prueba .m3u8 y, si falla, .ts automáticamente

## Compilar el APK

### Opción A: GitHub Actions (sin instalar nada)
1. Crea un repositorio en GitHub y sube todo el contenido de esta carpeta.
2. Pestaña **Actions** → **Build APK** → **Run workflow** (también corre solo al hacer push).
3. Cuando termine, descarga el artefacto `iptv-tv-apk` → contiene `app-debug.apk`.

### Opción B: Android Studio
Abrir la carpeta → esperar a que sincronice Gradle → **Build > Build APK(s)**.
El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Instalar en la TV
Copia el APK a un pendrive o usa `adb install app-debug.apk`
(o la app "Send Files to TV"). Hay que permitir "orígenes desconocidos".
