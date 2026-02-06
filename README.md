# Clipper

<p align="center" width="300">
   <img align="center" width="200" style="border-radius: 10px;" src="src/main/resources/images/logo.png" />
   <h3 align="center">Clipper</h3>
</p>

Aplicación moderna para gestionar el historial del portapapeles (texto e imágenes) y una colección de emojis para copiar rápidamente. Desarrollada en **Java 21**, utiliza **SQLite** para la persistencia de datos y **FlatLaf** para una interfaz gráfica limpia y moderna.

El historial se almacena localmente, permitiendo recuperar recortes incluso después de reiniciar la aplicación.

<p align="center" width="500">
   <img align="center" width="500" style="border-radius: 10px;" src="docs/dashboard.png" />
   <h3 align="center">Dashboard</h3>
</p>

## Características Principales

1.  **Portapapeles Mixto**: Monitoriza y guarda automáticamente tanto **texto** como **imágenes** que copias (Ctrl+C).
2.  **Historial Visual**: Visualiza tu historial con tarjetas que muestran previsualizaciones de imágenes y fragmentos de texto.
3.  **Pegado Rápido**: Al hacer clic en un elemento del historial, la ventana se minimiza y el contenido se pega automáticamente en tu aplicación activa.
4.  **Emojis**: Panel integrado con categorías de emojis listos para copiar con un clic.
5.  **Gestión**: Posibilidad de eliminar elementos individuales o limpiar todo el historial.
6.  **Persistencia**: Base de datos SQLite integrada para mantener tu historial seguro entre sesiones.

## Requisitos

- **Java 21** (JDK/JRE 21+)
- **Maven** (para construir el proyecto)

## Instalación

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/Jorge-Marco5/Clipper.git
   ```

2. Acceder al directorio:

   ```bash
   cd Clipper
   ```

## Construcción

### Generar JAR y Ejecutable Windows (.exe)

Este proyecto utiliza el plugin `launch4j` para generar un ejecutable de Windows.

```bash
mvn clean package
```

Esto generará:

- `target/Clipper-1.0-SNAPSHOT.jar` (Universal)
- `target/Clipper.exe` (Windows)

### Generar Paquete Linux (.deb)

Para crear un instalador `.deb` para distribuciones basadas en Debian/Ubuntu:

```bash
./script/build_linux.sh
```

Esto generará:

- `target/dist/Clipper-1.0-SNAPSHOT.deb`

## Ejecución y Archivos Generados

**Windows:**

```
./target/Clipper.exe
```

**Linux (Instalado desde .deb):**
Busca "Clipper" en tu menú de aplicaciones o ejecuta `clipper` en la terminal.

**Ejecución directa (JAR):**

```bash
java -jar target/Clipper-1.0-SNAPSHOT.jar
```

## Tecnologías

- Java 21
- Swing UI / FlatLaf (Tema Dark/Light)
- SQLite (Base de datos)
- Maven (Gestión de dependencias)

## Licencia

[MIT](LICENSE)
