package com.simats.stocksense.feature.reorder

import com.google.gson.Gson
import com.simats.stocksense.data.model.OrderActionResponse
import com.simats.stocksense.data.model.PurchaseOrderDto
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Purchase Order Delete and Cancel rules.
 *
 * Requirements:
 * - Open purchase orders: allow delete only if nothing has been received on them.
 * - Partly received or received orders: never delete. Cancel instead. Stock stays correct.
 * - The backend must enforce this, not only the app.
 * - Show a clear message when delete is refused.
 */
class PurchaseOrderDeleteRuleTest {

    private val gson = Gson()
    private val expectedRefusalMessage = "Cannot delete purchase order with received items. Cancel the order instead to keep stock correct."

    /**
     * Helper to verify delete eligibility rule
     */
    private fun canDeleteOrder(order: PurchaseOrderDto): Pair<Boolean, String?> {
        val status = order.status?.lowercase() ?: ""
        val isReceived = status == "received" || status == "partially_received" || !order.receivedAt.isNullOrBlank()
        return if (isReceived) {
            Pair(false, expectedRefusalMessage)
        } else {
            Pair(true, null)
        }
    }

    @Test
    fun testOpenPurchaseOrder_allowsDelete() {
        val openOrder = PurchaseOrderDto(
            id = 101,
            orderId = 101,
            orderNo = "PO-2026-001",
            status = "placed",
            receivedAt = null,
            totalItems = 2,
            totalUnits = 20
        )

        val (canDelete, refusalReason) = canDeleteOrder(openOrder)
        assertTrue("Open purchase order with 0 received items must be eligible for deletion", canDelete)
        assertNull("No refusal reason when open order is deleted", refusalReason)
    }

    @Test
    fun testReceivedPurchaseOrder_refusesDeleteWithClearMessage() {
        val receivedOrder = PurchaseOrderDto(
            id = 102,
            orderId = 102,
            orderNo = "PO-2026-002",
            status = "received",
            receivedAt = "2026-11-20 10:30:00",
            totalItems = 1,
            totalUnits = 10
        )

        val (canDelete, refusalReason) = canDeleteOrder(receivedOrder)
        assertFalse("Received purchase order must never be deleted", canDelete)
        assertEquals(expectedRefusalMessage, refusalReason)
    }

    @Test
    fun testPartlyReceivedPurchaseOrder_refusesDeleteWithClearMessage() {
        val partlyReceivedOrder = PurchaseOrderDto(
            id = 103,
            orderId = 103,
            orderNo = "PO-2026-003",
            status = "partially_received",
            receivedAt = "2026-11-20 09:15:00",
            totalItems = 3,
            totalUnits = 30
        )

        val (canDelete, refusalReason) = canDeleteOrder(partlyReceivedOrder)
        assertFalse("Partly received purchase order must never be deleted", canDelete)
        assertEquals(expectedRefusalMessage, refusalReason)
    }

    @Test
    fun testCancelledOrderWithPreviousReceipt_refusesDelete() {
        // If an order was cancelled after some goods were received, receivedAt is not null
        val cancelledReceivedOrder = PurchaseOrderDto(
            id = 104,
            orderId = 104,
            orderNo = "PO-2026-004",
            status = "cancelled",
            receivedAt = "2026-11-19 14:00:00",
            totalItems = 2,
            totalUnits = 15
        )

        val (canDelete, refusalReason) = canDeleteOrder(cancelledReceivedOrder)
        assertFalse("Cancelled order that had received items must never be deleted to protect stock audit records", canDelete)
        assertEquals(expectedRefusalMessage, refusalReason)
    }

    @Test
    fun testCancelledOrderWithZeroReceived_allowsDelete() {
        // If an open order was cancelled without any receipt, receivedAt is null
        val cancelledUnreceivedOrder = PurchaseOrderDto(
            id = 105,
            orderId = 105,
            orderNo = "PO-2026-005",
            status = "cancelled",
            receivedAt = null,
            totalItems = 1,
            totalUnits = 5
        )

        val (canDelete, refusalReason) = canDeleteOrder(cancelledUnreceivedOrder)
        assertTrue("Cancelled order where nothing was received may be safely deleted", canDelete)
        assertNull(refusalReason)
    }

    @Test
    fun testBackendErrorResponse_deserializesRefusalMessageCorrectly() {
        val json = """
            {
                "status": false,
                "message": "Cannot delete purchase order with received items. Cancel the order instead to keep stock correct.",
                "data": {
                    "order_id": 42,
                    "order_no": "PO-2026-042",
                    "current_status": "received"
                }
            }
        """.trimIndent()

        val response = gson.fromJson(json, OrderActionResponse::class.java)
        assertEquals(false, response.status)
        assertEquals(expectedRefusalMessage, response.message)
        assertNotNull(response.data)
        val dataMap = response.data as? Map<*, *>
        assertEquals("PO-2026-042", dataMap?.get("order_no"))
    }

    @Test
    fun testBackendSuccessResponse_deserializesDeleteSuccessCorrectly() {
        val json = """
            {
                "status": true,
                "message": "Purchase order 'PO-2026-050' successfully deleted.",
                "data": {
                    "order_id": 50,
                    "order_no": "PO-2026-050"
                }
            }
        """.trimIndent()

        val response = gson.fromJson(json, OrderActionResponse::class.java)
        assertEquals(true, response.status)
        assertEquals("Purchase order 'PO-2026-050' successfully deleted.", response.message)
        assertNotNull(response.data)
        val dataMap = response.data as? Map<*, *>
        assertEquals("PO-2026-050", dataMap?.get("order_no"))
    }
}
