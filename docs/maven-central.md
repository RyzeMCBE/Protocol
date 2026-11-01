# Publicación de Engine Protocol en Maven Central

## Objetivo

Se preparan los dos módulos de biblioteca, sin cambiar los paquetes Java
`org.cloudburstmc.*` ni el comportamiento del servidor:

- `io.github.ryzemcbe:bedrock-codec:1.26.60-R1`
- `io.github.ryzemcbe:bedrock-connection:1.26.60-R1`

Las compilaciones de desarrollo **siguen utilizando**
`org.allaymc.protocol:...:1.26.60-R1-SNAPSHOT`. La publicación con el
namespace `io.github.ryzemcbe` es **opt-in**, mediante
`-PcentralRelease=true`. La versión publicada se genera sin `-SNAPSHOT`.

## Dependencias de Cloudburst preparadas por RyzeMCBE

Tras el trabajo de los PR #8, #9 y #10, las dependencias originales están
compiladas y firmadas desde revisiones fijadas, conservando paquetes Java y
atribución de los autores originales. El propietario confirmó en Sonatype el envío/publicación de estas
coordenadas. **La disponibilidad pública todavía debe comprobarse**:
el CI del 1 de noviembre de 2026 recibió HTTP 404 desde
`repo.maven.apache.org` para Math Immutable, Natives y RakNet.
El estado de Sonatype no sustituye esta comprobación:

| Módulo | Versión |
|---|---|
| `io.github.ryzemcbe:cloudburst-math-api` | `2.0.0-ryze.1` |
| `io.github.ryzemcbe:cloudburst-math-immutable` | `2.0.0-ryze.1` |
| `io.github.ryzemcbe:nukkit-natives-java` | `1.0.3-ryze.1` |
| `io.github.ryzemcbe:cloudburst-raknet` | `1.0.0.CR3-ryze.1` |

Las bibliotecas publicadas se originan en los repositorios oficiales de
Cloudburst y NukkitX, con licencias MIT o Apache-2.0 según el módulo. El
código original está fijado a los commits indicados en los workflows de
`vendor-central`. No reclamar autoría del código original ni publicar
coordenadas reservadas a las organizaciones originales.

**Importante:** `nukkit-natives-java` distribuye las clases Java, no los
binarios JNI originales. El comportamiento de los fallbacks Java debe
verificarse en entornos reales si se necesita la aceleración nativa.

## Comprobaciones automáticas del PR

```bash
./gradlew build --no-daemon

./gradlew \
  :bedrock-codec:generatePomFileForMavenPublication \
  :bedrock-connection:generatePomFileForMavenPublication \
  -PcentralRelease=true --no-daemon

python3 .github/scripts/verify-central-poms.py
python3 .github/scripts/audit-central-resolution.py \
  --pending io.github.ryzemcbe:bedrock-codec:1.26.60-R1 \
  bedrock-codec/build/publications/maven/pom-default.xml \
  bedrock-connection/build/publications/maven/pom-default.xml

./gradlew :bedrock-codec:build :bedrock-connection:build \
  -PcentralRelease=true --refresh-dependencies --no-daemon
```

En modo `centralRelease`, Gradle usa **solo** `mavenCentral()` como
repositorio de dependencias. `mavenLocal()` se permite únicamente cuando
se solicita expresamente `-PlocalVendorSmoke=true` para pruebas locales.
El CI normal verifica los POMs, dejando temporalmente permitidas
exactamente las tres coordenadas vendor que siguen en propagación.
Esto permite integrar el código en `main` sin aprobar una publicación.

**La verificación de firma manual es la puerta estricta de publicación:**
`.github/workflows/verify-maven-central-signing.yml` debe compilar
ambos módulos con `-PcentralRelease=true` usando **solo Maven Central**.
No contiene excepciones de repositorios ni permite publicación automática.
Hasta superar esa verificación, no publicar Protocol.

`bedrock-connection` debe depender de la versión correspondiente de
`io.github.ryzemcbe:bedrock-codec`. El auditor de integración permite el `bedrock-codec` aún no publicado
y, mientras se propagan, las tres dependencias vendor mencionadas.
La auditoría estricta y la prueba consumidora independiente deben pasar
antes de publicar ambos módulos.

## Firma y publicación (separada de este PR)

No publicar por el mero hecho de fusionar este PR. La publicación
se ejecutará de forma manual, usando el entorno protegido de GitHub
`maven-central` y la firma original de RyzeMCBE Development Studio:

- Huella: `0DE435F45DAAE3CCB759E01B32F428403DE9E4D8`.
- Secretos de entorno: `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`,
  `MAVEN_CENTRAL_USERNAME` y `MAVEN_CENTRAL_PASSWORD`.
- Verificación sin publicar:
  `.github/workflows/verify-maven-central-signing.yml`.

Antes de enviar, comprobar que las coordenadas aún no estén publicadas:
Maven Central no permite sustituir una versión existente. La publicación
requiere los JARs, sus fuentes, Javadoc, POMs y archivos `.asc`, junto
con los metadatos, atribución y licencias correctos.

Una vez validado, usar un workflow de publicación aprobado, con las mismas
credenciales protegidas, que ejecute:

```bash
./gradlew publishToMavenCentral -PcentralRelease=true --no-daemon
```

Revisar el deployment en
https://central.sonatype.com/publishing/deployments y pulsar **Publish**
solo tras validación. Después verificar ambos POM desde
https://repo.maven.apache.org/maven2 y realizar una compilación consumidora
independiente con exclusivamente `mavenCentral()`.

Este documento refleja la preparación de los dos módulos; no declara
que `bedrock-codec` ni `bedrock-connection` estén ya publicados.
