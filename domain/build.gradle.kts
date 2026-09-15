plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    // Aucune dépendance de production : c'est ce qui garantit, dès la compilation,
    // que le domaine reste indépendant de tout framework (ADR 0001).
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    // Gradle 9 ne fournit plus le launcher implicitement.
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
