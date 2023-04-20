package com.example.pecscreator

import android.app.Application
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chaquo.python.PyObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var savedPhotoUri : Uri
    lateinit var savedPhotoName : String
    lateinit var module : PyObject
    var pdfFile : File? = null

    var selectedCards = mutableListOf<Card>()

    val numOfCards: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(0)
    }

    private var COORDINATES = listOf<Pair<Int, Int>>(
        Pair(38, 38),
        Pair(913, 38),
        Pair(1778, 38),
        Pair(2653, 38),
        Pair(38, 1287),
        Pair(913, 1287),
        Pair(1778, 1287),
        Pair(2653, 1287)
    )

    fun createPDFWithMultipleImage() {
        val file = getOutputFile()
        if (file != null) {
            try {
                val fileOutputStream = FileOutputStream(file)
                val pdfDocument = PdfDocument()
                var pageInfo = PdfDocument.PageInfo.Builder(3508, 2480, 1).create()
                var page = pdfDocument.startPage(pageInfo)
                var canvas = page.canvas
                var paint = Paint()
                paint.color = Color.BLACK
                paint.strokeWidth = 1F
                paint.textSize = 50F
                canvas.drawLine(875.5F, 0F, 875.5F, 2480F, paint)
                canvas.drawLine(1750.5F, 0F, 1750.5F, 2480F, paint)
                canvas.drawLine(2626.5F, 0F, 2626.5F, 2480F, paint)
                canvas.drawLine(0F, 1240F, 3508F, 1240F, paint)

                for (c in 0..selectedCards.size - 1) {

                    if (c != 0 && c % 8 == 0) {
                        pdfDocument.finishPage(page)
                        pageInfo = PdfDocument.PageInfo.Builder(3508, 2480, 1).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        paint = Paint()
                        paint.color = Color.BLACK
                        paint.strokeWidth = 1F
                        paint.textSize = 50F
                        canvas.drawLine(875.5F, 0F, 875.5F, 2480F, paint)
                        canvas.drawLine(1750.5F, 0F, 1750.5F, 2480F, paint)
                        canvas.drawLine(2626.5F, 0F, 2626.5F, 2480F, paint)
                        canvas.drawLine(0F, 1240F, 3508F, 1240F, paint)
                    }

                    val bitmap = selectedCards.get(c).imageUri
                    if (bitmap != null) {
                        canvas.drawBitmap(
                            bitmap,
                            null,
                            Rect(
                                COORDINATES.get(c%8).first,
                                COORDINATES.get(c%8).second,
                                COORDINATES.get(c%8).first + 800,
                                COORDINATES.get(c%8).second + 928
                            ),
                            null
                        )

                        val textpaint = Paint()
                        val bounds = Rect()

                        var text_width = 0

                        textpaint.typeface = Typeface.DEFAULT_BOLD // your preference here
                        textpaint.textSize = 60f // have this the same as your text size

                        val text = selectedCards.get(c).description.uppercase(Locale.getDefault())

                        textpaint.getTextBounds(text, 0, text.length, bounds);
                        text_width = bounds.width()

                        if (text_width > 800 && text_width < 1600) {
                            val listOfWords = text.split(" ")
                            val text1 = listOfWords.subList(0, listOfWords.size/2 + 1).joinToString(" ")
                            val text2 = listOfWords.subList(listOfWords.size/2+1, listOfWords.size).joinToString(" ")

                            textpaint.getTextBounds(text1, 0, text1.length, bounds);
                            text_width = bounds.width()
                            drawText(canvas, text1, 1028F, text_width, textpaint, c%8)

                            textpaint.getTextBounds(text2, 0, text2.length, bounds);
                            text_width = bounds.width()
                            drawText(canvas, text2, 1100F, text_width, textpaint, c%8)

                        } else {
                            drawText(canvas, text, 1028F, text_width, textpaint, c%8)
                        }

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

    private fun drawText(canvas : Canvas, string : String, pos : Float, text_width : Int, textpaint : Paint, c : Int) {
        canvas.drawText(
            string,
            COORDINATES.get(c).first.toFloat() + 800 - text_width - ((800 - text_width) / 2F),
            COORDINATES.get(c).second + pos, textpaint
        )
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
            var file = File(root, "$imageFileName.pdf")
            pdfFile = file
            File(root, "$imageFileName.pdf")
        } else {
            null
        }
    }






}