rootProject.name = "Polyp"

include("codegen")

// dev loop: build against the sibling polyp-world checkout when present; absent, mavenLocal/Central resolves
if (file("../polyp-world").exists()) {
    includeBuild("../polyp-world") {
        dependencySubstitution {
            substitute(module("io.github.term4:polyp-world")).using(project(":"))
        }
    }
}
