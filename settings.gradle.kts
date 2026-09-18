rootProject.name = "livrets-epargne"

dependencyResolutionManagement {
    // Dépôts déclarés ici et nulle part ailleurs : un module ne peut pas introduire
    // une source de dépendances qui n'a pas été revue.
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

include("domain")

// Démo jetable pour voir le flux Spring Boot de bout en bout. Hors phasage du projet.
include("demo-spring")
