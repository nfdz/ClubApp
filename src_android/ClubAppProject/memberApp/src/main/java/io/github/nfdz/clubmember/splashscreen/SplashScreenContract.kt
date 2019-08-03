package io.github.nfdz.clubmember.splashscreen

interface SplashScreenView {
    fun hideLoading()
    fun showLoading()
    fun showLoginForm()
    fun hideLoginForm()
    fun showLoginCredentialsError()
    fun showLoginConnectionError()
    fun showResetPasswordError()
    fun showCheckEmailMsg()
    fun showDisabledUserMsg()
    fun navigateToMain()
    fun navigateToChat()
    fun navigateToEvent(eventId: String)
    fun navigateToContact()
}

interface SplashScreenPresenter {
    fun onCreate(navigateToChatFlag: Boolean, navigateToEventId: String)
    fun onDestroy()
    fun onLoginClick(email: String, password: String)
    fun onContactClick()
    fun onResetPassword(email: String)
}

interface SplashScreenInteractor {
    fun initialize()
    fun destroy()
    fun isLoggedIn(): Boolean
    fun syncAllAndSubscribeContent(forceSync: Boolean, successCallback: () -> Unit, userErrorStateCallback: (isDisabled: Boolean) -> Unit = {})
    fun logIn(email: String,
              password: String,
              successCallback: () -> Unit,
              errorCallback: (credentialsError: Boolean, disabledError: Boolean) -> Unit)
    fun resetPassword(email: String, successCallback: () -> Unit, errorCallback: () -> Unit)
}