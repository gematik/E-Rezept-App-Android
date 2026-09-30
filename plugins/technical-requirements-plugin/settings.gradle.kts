rootProject.name = "technical-requirements-plugin"

pluginManagement {
    repositories {
        val ciOverrides = java.util.Properties().apply {
            val f = java.io.File("../../ci/local/ci-overrides.properties")
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
                name = "nexus-technical-plugins"
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
    repositories {
        val ciOverrides = java.util.Properties().apply {
            val f = java.io.File("../../ci/local/ci-overrides.properties")
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
                name = "nexus-technical-deps"
                setUrl(nexusUrl!!)
                credentials {
                    username = nexusUsername
                    password = nexusPassword
                }
            }
        } else {
            google()
            mavenCentral()
        }
    }
}
