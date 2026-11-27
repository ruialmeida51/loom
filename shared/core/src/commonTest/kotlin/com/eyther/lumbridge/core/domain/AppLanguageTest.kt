package com.eyther.lumbridge.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class AppLanguageTest {
    @Test
    fun storedTagsMatchByLanguageWhateverTheRegion() {
        assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromTag("pt-PT"))
        assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromTag("pt"))
        assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromTag("pt_BR"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en-US"))
    }

    @Test
    fun noTagOrALanguageTheAppLacksFollowsTheDevice() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTag(null))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTag("fr-FR"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTag(""))
    }
}
