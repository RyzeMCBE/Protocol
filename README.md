<div align="center">

# Engine Protocol · RyzeMCBE

### Biblioteca de protocolos multiversión para Minecraft Bedrock Edition

**Codecs, paquetes, serialización y conexión para Engine y plugins del ecosistema Allay.**

[![Java](https://img.shields.io/badge/Java-JVM-f97316)](https://github.com/RyzeMCBE/Protocol)
[![Apache 2.0](https://img.shields.io/badge/Licencia-Apache%202.0-blue)](LICENSE)
[![Estado](https://img.shields.io/badge/Estado-En%20desarrollo-orange)](https://github.com/RyzeMCBE/Protocol)

[Repositorio](https://github.com/RyzeMCBE/Protocol) · [Engine (repositorio actual)](https://github.com/RyzeMCBE/Allay) · [Organización](https://github.com/RyzeMCBE)

</div>

---

## 📖 Descripción

**Engine Protocol** es una biblioteca para el protocolo de **Minecraft: Bedrock Edition**, con codecs y serializadores para diferentes versiones. Esta copia se mantiene en **RyzeMCBE Development Studio** y está basada en la línea de Protocol utilizada por Allay, derivada del proyecto [CloudburstMC/Protocol](https://github.com/CloudburstMC/Protocol).

El propósito de este repositorio es administrar las implementaciones del protocolo por separado del servidor, facilitando actualizaciones, pruebas de regresión y su integración con nuestro fork de Allay.

## 📦 Módulos

| Módulo | Función |
| --- | --- |
| `bedrock-codec` | Definiciones de paquetes, serializers y codecs específicos por versión, como `Bedrock_v944.CODEC`. |
| `bedrock-connection` | Transporte de red con Netty/RakNet, sesiones, compresión y cifrado. |


> **Compatibilidad binaria:** el nombre público de este componente es Engine Protocol, pero se conservan los paquetes `org.cloudburstmc.protocol.*`, los módulos `bedrock-codec` y `bedrock-connection`, y el grupo Maven `org.allaymc.protocol`. No se necesita recompilar plugins por este cambio de identidad. La compatibilidad real también depende de la versión del servidor y del protocolo.

Las coordenadas de dependencias de esta línea usan el grupo `org.allaymc.protocol`, aunque los paquetes Java mantienen el espacio de nombres `org.cloudburstmc.protocol`.

## 🔗 Integración con Engine

**[RyzeMCBE/Allay](https://github.com/RyzeMCBE/Allay)** incorpora este repositorio como submódulo Git en **`protocol-local/`**, fijado a una revisión concreta.

```text
RyzeMCBE/Allay
├── api/
├── server/
├── settings.gradle.kts
└── protocol-local/      ← RyzeMCBE/Protocol
    ├── bedrock-codec/
    └── bedrock-connection/
```

Clona Allay con sus submódulos:

```bash
git clone --recurse-submodules https://github.com/RyzeMCBE/Allay.git
cd Allay
git submodule sync --recursive
git submodule update --init --recursive
```

**Importante:** un submódulo queda asociado al commit registrado por Allay. Actualizar `main` de Protocol no modifica automáticamente ese commit en Allay; hay que actualizar y guardar el puntero del submódulo.

## 🛠️ Compilar Protocol

Requiere el JDK y las dependencias declaradas por los scripts Gradle del repositorio.

```bash
git clone https://github.com/RyzeMCBE/Protocol.git
cd Protocol
chmod +x gradlew
./gradlew build --no-daemon
```

Para Windows, utiliza `gradlew.bat`.

### Integración con Gradle

En el archivo `settings.gradle.kts` de Allay se usa `includeBuild("protocol-local")` y sustitución de dependencias para resolver estos módulos desde el código local:

```text
org.allaymc.protocol:bedrock-codec
org.allaymc.protocol:bedrock-connection
```

No es necesario publicar esos módulos en Maven Central para compilarlos como parte del proyecto compuesto de Allay.

## 🧪 Compatibilidad y pruebas

El repositorio incluye implementaciones para múltiples protocolos históricos y actuales de Bedrock. **Que exista un codec no garantiza por sí mismo que una versión concreta pueda entrar y jugar sin errores**: también deben verificarse los paquetes, el estado de sesión, los inventarios, los bloques y las entidades.

La revisión importada incluye una prueba de regresión sobre el protocolo **v2168**:

```text
bedrock-codec/src/test/java/org/cloudburstmc/protocol/bedrock/codec/v2168/serializer/ItemStackResponseSerializer_v2168Test.java
```

Esta prueba es requerida explícitamente por `settings.gradle.kts` de nuestro fork de Allay. La presencia del archivo no reemplaza la ejecución de las pruebas y una compilación exitosa.

## 🤝 Contribuir

Los reportes deben identificar la versión de Minecraft, la versión del protocolo, el paquete/serializador involucrado, los pasos de reproducción y, cuando sea posible, una prueba automatizada. Incluye logs sin credenciales ni datos privados.

## ⚖️ Licencia y atribuciones

El código está sujeto a los términos descritos en [LICENSE](LICENSE) (**Apache 2.0**). Reconocemos a los autores originales de [CloudburstMC/Protocol](https://github.com/CloudburstMC/Protocol) y a quienes han mantenido sus adaptaciones para Allay.

---

<div align="center">

**RyzeMCBE Development Studio** · Ingeniería, compatibilidad y herramientas para Minecraft Bedrock.

[Allay](https://github.com/RyzeMCBE/Allay) · [Protocol](https://github.com/RyzeMCBE/Protocol)

<sub>Proyecto independiente y no afiliado a Mojang Studios ni Microsoft.</sub>

</div>
