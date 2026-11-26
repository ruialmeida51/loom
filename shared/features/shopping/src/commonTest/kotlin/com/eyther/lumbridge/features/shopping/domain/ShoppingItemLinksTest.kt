package com.eyther.lumbridge.features.shopping.domain

import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingNamePartDomain.Link
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingNamePartDomain.Text
import kotlin.test.Test
import kotlin.test.assertEquals

class ShoppingItemLinksTest {
    private fun item(name: String): ShoppingItemDomain =
        ShoppingItemDomain(
            id = "i",
            name = name,
            quantity = "",
            category = "Pantry",
            checked = false,
            listId = "l",
        )

    @Test
    fun aPastedProductPageShowsAsItsSite() {
        val lamp = item("Desk lamp https://www.ikea.com/pt/en/p/tertial-work-lamp-50355395/")
        assertEquals(
            listOf(
                Text("Desk lamp "),
                Link(url = "https://www.ikea.com/pt/en/p/tertial-work-lamp-50355395/", label = "ikea.com"),
            ),
            lamp.nameParts(),
        )
        assertEquals("Desk lamp ikea.com", lamp.displayName())
    }

    @Test
    fun bareWwwAddressesOpenOverHttpsAndSentencePunctuationStaysText() {
        assertEquals(
            listOf(
                Text("Recipe ("),
                Link(url = "https://www.bbcgoodfood.com/recipes/pasta", label = "bbcgoodfood.com"),
                Text("), then buy."),
            ),
            item("Recipe (www.bbcgoodfood.com/recipes/pasta), then buy.").nameParts(),
        )
        assertEquals(
            listOf(Link(url = "https://en.wikipedia.org/wiki/Kale_(food)", label = "en.wikipedia.org")),
            item("https://en.wikipedia.org/wiki/Kale_(food)").nameParts(),
        )
    }

    @Test
    fun ordinaryNamesAndNonAddressesStayPlain() {
        assertEquals(listOf(Text("Oat milk")), item("Oat milk").nameParts())
        assertEquals(listOf(Text("www. later")), item("www. later").nameParts())
        assertEquals(listOf(Text("ftp://files.example.test")), item("ftp://files.example.test").nameParts())
        assertEquals("Two links: a.example.test b.example.test",
            item("Two links: https://a.example.test/x https://b.example.test").displayName())
    }
}
