pluginManagement {
    repositories {
        val relay = System.getenv("SEENEMA_ARTIFACT_RELAY")
        if (relay != null) {
            maven { url = uri("$relay/google/"); isAllowInsecureProtocol = true }
            maven { url = uri("$relay/central/"); isAllowInsecureProtocol = true }
            maven { url = uri("$relay/plugins/"); isAllowInsecureProtocol = true }
        } else { google(); mavenCentral(); gradlePluginPortal() }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        val relay = System.getenv("SEENEMA_ARTIFACT_RELAY")
        if (relay != null) {
            maven { url = uri("$relay/google/"); isAllowInsecureProtocol = true }
            maven { url = uri("$relay/central/"); isAllowInsecureProtocol = true }
        } else { google(); mavenCentral() }
    }
}
rootProject.name = "MovieFlix"
include(":app")
