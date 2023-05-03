package com.example.pecscreator

import android.app.Application
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.chaquo.python.PyObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var savedPhotoUri: Uri
    lateinit var savedPhotoName: String
    lateinit var module: PyObject
    var pdfFile: File? = null
    var numberOfCardsOnSinglePage: Int = 0

    var selectedCards = mutableListOf<Card>()

    val numOfCards: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(0)
    }

    lateinit var canvas: Canvas

    private var COORDINATES_EIGHT = listOf(
        Pair(38, 38),
        Pair(913, 38),
        Pair(1778, 38),
        Pair(2653, 38),
        Pair(38, 1287),
        Pair(913, 1287),
        Pair(1778, 1287),
        Pair(2653, 1287)
    )

    private var COORDINATES_FOUR = listOf(
        Pair(38, 38),
        Pair(1280, 38),
        Pair(38, 1792),
        Pair(1280, 1792)
    )

    fun createPDFWithMultipleImage() {
        val file = getOutputFile()
        if (file != null) {
            try {
                val fileOutputStream = FileOutputStream(file)
                val pdfDocument = PdfDocument()
                var pageInfo = initializePageInfo()
                var page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                val paint = Paint()

                paint.color = Color.BLACK
                paint.strokeWidth = 1F
                paint.textSize = 50F

                drawBoundaries(paint)

                for (c in 0..selectedCards.size - 1) {

                    if (c != 0 && c % numberOfCardsOnSinglePage == 0) {
                        pdfDocument.finishPage(page)
                        pageInfo = initializePageInfo()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        drawBoundaries(paint)
                    }

                    val bitmap = selectedCards.get(c).imageUri
                    if (bitmap != null) {

                        when (numberOfCardsOnSinglePage) {
                            8 -> drawImage(bitmap, c, COORDINATES_EIGHT, 800, 928)
                            4 -> drawImage(bitmap, c, COORDINATES_FOUR, 1166, 1352)
                        }
                        drawTextForAll(c)
                    }
                }

                pdfDocument.finishPage(page)
                pdfDocument.writeTo(fileOutputStream)
                pdfDocument.close()

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun initializePageInfo(): PageInfo? {
        return when (numberOfCardsOnSinglePage) {
            8 -> PageInfo.Builder(3508, 2480, 1).create()
            4 -> PageInfo.Builder(2480, 3508, 1).create()
            else -> null
        }
    }

    private fun drawTextForAll(c: Int) {
        val textpaint = Paint()
        val bounds = Rect()
        var text_width = 0
        textpaint.typeface = Typeface.DEFAULT_BOLD
        textpaint.textSize = 60f

        val text = selectedCards.get(c).description.uppercase(Locale.getDefault())

        textpaint.getTextBounds(text, 0, text.length, bounds)
        text_width = bounds.width()

        when (numberOfCardsOnSinglePage) {
            8 -> {
                if (text_width > 800) {
                    drawTextTwoLines(
                        text,
                        textpaint,
                        text_width,
                        bounds,
                        c,
                        1028F,
                        1100F,
                        800,
                        COORDINATES_EIGHT
                    )
                } else {
                    drawText(
                        text,
                        1028F,
                        text_width,
                        textpaint,
                        c % numberOfCardsOnSinglePage,
                        800,
                        COORDINATES_EIGHT
                    )
                }
            }
            4 -> {
                if (text_width > 1166) {
                    drawTextTwoLines(
                        text,
                        textpaint,
                        text_width,
                        bounds,
                        c,
                        1450F,
                        1520F,
                        1166,
                        COORDINATES_FOUR
                    )
                } else {
                    drawText(
                        text,
                        1450F,
                        text_width,
                        textpaint,
                        c % numberOfCardsOnSinglePage,
                        1166,
                        COORDINATES_FOUR
                    )
                }
            }
        }
    }

    private fun drawTextTwoLines(
        text: String,
        textpaint: Paint,
        tx_wt: Int,
        bounds: Rect,
        c: Int,
        posFirst: Float,
        posSecond: Float,
        imageWidth: Int,
        coordinates: List<Pair<Int, Int>>
    ) {
        var text_width = tx_wt
        val listOfWords = text.split(" ")
        val text1 = listOfWords.subList(0, listOfWords.size / 2 + 1).joinToString(" ")
        val text2 =
            listOfWords.subList(listOfWords.size / 2 + 1, listOfWords.size).joinToString(" ")

        textpaint.getTextBounds(text1, 0, text1.length, bounds)
        text_width = bounds.width()
        drawText(
            text1,
            posFirst,
            text_width,
            textpaint,
            c % numberOfCardsOnSinglePage,
            imageWidth,
            coordinates
        )

        textpaint.getTextBounds(text2, 0, text2.length, bounds)
        text_width = bounds.width()
        drawText(
            text2,
            posSecond,
            text_width,
            textpaint,
            c % numberOfCardsOnSinglePage,
            imageWidth,
            coordinates
        )
    }

    private fun drawText(
        string: String,
        pos: Float,
        text_width: Int,
        textpaint: Paint,
        c: Int,
        imageWidth: Int,
        coordinates: List<Pair<Int, Int>>
    ) {
        canvas.drawText(
            string,
            coordinates.get(c).first.toFloat() + imageWidth - text_width - ((imageWidth - text_width) / 2F),
            coordinates.get(c).second + pos, textpaint
        )
    }

    private fun drawImage(
        bitmap: Bitmap,
        c: Int,
        coordinates: List<Pair<Int, Int>>,
        xplus: Int,
        yplus: Int
    ) {
        canvas.drawBitmap(
            bitmap,
            null,
            Rect(
                coordinates.get(c % numberOfCardsOnSinglePage).first,
                coordinates.get(c % numberOfCardsOnSinglePage).second,
                coordinates.get(c % numberOfCardsOnSinglePage).first + xplus,
                coordinates.get(c % numberOfCardsOnSinglePage).second + yplus
            ),
            null
        )
    }

    private fun drawBoundaries(paint: Paint) {
        when (numberOfCardsOnSinglePage) {
            8 -> drawBoundariesForEightCards(paint)
            4 -> drawBoundariesForFourCards(paint)
        }
    }

    private fun drawBoundariesForEightCards(paint: Paint) {
        canvas.drawLine(875.5F, 0F, 875.5F, 2480F, paint)
        canvas.drawLine(1750.5F, 0F, 1750.5F, 2480F, paint)
        canvas.drawLine(2626.5F, 0F, 2626.5F, 2480F, paint)
        canvas.drawLine(0F, 1240F, 3508F, 1240F, paint)
    }

    private fun drawBoundariesForFourCards(paint: Paint) {
        canvas.drawLine(1240.5F, 0F, 1240.5F, 3508F, paint)
        canvas.drawLine(0F, 1754F, 2480F, 1754F, paint)
    }

    private fun getOutputFile(): File? {
        val root = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "PECS Creator"
        )
        var isFolderCreated = true
        if (!root.exists()) {
            isFolderCreated = root.mkdir()
        }
        return if (isFolderCreated) {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val imageFileName = "PECS-Creator_$timeStamp"
            val file = File(root, "$imageFileName.pdf")
            pdfFile = file
            File(root, "$imageFileName.pdf")
        } else {
            null
        }
    }
}