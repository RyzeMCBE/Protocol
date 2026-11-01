import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.37.0" apply false
}

// The CI checks out original licensed sources into ../vendor-upstream/.
// These projects compile and publish from those sources; they do not
// claim authorship of upstream classes.
val upstream = rootProject.file("../vendor-upstream")

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.vanniktech.maven.publish")

    group = "io.github.ryzemcbe"
    version = when (name) {
        "cloudburst-math-api", "cloudburst-math-immutable" -> "2.0.0-ryze.1"
        "nukkit-natives-java" -> "1.0.3-ryze.1"
        "cloudburst-raknet" -> "1.0.0.CR3-ryze.1"
        else -> error("Unexpected vendor project ${project.name}")
    }

    val originalProject = when (name) {
        "cloudburst-math-api" -> "CloudburstMC/math"
        "cloudburst-math-immutable" -> "CloudburstMC/math"
        "nukkit-natives-java" -> "CloudburstMC/Natives"
        "cloudburst-raknet" -> "CloudburstMC/Network"
        else -> error("Unknown upstream source")
    }

    val licenseName = if (name.startsWith("cloudburst-math")) "MIT" else "Apache-2.0"
    val rootSources = when (name) {
        "cloudburst-math-api" -> upstream.resolve("math/api")
        "cloudburst-math-immutable" -> upstream.resolve("math/immutable")
        "nukkit-natives-java" -> upstream.resolve("natives")
        "cloudburst-raknet" -> upstream.resolve("raknet/transport-raknet")
        else -> error("Unexpected project")
    }

    sourceSets.named("main") {
        java.setSrcDirs(buildList {
            add(rootSources.resolve("src/main/java"))
            if (project.name.startsWith("cloudburst-math")) {
                add(rootSources.resolve("target/generated-sources"))
            }
        })
    }

    repositories {
        mavenCentral()
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (project.name == "nukkit-natives-java") {
            // The original 1.0.3 Java API includes Java 11 ByteBuffer
            // compression and JDK-internal DirectBuffer/Unsafe types.
            // Do not compile it with --release 8.
            options.compilerArgs.addAll(listOf(
                "--add-exports", "java.base/sun.nio.ch=ALL-UNNAMED"
            ))
        } else {
            options.release.set(8)
        }
    }

    tasks.withType<Javadoc>().configureEach {
        isFailOnError = false
        (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
    }

    val licenseFile = if (name.startsWith("cloudburst-math")) {
        upstream.resolve("math/LICENSE.txt")
    } else if (name == "nukkit-natives-java") {
        upstream.resolve("natives/LICENSE")
    } else {
        upstream.resolve("raknet/LICENSE")
    }

    tasks.withType<Jar>().configureEach {
        from(licenseFile) {
            into("META-INF")
            rename { "LICENSE" }
        }
    }

    dependencies {
        when (project.name) {
            "cloudburst-math-api" -> {
                "compileOnly"("com.google.code.findbugs:jsr305:3.0.2")
            }
            "cloudburst-math-immutable" -> {
                "api"(project(":cloudburst-math-api"))
                "compileOnly"("com.google.code.findbugs:jsr305:3.0.2")
            }
            "nukkit-natives-java" -> {
                // Java 11+ Zlib and pure-Java AES/SHA-256 fallbacks provided
                // by original NukkitX source; JNI binary is NOT packaged.
            }
            "cloudburst-raknet" -> {
                "api"("io.netty:netty-common:4.1.101.Final")
                "api"("io.netty:netty-buffer:4.1.101.Final")
                "api"("io.netty:netty-codec:4.1.101.Final")
                "api"("io.netty:netty-transport:4.1.101.Final")
                "api"("io.netty:netty-transport-native-unix-common:4.1.101.Final")
                "api"("net.jodah:expiringmap:0.5.10")
            }
        }
    }

    configure<MavenPublishBaseExtension> {
        publishToMavenCentral()
        signAllPublications()
        coordinates(project.group.toString(), project.name, project.version.toString())

        pom {
            name.set("RyzeMCBE build of ${originalProject.substringAfterLast('/')} - ${project.name}")
            description.set("Independently built from original ${originalProject} sources for RyzeMCBE Protocol interoperability. Not an official upstream release.")
            url.set("https://github.com/RyzeMCBE/Protocol")
            scm {
                connection.set("scm:git:https://github.com/RyzeMCBE/Protocol.git")
                developerConnection.set("scm:git:ssh://github.com/RyzeMCBE/Protocol.git")
                url.set("https://github.com/RyzeMCBE/Protocol")
            }
            licenses {
                license {
                    name.set(if (licenseName == "MIT") "MIT License" else "The Apache License, Version 2.0")
                    url.set(if (licenseName == "MIT") "https://opensource.org/licenses/MIT" else "https://www.apache.org/licenses/LICENSE-2.0")
                }
            }
            developers {
                developer {
                    name.set("RyzeMCBE Development Studio (source build/maintenance)")
                    organization.set("RyzeMCBE")
                    organizationUrl.set("https://github.com/RyzeMCBE")
                }
                developer {
                    name.set("Original authors: ${originalProject}")
                    organization.set(originalProject.substringBefore('/'))
                    organizationUrl.set("https://github.com/${originalProject.substringBefore('/')}")
                }
            }
        }
    }
}
