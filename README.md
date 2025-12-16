# Clipper

<p align="center" width="300">
   <img align="center" width="200" style="border-radius: 10px;" src="src/main/resources/images/logo.png" />
   <h3 align="center">Clipper</h3>
</p>



Aplicación para gestionar el portapapeles y lista de emojis para copiar. Programada en lenguaje Java Version 21, y usa sqlite como base de datos.

El historial se almacena en la base de datos sqlite por lo que los registros se mantienen incluso si se cierra la aplicacion, puedes eliminar las copias manualmente si lo deseas.

<p align="center" width="500">
   <img align="center" width="500" style="border-radius: 10px;" src="assets/dashboard.png" />
   <h3 align="center">Dashboard</h3>
</p>

## Caracteristicas

- Guardar texto en el portapapeles.
- Ver historial del portapapeles.
- Eliminar texto del portapapeles.
- Eliminar todo el portapapeles.
- Listado de emojis.

## Requisitos

- Java 21.0.5 JBR
- Maven

## Instalación

```bash
### Clonar el repositorio

git clone https://github.com/Jorge-Marco5/Clipper.git

### Cambiar al directorio del proyecto

cd Clipper

### Compilar

Para generar el ejecutable (JAR con dependencias incluidas):

```bash
mvn clean package
```

### Ejecución

Una vez compilado, puedes iniciar la aplicación desde la carpeta `target`:

```bash
java -jar target/Clipper-1.0-SNAPSHOT.jar
```

## Uso

1. **Portapapeles**: La aplicación monitoriza automáticamente lo que copias (Ctrl+C).
2. **Historial**: Abre la aplicación para ver y reutilizar textos copiados anteriormente.
3. **Pegado Rápido**: Haz clic en cualquier elemento del historial para minimizar la app y pegarlo automáticamente donde estabas escribiendo.
4. **Emojis**: Navega por las pestañas de emojis, haz clic en uno para pegarlo automáticamente.
5. **Gestión**: Puedes fijar o eliminar elementos del historial.

## Licencia

[MIT](LICENSE)
