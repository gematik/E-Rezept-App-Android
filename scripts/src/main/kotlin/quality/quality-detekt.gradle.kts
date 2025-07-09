import extensions.detektComposeRules
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

val versionCatalog: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

apply<DetektPlugin>()

dependencies {
    "detektPlugins"(versionCatalog.detektComposeRules)
}

afterEvaluate {
    // Dynamically calculate source roots
    val detektSources = rootProject.subprojects
        .flatMap { project ->
            val srcDir = File(project.projectDir, "src")
            if (srcDir.exists()) {
                project.fileTree(srcDir) {
                    include("**/*.kt")
                }.files
            } else {
                emptySet()
            }
        }
        .filter { it.exists() }
        .map { it.parentFile }
        .toSet()

    configure<DetektExtension> {
        autoCorrect = false
        source.setFrom(files(detektSources))
        parallel = true
        config.setFrom(files("${rootDir}/config/detekt/detekt.yml"))
        baseline = file("${rootDir}/config/detekt/baseline.xml")
        buildUponDefaultConfig = true
        allRules = true
        disableDefaultRuleSets = false
        debug = false
        ignoreFailures = false
    }

    tasks.withType<Detekt>().configureEach {
        exclude("**/resources/**", "**/build/**")
    }
}
