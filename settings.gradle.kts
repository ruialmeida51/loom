pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Loom"

include(":androidApp", ":desktopApp")

include(":shared:core")

include(":shared:entrypoint")

include(":shared:features:authentication")

include(":shared:features:household")

include(":shared:features:shopping")

include(":shared:features:reminders")

include(":shared:features:expenses")

include(":shared:features:feed")

include(":shared:features:insights")

include(":shared:features:settings")

include(":shared:features:home")
