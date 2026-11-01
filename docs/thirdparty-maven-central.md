# Original-source Protocol dependencies for Maven Central

This directory contains **independently compiled source builds**, not official
releases by the upstream organizations. All sources are fetched fresh from
pinned Git commits; no upstream license headers or Java packages are removed.

| RyzeMCBE Maven coordinate | Original Git source SHA | License |
|---|---|---|
| `io.github.ryzemcbe:cloudburst-math-api:2.0.0-ryze.1` | `CloudburstMC/math@87f24259a74a5ed690251fcc940b97f3fb540e47` | MIT |
| `io.github.ryzemcbe:cloudburst-math-immutable:2.0.0-ryze.1` | same as above | MIT |
| `io.github.ryzemcbe:nukkit-natives-java:1.0.3-ryze.1` | `CloudburstMC/Natives@41826fe18358f7358707dc55e698abc16cdcc49a` | Apache 2.0 |
| `io.github.ryzemcbe:cloudburst-raknet:1.0.0.CR3-ryze.1` | `CloudburstMC/Network@78a3224e829f2c4bcfe5a298320477d2f46ce6fa` | Apache 2.0 |

## Critical compatibility details

- The Math API and immutable jars are **separate** so the Immutable POM
  references the correct Ryze Math API artifact; original Java packages stay
  `org.cloudburstmc.math.*`.
- RakNet is compiled from an immutable April 21, 2026 tree retaining
  `RakDisconnectReason`; do not substitute an unrelated newer source.
- Natives is compiled as a **Java-only variant**. The original NukkitX
  library provides Java 11 Zlib and pure Java AES/SHA256 fallbacks. No JNI
  `.so` is included; therefore this is not guaranteed to match the performance
  or behavior of optional native acceleration. Real server runtime smoke tests
  are still required. Artifact name is deliberately `nukkit-natives-java`.
- Math's historical Maven shade SNAPSHOT is replaced with released
  Maven shade 3.3.0 **only in the CI source checkout** to generate templates.
  All archived source commits remain unchanged.
- Source and Javadoc JARs and applicable original licenses are included.
  Verify copyright and attribution requirements before public distribution.
- Javadoc warnings exist in original Math source links. The archive must
  still contain actual generated API documentation.
- Central-only consumption must be tested after these four modules are
  published and **before** Protocol itself is published.

## Verification before merge

Workflow:
`.github/workflows/build-vendor-maven-libraries.yml`
builds the same four modules with `publishToMavenLocal` and
checks POM metadata and source and Javadoc JARs.
It does not upload anything to Sonatype.

## Protected manual release

1. Merge the dependency-publication PR only after CI is green and licenses
   and POM contents have been reviewed.
2. In `RyzeMCBE/Protocol` GitHub Settings > Environments create
   `maven-central`; restrict deployments to `main`, require manual approval.
3. Add environment secrets:
   `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`,
   `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD`.
   They must never appear in Git files or build logs.
4. Start Actions > `Maven Central - Upload Protocol original-source dependencies`
   from `main`; input `ENVIAR-PROTOCOL-DEPS`.
5. Approve the protected environment. GitHub compiles, signs, checks
   that no release coordinates already exist, and **uploads only**.
6. Inspect Sonatype's deployment at
   https://central.sonatype.com/publishing/deployments and click **Publish**
   only if `VALIDATED` and contents are correct. Publishing is irreversible.
7. Confirm all four POM URLs respond with HTTP 200 in
   `https://repo.maven.apache.org/maven2/io/github/ryzemcbe/`.

Only **then** merge Protocol PR #7, which changes
`bedrock-codec` and `bedrock-connection` central-release POMs to
depend on these four artifacts. Development builds retain their original
dependency coordinates. The release workflow here does NOT publish the
two Protocol modules; that is a separate later step.
