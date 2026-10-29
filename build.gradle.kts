import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    id("java-library")
    alias(libs.plugins.lombok) apply false
    alias(libs.plugins.publish) apply false
}

tasks.jar {
    enabled = false
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = rootProject.libs.plugins.lombok.get().pluginId)
    apply(plugin = rootProject.libs.plugins.publish.get().pluginId)

    group = "org.allaymc.protocol"
    version = rootProject.version

    tasks {
        withType<JavaCompile>().configureEach {
            options.encoding = Charsets.UTF_8.name()
            options.compilerArgs.add("-parameters")
        }

        withType<Javadoc>().configureEach {
            (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:-missing", true)
        }

        test {
            useJUnitPlatform()
        }
    }

    repositories {
        mavenCentral()
        maven("https://repo.opencollab.dev/maven-releases/")
        maven("https://repo.opencollab.dev/maven-snapshots/")
    }

    dependencies {
        compileOnly(rootProject.libs.checker.qual)
    }

    java {
        withSourcesJar()
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    configure<MavenPublishBaseExtension> {
        publishToMavenCentral()
        signAllPublications()

        coordinates(
            project.group.toString(),
            project.name,
            project.version.toString()
        )

        pom {
            name.set("Engine Protocol - ${project.name}")
            description.set("Engine Protocol: multiversion Bedrock network libraries, retaining Cloudburst and Allay compatibility.")
            url.set("https://github.com/RyzeMCBE/Protocol")

            scm {
                connection.set("scm:git:git://github.com/AllayMC/Protocol.git")
                developerConnection.set("scm:git:ssh://github.com/AllayMC/Protocol.git")
                url.set("https://github.com/RyzeMCBE/Protocol")
            }

            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    name.set("RyzeMCBE Development Studio")
                    organization.set("RyzeMCBE")
                    organizationUrl.set("https://github.com/RyzeMCBE")
                }
                developer {
                    name.set("CloudburstMC Team")
                    organization.set("CloudburstMC")
                    organizationUrl.set("https://github.com/CloudburstMC")
                }
                developer {
                    name.set("AllayMC Team")
                    organization.set("AllayMC")
                    organizationUrl.set("https://github.com/AllayMC")
                }
            }
        }
    }
}
