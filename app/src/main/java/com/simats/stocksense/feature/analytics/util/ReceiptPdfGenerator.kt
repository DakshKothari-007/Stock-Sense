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
     * Splits text into wrapped lines that fit within maxWidth according to paint.
     */
    fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isEmpty()) return listOf("")
        val words = text.split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "${currentLine} $word"
            if (paint.measureText(candidate) <= maxWidth) {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder()
                }
                if (paint.measureText(word) > maxWidth) {
                    var remaining = word
                    while (paint.measureText(remaining) > maxWidth) {
                        val count = paint.breakText(remaining, true, maxWidth, null)
                        if (count <= 0) break
                        lines.add(remaining.substring(0, count))
                        remaining = remaining.substring(count)
                    }
                    if (remaining.isNotEmpty()) {
                        currentLine.append(remaining)
                    }
                } else {
                    currentLine.append(word)
                }
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return if (lines.isEmpty()) listOf(text) else lines
    }

    /**
     * Generates an 80mm receipt-style PDF using Android's built-in PdfDocument.
     * Returns the File and its FileProvider content URI.
     */
    fun generateReceiptPdf(context: Context, bill: BillReceiptData): Pair<File, Uri> {
        val document = PdfDocument()

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
        val itemColWidth = 105f

        val itemLineHeight = 10f
        val itemSpacing = 5f

        // Precompute wrapped lines for accurate height calculation
        val wrappedItems = bill.items.map { item ->
            Pair(item, wrapText(item.name, bodyPaint, itemColWidth))
        }

        var itemsHeight = 0f
        if (wrappedItems.isEmpty()) {
            itemsHeight = 16f
        } else {
            for ((_, lines) in wrappedItems) {
                itemsHeight += itemLineHeight + ((lines.size - 1) * itemLineHeight) + itemSpacing
            }
        }

        val thankYouLines = wrapText(bill.thankYouMessage, thankYouPaint, rightMargin - leftMargin)
        val thankYouHeight = thankYouLines.size * 10f

        var headerHeight = 22f /* top */ + 12f /* RECEIPT */ + 12f /* shop name */
        if (!bill.gstin.isNullOrBlank()) {
            headerHeight += 12f /* GSTIN */
        }
        headerHeight += 3f /* spacer */ + 11f /* bill no */ + 12f /* date */ + 11f /* divider */ + 6f /* table header */ + 12f /* solid divider */

        val footerHeight = 4f + 12f /* divider */ + 13f /* counts */ + 16f /* grand total */ + 14f /* solid divider */ + thankYouHeight + 18f /* bottom margin */

        val calculatedHeight = (headerHeight + itemsHeight + footerHeight).toInt()
        val pageHeight = max(180, calculatedHeight)

        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH_POINTS, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        var currentY = 22f

        // 1. Heading "RECEIPT"
        canvas.drawText("RECEIPT", centerX, currentY, subtitlePaint)
        currentY += 12f

        // 2. Shop Name
        canvas.drawText(bill.shopName.ifBlank { "StockSense Store" }, centerX, currentY, titlePaint)
        currentY += 12f

        // 3. GSTIN (if set)
        if (!bill.gstin.isNullOrBlank()) {
            canvas.drawText("GSTIN: ${bill.gstin}", centerX, currentY, subtitlePaint)
            currentY += 12f
        }
        currentY += 3f

        // 4. Bill details
        canvas.drawText("Bill No:", leftMargin, currentY, metaPaint)
        canvas.drawText(bill.billNo, leftMargin + 32f, currentY, metaBoldPaint)
        currentY += 11f

        canvas.drawText("Date:", leftMargin, currentY, metaPaint)
        canvas.drawText(bill.dateFormatted, leftMargin + 32f, currentY, metaPaint)
        currentY += 12f

        // Dashed Divider
        canvas.drawLine(leftMargin, currentY, rightMargin, currentY, dividerPaint)
        currentY += 11f

        // 5. Table Header (ITEM, QTY, RATE, TOTAL)
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

        // 6. Items List with full multi-line wrapping
        val bodyRight = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }
        val bodyCenter = Paint(bodyPaint).apply { textAlign = Paint.Align.CENTER }

        if (wrappedItems.isEmpty()) {
            canvas.drawText("No items in this bill", leftMargin, currentY, bodyPaint)
            currentY += 16f
        } else {
            for ((item, lines) in wrappedItems) {
                val firstLineY = currentY
                canvas.drawText(lines.firstOrNull() ?: "", leftMargin, firstLineY, bodyPaint)
                canvas.drawText(item.quantity.toString(), colQtyX, firstLineY, bodyCenter)
                canvas.drawText(ReceiptFormatter.formatAmount(item.unitPrice), colRateX, firstLineY, bodyRight)
                canvas.drawText(ReceiptFormatter.formatAmount(item.lineTotal), colTotalX, firstLineY, bodyRight)

                for (i in 1 until lines.size) {
                    currentY += itemLineHeight
                    canvas.drawText(lines[i], leftMargin, currentY, bodyPaint)
                }
                currentY += itemLineHeight + itemSpacing
            }
        }

        // 7. Summary Divider
        currentY += 4f
        canvas.drawLine(leftMargin, currentY, rightMargin, currentY, dividerPaint)
        currentY += 12f

        // 8. Items / Quantity count
        canvas.drawText("Total Items: ${bill.items.size}   Units: ${bill.totalQuantity}", leftMargin, currentY, metaPaint)
        currentY += 13f

        // 9. Grand Total
        canvas.drawText("GRAND TOTAL", leftMargin, currentY, totalLabelPaint)
        canvas.drawText(ReceiptFormatter.formatAmount(bill.totalAmount), colTotalX, currentY, totalAmountPaint)
        currentY += 16f

        canvas.drawLine(leftMargin, currentY, rightMargin, currentY, solidDividerPaint)
        currentY += 14f

        // 10. Thank You Footer (single message, no duplicate "Visit Again!")
        for (line in thankYouLines) {
            canvas.drawText(line, centerX, currentY, thankYouPaint)
            currentY += 10f
        }

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

