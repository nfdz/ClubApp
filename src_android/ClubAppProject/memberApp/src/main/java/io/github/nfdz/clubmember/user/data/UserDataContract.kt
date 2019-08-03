package io.github.nfdz.clubmember.user.data

interface UserDataView {
    fun setContent(fields: UserDataFields)
    fun showLoading()
    fun hideLoading()
    fun showInvalidBirthdayMsg()
    fun showCheckEmailMsg()
    fun showChangePasswordError()
    fun showSaveErrorMsg()
    fun navigateToFinish()
}

interface UserDataPresenter {
    fun onCreate()
    fun onDestroy()
    fun onSaveClick(birthday: String, address: String, phoneNumber: String)
    fun onChangePasswordClick()
}

interface UserDataInteractor {
    fun initialize()
    fun destroy()
    fun loadData(callback: (UserDataFields) -> Unit)
    fun saveData(birthday: String,
                 address: String,
                 phoneNumber: String,
                 successCallback: () -> Unit,
                 errorCallback: (invalidBirthday: Boolean) -> Unit)
    fun changePassword(successCallback: () -> Unit, errorCallback: () -> Unit)
}

data class UserDataFields(val fullName: String,
                          val email: String,
                          val birthday: String,
                          val address: String,
                          val phoneNumber: String)