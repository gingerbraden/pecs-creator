package com.example.pecscreator.ui.takephoto

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream

class CreateCardViewModel : ViewModel() {

    lateinit var savedPhotoUri : Uri
    lateinit var module : PyObject










}