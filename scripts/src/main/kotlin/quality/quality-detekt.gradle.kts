import extensions.detektComposeRules
import extensions.sourcesKt
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

val libs: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

apply<DetektPlugin>()

dependencies {
    "detektPlugins"(libs.detektComposeRules)
}

@Suppress("SpreadOperator")
configure<DetektExtension> {
    autoCorrect = false
    source = fileTree(rootDir) {
        include(sourcesKt)
    }.filter { it.extension != "kts" }
        .map { it.parentFile }
        .let {
            files(*it.toTypedArray())
        }
    parallel = true
    config = files("${rootDir}/config/detekt/detekt.yml")
    baseline = file("${rootDir}config/detekt/baseline.xml")
    buildUponDefaultConfig = false
    allRules = false
    disableDefaultRuleSets = false
    debug = false
    ignoreFailures = false
}

tasks.withType<Detekt>().configureEach {
    exclude("**/resources/**,**/build/**")
}
