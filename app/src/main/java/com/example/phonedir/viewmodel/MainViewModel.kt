package com.example.phonedir.viewmodel

import androidx.lifecycle.ViewModel
import com.example.phonedir.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val mainRepository: MainRepository) : ViewModel() {

}