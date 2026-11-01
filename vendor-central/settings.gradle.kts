pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "ryzemcbe-original-protocol-dependencies"
include("cloudburst-math-api", "cloudburst-math-immutable", "nukkit-natives-java", "cloudburst-raknet")
