dependencies {
    api(platform(libs.fastutil.bom))
    api(libs.fastutil.`object`.int.maps)
    api(libs.fastutil.int.`object`.maps)
    api(libs.fastutil.long.common)
    api(libs.fastutil.long.`object`.maps)
    api(libs.netty.buffer)
    // For Central-only consumers we distribute independently compiled,
    // attribution-preserving builds of these original upstream libraries.
    if (providers.gradleProperty("centralRelease").map(String::toBoolean).getOrElse(false)) {
        api("io.github.ryzemcbe:cloudburst-math-immutable:2.0.0-ryze.1")
        api("io.github.ryzemcbe:nukkit-natives-java:1.0.3-ryze.1")
    } else {
        api(libs.math)
        api(libs.natives)
    }
    api(libs.jose4j)
    api(libs.nbt)
    implementation(libs.jackson.annotations)

    // Tests
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.platform.launcher)
}
