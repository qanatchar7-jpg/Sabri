package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CartItem
import com.example.data.model.ProductItem
import com.example.data.model.ScannedItemRecord
import com.example.data.model.ShoppingSessionEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ScanFacture", appName)
  }

  @Test
  fun `test cart item calculation`() {
    val product = ProductItem(
      barcode = "3017620422003",
      name = "Nutella",
      price = 3.50,
      category = "Alimentation"
    )
    val item = CartItem(product = product, quantity = 3)
    assertEquals(10.50, item.lineTotal, 0.001)
  }

  @Test
  fun `test scanned item record total calculation`() {
    val scanned = ScannedItemRecord(
      sessionId = "SES-TEST-001",
      barcode = "3228857000166",
      productName = "Eau Minérale",
      price = 0.95,
      quantity = 4,
      scanSource = "CAMERAX"
    )
    assertEquals(3.80, scanned.lineTotal, 0.001)
  }

  @Test
  fun `test shopping session entity default status`() {
    val session = ShoppingSessionEntity(
      sessionId = "SES-TEST-002",
      sessionName = "Courses Hebdomadaires",
      totalItemsCount = 5,
      totalAmount = 24.50
    )
    assertEquals("COMPLETED", session.status)
    assertEquals(24.50, session.totalAmount, 0.001)
  }

  @Test
  fun `test barcode sound manager toggle`() {
    com.example.ui.sound.BarcodeSoundManager.isSoundEnabled = true
    assertEquals(true, com.example.ui.sound.BarcodeSoundManager.isSoundEnabled)
    com.example.ui.sound.BarcodeSoundManager.playScanBeep()
    com.example.ui.sound.BarcodeSoundManager.isSoundEnabled = false
    assertEquals(false, com.example.ui.sound.BarcodeSoundManager.isSoundEnabled)
    com.example.ui.sound.BarcodeSoundManager.isSoundEnabled = true
  }
}
