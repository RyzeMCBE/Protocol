# Publicación de Engine Protocol en Maven Central

## Alcance

Se reutiliza el namespace verificado `io.github.ryzemcbe`. Los builds
habituales mantienen `org.allaymc.protocol:...:1.26.60-R1-SNAPSHOT`.

Con `-PcentralRelease=true`, la publicación de **candidato estable** será:

- `io.github.ryzemcbe:bedrock-codec:1.26.60-R1`
- `io.github.ryzemcbe:bedrock-connection:1.26.60-R1`

No se modifican los paquetes Java `org.cloudburstmc.*`, ni se publica
un JAR ejecutable de servidor. `bedrock-connection` debe referenciar
el mismo `bedrock-codec` bajo `io.github.ryzemcbe`.

## Auditoría obligatoria antes de liberar

1. Comprobar el workflow de CI y los POM que genera.
2. Verificar **todas** las dependencias transitivas en Maven Central,
   especialmente `netty-transport-raknet` y
   `org.cloudburstmc.math:immutable`, que actualmente se resuelven también
   desde repositorios OpenCollab. No se presupone que esos builds
   estén publicados en Maven Central.
3. Si una dependencia no es resoluble con `mavenCentral()`, escoger una
   versión publicada compatible o preparar una distribución propia
   respetando el código fuente, licencias y cambios necesarios.
   **No** publicar un POM que apunte a un snapshot externo inaccesible.
4. Verificar que no se han publicado previamente las mismas coordenadas.
   Las versiones definitivas de Maven Central son inmutables.

## Validación local

```bash
./gradlew build --no-daemon
./gradlew :bedrock-codec:generatePomFileForMavenPublication \
  :bedrock-connection:generatePomFileForMavenPublication \
  -PcentralRelease=true --no-daemon
python3 .github/scripts/verify-central-poms.py
```

Estos comandos **no publican** a Sonatype.

## Publicación manual, solo después de aprobar todo

El plugin `com.vanniktech.maven.publish` ya tiene configurado
`publishToMavenCentral()` y `signAllPublications()`. Tras validar la
resolución exclusivamente desde Maven Central, configurar el token Sonatype
y la clave GPG en los secretos del entorno que ejecute Gradle, no en el
repositorio. Publicar con:

```bash
./gradlew publishToMavenCentral -PcentralRelease=true
```

El responsable revisa el deployment en
https://central.sonatype.com/publishing y pulsa **Publish** si Sonatype
lo valida. **No ejecutar todavía**: el cierre de dependencias de Maven
Central sigue pendiente de verificación.
