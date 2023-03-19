package com.example.pecscreator

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chaquo.python.PyObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var savedPhotoUri : Uri
    lateinit var savedPhotoName : String
    lateinit var module : PyObject



}