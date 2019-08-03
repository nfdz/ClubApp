package io.github.nfdz.clubmember.user

class UserPresenterImpl(var view: UserView?, var interactor: UserInteractor?) : UserPresenter {

    private var currentAlias: String? = null
    private var currentPoints: Int = 0

    override fun onCreate() {
        interactor?.initialize()
        refreshContent()
    }

    private fun refreshContent() {
        interactor?.loadUserData { alias, fullName, hasBookings, signUpDate, points ->
            currentAlias = alias
            currentPoints = points
            view?.setContent(alias, fullName, hasBookings, signUpDate)
        }
    }

    override fun onDestroy() {
        view = null
        interactor?.destroy()
        interactor = null
    }

    override fun onEditAliasClick() {
        view?.askEditAlias(currentAlias)
    }

    override fun onSaveAliasClick(alias: String?) {
        view?.showLoadingAlias()
        interactor?.changeAlias(alias, {
            view?.hideLoadingAlias()
            refreshContent()
        }, { invalid, conflict ->
            view?.hideLoadingAlias()
            when {
                invalid -> view?.showInvalidAlias(alias)
                conflict -> view?.showConflictAlias(alias)
                else -> view?.showErrorSavingAliasMsg()
            }
        })
    }

    override fun onMyBookingsClick() {
        view?.navigateToBookings()
    }

    override fun onMyPointsClick() {
        view?.navigateToPoints(currentPoints)
    }

    override fun onMyDataClick() {
        view?.navigateToData()
    }

    override fun onLogoutClick() {
        view?.askConfirmLogout()
    }

    override fun onConfirmLogoutClick() {
        interactor?.logout { view?.navigateToLogIn() }
    }

    override fun onDataChangeEvent() {
        refreshContent()
    }

}