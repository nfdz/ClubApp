package io.github.nfdz.clubmember.user

interface UserView {
    fun showLoadingAlias()
    fun hideLoadingAlias()
    fun setContent(alias: String?, fullName: String, hasBookings: Boolean, signUpDate: Long)
    fun askEditAlias(currentAlias: String?)
    fun askConfirmLogout()
    fun showInvalidAlias(alias: String?)
    fun showConflictAlias(alias: String?)
    fun showErrorSavingAliasMsg()
    fun navigateToBookings()
    fun navigateToPoints(userPoints: Int)
    fun navigateToData()
    fun navigateToLogIn()
}

interface UserPresenter {
    fun onCreate()
    fun onDestroy()
    fun onEditAliasClick()
    fun onSaveAliasClick(alias: String?)
    fun onMyBookingsClick()
    fun onMyDataClick()
    fun onMyPointsClick()
    fun onLogoutClick()
    fun onConfirmLogoutClick()
    fun onDataChangeEvent()
}

interface UserInteractor {
    fun initialize()
    fun destroy()
    fun loadUserData(callback: (alias: String?, fullName: String, hasBookings: Boolean, signUpDate: Long, points: Int) -> Unit)
    fun changeAlias(alias: String?, successCallback: () -> Unit, errorCallback: (invalid: Boolean, conflict: Boolean) -> Unit)
    fun logout(callback: () -> Unit)
}