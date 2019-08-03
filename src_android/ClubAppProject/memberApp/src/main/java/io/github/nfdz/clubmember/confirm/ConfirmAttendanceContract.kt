package io.github.nfdz.clubmember.confirm

interface ConfirmAttendanceView {
    fun showScanner()
    fun hideScanner()
    fun showLoading()
    fun hideLoading()
    fun showInvalidCodeMessage()
    fun showErrorConfirmingMessage()
    fun navigateToFinish()
}

interface ConfirmAttendancePresenter {
    fun onCreate(eventId: String)
    fun onDestroy()
    fun onBarcodeDetected(values: List<String>)
}

interface ConfirmAttendanceInteractor {
    fun initialize(eventId: String)
    fun destroy()
    fun confirmAttendance(values: List<String>,
                          successCallback: () -> Unit,
                          errorCallback: (invalidCode: Boolean) -> Unit)
}