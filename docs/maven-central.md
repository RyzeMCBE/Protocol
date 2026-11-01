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

## Resultado del control real de Maven Central (31-10-2026)

El CI ejecuta `.github/scripts/audit-central-resolution.py` tras generar
los POM. Las pruebas Java y las coordenadas publicables pasan, pero
**no publicar todavía**: Maven Central responde HTTP 404 para estas tres
dependencias directas:

- `org.cloudburstmc.math:immutable:2.0-20220327.162752-1`
- `com.nukkitx:natives:1.0.3`
- `org.cloudburstmc.netty:netty-transport-raknet:1.0.0.CR3-20260421.213623-35`

Hay que reemplazarlas por **versiones realmente disponibles en Central**
solo después de comprobar compatibilidad binaria y ejecutar tests,
o publicar forks propios y verificables desde código fuente y licencias
bajo el namespace autorizado `io.github.ryzemcbe`, actualizando las
coordenadas directas y transitivas. No simplemente eliminar estas
dependencias del POM: los clientes aún necesitarían esas clases.
El CI bloquea la publicación mientras haya una dependencia directa ausente.

## Fuentes originales: preparación de dependencias RyzeMCBE

Antes de usar `publishToMavenCentral` es obligatorio proporcionar tres
dependencias que **no están en Maven Central**. En lugar de cambiar a versiones
no probadas, primero se compila el código original fijado a commits exactos
mediante `.github/workflows/verify-upstream-dependencies.yml`.

| Actual dependencia | Fuente original y revisión fijada | Estado de distribución |
|---|---|---|
| `org.cloudburstmc.math:immutable:2.0-20220327.162752-1` | `CloudburstMC/math@87f24259a74a5ed690251fcc940b97f3fb540e47` | MIT; crear **math API + immutable** con POMs internos corregidos |
| `com.nukkitx:natives:1.0.3` | `CloudburstMC/Natives@41826fe18358f7358707dc55e698abc16cdcc49a` | Apache-2.0; JNI del original es anticuado; variante Java tiene fallbacks para Zlib/AES/SHA256 |
| `org.cloudburstmc.netty:netty-transport-raknet:1.0.0.CR3-20260421.213623-35` | `CloudburstMC/Network@78a3224e829f2c4bcfe5a298320477d2f46ce6fa` | Apache-2.0; esta revisión mantiene `RakDisconnectReason` |

El resultado de ese workflow son **candidatos de compilación, NO publicaciones
listas para Central**. Las adaptaciones históricas de plugin Maven/Gradle
se aplican sobre el checkout temporal de CI, no sobre el repositorio original.

### Orden de publicación, después de completar pruebas

1. Publicar y comprobar el POM Java API de Math bajo el namespace autorizado.
2. Publicar Math Immutable con dependencia al nuevo Math API (además de
   conservar los paquetes originales `org.cloudburstmc.math.*`).
3. Publicar Natives (probar carga y/o fallback puro Java en Java 21; tener
   presente que esto puede ser más lento que el JNI original).
4. Publicar RakNet fijado al árbol compatible de abril de 2026, comprobando
   API y compatibilidad Netty.
5. Cambiar el catálogo de Protocol **solo para publicación** para apuntar a
   esas coordenadas publicadas y ejecutar el consumidor Central-only.
6. Publicar `bedrock-codec:1.26.60-R1` y
   `bedrock-connection:1.26.60-R1` mediante envío manual a Sonatype.

**No** usar el `groupId` original de terceros bajo una cuenta RyzeMCBE.
Los nuevos POM tienen que señalar módulos del namespace autorizado
`io.github.ryzemcbe` y contener correctamente sus dependencias,
autores y licencias. Nunca subir JARs de terceros sin derecho de distribución
ni atribución. Respetar la autoría del proyecto original.

### Firma y envío controlado

Configurar el entorno GitHub `maven-central` en este repositorio, permitir
solo `main` y aprobar manualmente cada despliegue. Replicar de forma segura
`GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`,
`MAVEN_CENTRAL_USERNAME` y `MAVEN_CENTRAL_PASSWORD`.
No copiar sus valores a archivos Git.

Mientras el control de dependencias siga fallando, **no agregar o ejecutar**
ningún workflow que invoque `publishToMavenCentral` para Protocol.
