package com.example.pecscreator

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chaquo.python.PyObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var savedPhotoUri : Uri
    lateinit var savedPhotoName : String
    lateinit var module : PyObject
    var pdfFile : File? = null

    var selectedCards = mutableListOf<Card>()

    val numOfCards: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(0)
    }

    






}