package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res

/** Synthetic receipt shared by desktop captures and Android's actual photo-picker test. */
suspend fun loadDemoReceiptBytes(): ByteArray = Res.readBytes("files/demo/receipt.jpg")
