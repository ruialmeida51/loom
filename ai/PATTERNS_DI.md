# Dependency injection

Adapted from Plannot `ai/PATTERNS_DI.md`, with Loom's central-module convention.

All @Module, @Single and @KoinViewModel factories live in shared/di. Providers return domain ports
or VM contracts. @Single omits redundant binds; @KoinViewModel binds its contract. Runtime navigation
parameters use @InjectedParam. Koin's Kotlin compiler plugin is used; no KSP or feature-level registrations.
Hosts own shared VM lifetimes and sheet stores. A fresh split sheet gets its own VM identity.
Dedicated demo ports are supplied through the same central binding modules.
