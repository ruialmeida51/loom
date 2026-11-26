package com.eyther.lumbridge.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class WebPicturesTest {
    @Test
    fun onlyHttpsAddressesAreWebPictures() {
        assertTrue(isWebPicture("https://img.example.test/a.jpg"))
        assertFalse(isWebPicture("http://img.example.test/a.jpg"))
        assertFalse(isWebPicture("profiles/you/picture.jpg"))
    }

    @Test
    fun missingOrUnreadablePicturesLoadAsNothingAndOthersAreNeverFetched() = runTest {
        val requested = mutableListOf<String>()
        val pictures =
            WebPictures(
                client =
                    HttpClient(
                        MockEngine { request ->
                            requested += request.url.toString()
                            if (request.url.encodedPath.endsWith("missing.jpg"))
                                respond(content = "", status = HttpStatusCode.NotFound)
                            else respond(content = "not a picture", status = HttpStatusCode.OK)
                        }
                    )
            )
        assertNull(pictures.load("https://img.example.test/missing.jpg"))
        assertNull(pictures.load("https://img.example.test/broken.jpg"))
        assertNull(pictures.load("profiles/you/picture.jpg"))
        assertEquals(
            listOf("https://img.example.test/missing.jpg", "https://img.example.test/broken.jpg"),
            requested,
        )
    }
}
