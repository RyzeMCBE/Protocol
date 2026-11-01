dependencies {
    api(project(":bedrock-codec"))
    if (providers.gradleProperty("centralRelease").map(String::toBoolean).getOrElse(false)) {
        api("io.github.ryzemcbe:cloudburst-raknet:1.0.0.CR3-ryze.1")
    } else {
        api(libs.netty.transport.raknet)
    }
    api(libs.snappy)

    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.platform.launcher)
}
