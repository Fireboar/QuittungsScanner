package com.example.quittungsscanner.ui.bands

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import javax.inject.Inject


@HiltViewModel
class BandsViewModel @Inject constructor(
    private val bandsService: BandsApiService
) : ViewModel() {

    private val _bandsFlow: MutableStateFlow<List<BandCode>> = MutableStateFlow(emptyList())
    val bandsFlow: StateFlow<List<BandCode>> = _bandsFlow

    private val _currentBand: MutableSharedFlow<BandInfo?> = MutableSharedFlow()
    val currentBand: Flow<BandInfo?> = _currentBand

    private val _bandCount = MutableStateFlow(0)
    val bandCount: StateFlow<Int> = _bandCount

    init {
        requestBandCodesFromServer()
    }

    fun requestBandCodesFromServer() {
        viewModelScope.launch {
            val bands = getBandCodesFromServer()
            bands?.let {
                _bandsFlow.emit(bands)
                _bandCount.emit(bands.size)
            }

        }
    }

    private suspend fun getBandCodesFromServer(): List<BandCode>? {
        return withContext(Dispatchers.IO) {
            val response = bandsService.getBandNames()
            if (response.code() == HttpURLConnection.HTTP_OK) {
                response.body().orEmpty()
            } else {
                null
            }
        }
    }

    fun requestBandInfoFromServer(code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = bandsService.getBandInfo(code)
            if (response.code() == HttpURLConnection.HTTP_OK) {
                _currentBand.emit(response.body())
            }
        }
    }
}
