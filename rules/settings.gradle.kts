pluginManagement {
    repositories {
        val ciOverrides = java.util.Properties().apply {
            val f = java.io.File("../ci/local/ci-overrides.properties")
            if (f.exists()) f.inputStream().use { this.load(it) }
        }
        fun resolveProperty(key: String): String? =
            System.getenv(key)
                ?: gradle.startParameter.projectProperties[key]
                ?: ciOverrides.getProperty(key)

        val nexusUrl = resolveProperty("NEXUS_URL")
        val nexusUsername = resolveProperty("NEXUS_USERNAME")
        val nexusPassword = resolveProperty("NEXUS_PASSWORD")
        val hasNexus =
            !nexusUrl.isNullOrEmpty() && !nexusUsername.isNullOrEmpty() && !nexusPassword.isNullOrEmpty()

        if (hasNexus) {
            maven {
                name = "nexus-rules-plugins"
                setUrl(nexusUrl!!)
                credentials {
                    username = nexusUsername
                    password = nexusPassword
                }
            }
        } else {
            google()
            gradlePluginPortal()
            mavenCentral()
        }
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
