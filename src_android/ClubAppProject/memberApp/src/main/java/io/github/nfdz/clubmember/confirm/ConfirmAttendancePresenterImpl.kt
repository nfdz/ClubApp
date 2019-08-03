package io.github.nfdz.clubmember.confirm

class ConfirmAttendancePresenterImpl(var view: ConfirmAttendanceView?, var interactor: ConfirmAttendanceInteractor?) : ConfirmAttendancePresenter {

    var isConfirming = false

    override fun onCreate(eventId: String) {
        interactor?.initialize(eventId)
        view?.showScanner()
    }

    override fun onDestroy() {
        view = null
        interactor?.destroy()
        interactor = null
    }

    override fun onBarcodeDetected(values: List<String>) {
        if (isConfirming) return
        isConfirming = true
        view?.hideScanner()
        view?.showLoading()
        interactor?.confirmAttendance(values, {
            view?.navigateToFinish()
        }, { invalidCode ->
            isConfirming = false
            view?.showScanner()
            view?.hideLoading()
            if (invalidCode) {
                view?.showInvalidCodeMessage()
            } else {
                view?.showErrorConfirmingMessage()
            }
        })
    }

}