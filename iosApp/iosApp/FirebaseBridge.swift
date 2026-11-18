import Foundation
import FirebaseCore
import FirebaseCrashlytics
import FirebaseAnalytics
import LoomFoundation

/// No user ID, household details, expense content or custom data is attached by Loom.
final class FirebaseCrashReporter: NSObject, IOSCrashReporter {
    func setCollectionEnabled(enabled: Bool) {
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(enabled)
    }
    func deleteUnsentReports() { Crashlytics.crashlytics().deleteUnsentReports() }
    func recordUncaughtException(name: String, stackTrace: [String]) {
        guard Crashlytics.crashlytics().isCrashlyticsCollectionEnabled() else { return }
        let exception = ExceptionModel(name: name, reason: "Unhandled Kotlin exception")
        exception.stackTrace = stackTrace.map { StackFrame(symbol: $0, file: "", line: 0) }
        Crashlytics.crashlytics().record(exceptionModel: exception)
    }
}

final class FirebaseAnalyticsReporter: NSObject, IOSAnalyticsReporter {
    func setConsent(enabled: Bool) {
        Analytics.setConsent([
            .analyticsStorage: enabled ? .granted : .denied,
            .adStorage: .denied, .adUserData: .denied, .adPersonalization: .denied
        ])
        Analytics.setAnalyticsCollectionEnabled(enabled)
    }
    func logEvent(name: String, parameters: [String: String]) {
        Analytics.logEvent(name, parameters: parameters)
    }
    func clearData() { Analytics.resetAnalyticsData() }
}
