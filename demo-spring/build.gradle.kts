plugins {
    application
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

dependencies {
    // Le BOM aligne toutes les versions Spring : on ne versionne aucune dépendance à la main.
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.4.1"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}

// Conserve le nom des paramètres dans le bytecode : sans ce flag, Spring ne peut pas
// deviner à quoi correspond @PathVariable/@RequestParam. Le plugin Spring Boot l'ajoute
// tout seul ; avec le plugin `application`, il faut le mettre à la main.
tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

application {
    mainClass = "io.github.jurch29.epargne.demo.Application"
}
