rootProject.name = "MinestomMechanics"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://jitpack.io")
        maven { url = uri("https://jitpack.io") }
    }
}
