package com.example.pecscreator.ui.takephoto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.pecscreator.databinding.FragmentCropPhotoBinding


/**
 * A simple [Fragment] subclass.
 * Use the [CropPhoto.newInstance] factory method to
 * create an instance of this fragment.
 */
class CropPhoto : Fragment() {

    private var _binding : FragmentCropPhotoBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedViewModel : CreateCardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCropPhotoBinding.inflate(inflater, container, false)


        binding.imageView.setImageURI(sharedViewModel.savedPhotoUri)

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CropPhoto.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(vm : CreateCardViewModel) =
            CropPhoto().apply {
                sharedViewModel = vm
            }
    }
}