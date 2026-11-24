package com.simats.stocksense.feature.analytics.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.simats.stocksense.feature.analytics.model.BillReceiptData
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

object ReceiptPdfGenerator {

    // 80mm width in PDF points: (80mm / 25.4mm) * 72 points ≈ 226.77 points
    private const val PAGE_WIDTH_POINTS = 227

    /**
     * Generates an 80mm receipt-style PDF using Android's built-in PdfDocument.
     * Returns the File and its FileProvider content URI.
     */
    fun generateReceiptPdf(context: Context, bill: BillReceiptData): Pair<File, Uri> {
        val document = PdfDocument()

        // Calculate dynamic height based on item count
        val estimatedItemHeight = 22
        val baseHeaderHeight = 95
        val baseFooterHeight = 90
        val itemsTotalHeight = max(1, bill.items.size) * estimatedItemHeight
        val calculatedHeight = baseHeaderHeight + itemsTotalHeight + baseFooterHeight
        val pageHeight = max(340, calculatedHeight)

        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH_POINTS, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Paint definitions
        val titlePaint = Paint().apply {
            color = Color.rgb(11, 18, 48) // InkDark
            textSize = 11.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(75, 85, 99) // Gray-600
            textSize = 7.5f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.rgb(55, 65, 81)
            textSize = 7.5f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val metaBoldPaint = Paint().apply {
            color = Color.rgb(17, 24, 39)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(17, 24, 39)
            textSize = 7f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(31, 41, 55)
            textSize = 7.5f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val totalLabelPaint = Paint().apply {
            color = Color.rgb(11, 18, 48)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val totalAmountPaint = Paint().apply {
            color = Color.rgb(11, 18, 48)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        val dividerPaint = Paint().apply {
            color = Color.rgb(209, 213, 219)
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(3f, 2f), 0f)
            isAntiAlias = true
        }

        val solidDividerPaint = Paint().apply {
            color = Color.rgb(156, 163, 175)
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val thankYouPaint = Paint().apply {
            color = Color.rgb(107, 114, 128)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        // Layout bounds
        val leftMargin = 12f
        val rightMargin = (PAGE_WIDTH_POINTS - 12).toFloat()
        val centerX = PAGE_WIDTH_POINTS / 2f
        var currentY = 22f

        // 1. Shop Name
        canvas.drawText(bill.shopName.ifBlank { "StockSense Store" }, centerX, currentY, titlePaint)
        currentY += 12f

        // 2. Receipt Tag
        canvas.drawText("TAX INVOICE / RETAIL RECEIPT", centerX, currentY, subtitlePaint)
        currentY += 14f

        // 3. Bill details
        canvas.drawText("Bill No:", leftMargin, currentY, metaPaint)
        canvas.drawText(bill.billNo, leftMargin + 32f, currentY, metaBoldPaint)
        currentY += 11f

        canvas.drawText("Date:", leftMargin, currentY, metaPaint)
        canvas.drawText(bill.dateFormatted, leftMargin + 32f, currentY, metaPaint)
        currentY += 12f

        // Dashed Divider
        canvas.drawLine(leftMargin, currentY, rightMargin, currentY, dividerPaint)
        currentY += 11f

        // 4. Table Header (ITEM, QTY, RATE, TOTAL)
        canvas.drawText("ITEM", leftMargin, currentY, headerPaint)

        val colQtyX = 125f
        val colRateX = 168f
        val colTotalX = rightMargin

        val rightAlignHeader = Paint(headerPaint).apply { textAlign = Paint.Align.RIGHT }
        val centerAlignHeader = Paint(headerPaint).apply { textAlign = Paint.Align.CENTER }

        canvas.drawText("QTY", colQtyX, currentY, centerAlignHeader)
        canvas.drawText("RATE", colRateX, currentY, rightAlignHeader)
        canvas.drawText("TOTAL", colTotalX, currentY, rightAlignHeader)
        currentY += 6f

        canvas.drawLine(leftMargin, currentY, rightMargin, currentY, solidDividerPaint)
        currentY += 12f

        // 5. Items List
        val bodyRight = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }
        val bodyCenter = Paint(bodyPaint).apply { textAlign = Paint.Align.CENTER }

        for (item in bill.items) {
            var itemName = item.name
            if (bodyPaint.measureText(itemName) > 105f) {
                val count = bodyPaint.breakText(itemName, true, 100f, null)
                itemName = itemName.substring(0, count) + "…"
            }

            canvas.drawText(itemName, leftMargin, currentY, bodyPaint)
            canvas.drawText(item.quantity.toString(), colQtyX, currentY, bodyCenter)
            canvas.drawText(ReceiptFormatter.formatAmount(item.unitPrice), colRateX, currentY, bodyRight)
            canvas.drawText(ReceiptFormatter.formatAmount(item.lineTotal), colTotalX, currentY, bodyRight)
            currentY += 16f
        }

        // 6. Summary Divider
        currentY += 2f
        canvas.drawLine(leftMargin, currentY, rightMargin, currentY, dividerPaint)
        currentY += 13f

        // 7. Items / Quantity count
        canvas.drawText("Total Items: ${bill.items.size}   Units: ${bill.totalQuantity}", leftMargin, currentY, metaPaint)
        currentY += 14f

        // 8. Grand Total
        canvas.drawText("GRAND TOTAL", leftMargin, currentY, totalLabelPaint)
        canvas.drawText(ReceiptFormatter.formatAmount(bill.totalAmount), colTotalX, currentY, totalAmountPaint)
        currentY += 8f

        canvas.drawLine(leftMargin, currentY, rightMargin, currentY, solidDividerPaint)
        currentY += 18f

        // 9. Thank You Footer
        canvas.drawText(bill.thankYouMessage, centerX, currentY, thankYouPaint)
        currentY += 10f
        canvas.drawText("Visit Again!", centerX, currentY, subtitlePaint)

        document.finishPage(page)

        // Save PDF file in cacheDir/receipts
        val receiptsDir = File(context.cacheDir, "receipts")
        if (!receiptsDir.exists()) {
            receiptsDir.mkdirs()
        }

        val sanitizedBillNo = bill.billNo.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val pdfFile = File(receiptsDir, "bill_${sanitizedBillNo}.pdf")
        val outputStream = FileOutputStream(pdfFile)
        document.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        document.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        return Pair(pdfFile, uri)
    }
}

