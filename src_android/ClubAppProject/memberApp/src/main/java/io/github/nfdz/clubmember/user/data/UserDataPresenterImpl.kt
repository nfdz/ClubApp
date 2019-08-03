package io.github.nfdz.clubmember.user.data

class UserDataPresenterImpl(var view: UserDataView?, var interactor: UserDataInteractor?) : UserDataPresenter {

    override fun onCreate() {
        interactor?.initialize()
        interactor?.loadData { view?.setContent(it) }
    }

    override fun onDestroy() {
        view = null
        interactor?.destroy()
        interactor = null
    }

    override fun onSaveClick(birthday: String, address: String, phoneNumber: String) {
        view?.showLoading()
        interactor?.saveData(birthday, address, phoneNumber, {
            view?.navigateToFinish()
        }, { invalidBirthday: Boolean ->
            view?.hideLoading()
            when {
                invalidBirthday -> view?.showInvalidBirthdayMsg()
                else -> view?.showSaveErrorMsg()
            }
        })
    }

    override fun onChangePasswordClick() {
        view?.showLoading()
        interactor?.changePassword({
            view?.hideLoading()
            view?.showCheckEmailMsg()
        }, {
            view?.hideLoading()
            view?.showChangePasswordError()
        })
    }

}