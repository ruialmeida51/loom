-keepattributes Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,AnnotationDefault
-keepattributes SourceFile,LineNumberTable
# Typed failure names remain useful even before Crashlytics has processed the matching mapping.
-keepnames class com.eyther.lumbridge.core.domain.errors.AppError
-keepnames class * extends com.eyther.lumbridge.core.domain.errors.AppError
