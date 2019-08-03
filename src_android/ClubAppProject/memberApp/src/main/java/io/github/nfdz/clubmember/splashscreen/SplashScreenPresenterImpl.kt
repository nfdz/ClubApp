package io.github.nfdz.clubmember.splashscreen

class SplashScreenPresenterImpl(var view: SplashScreenView?, var interactor: SplashScreenInteractor?) : SplashScreenPresenter {

    private var navigateToChatFlag: Boolean = false
    private var navigateToEventId: String = ""

    override fun onCreate(navigateToChatFlag: Boolean, navigateToEventId: String) {
        this.navigateToChatFlag = navigateToChatFlag
        this.navigateToEventId = navigateToEventId
        interactor?.initialize()
        if (interactor?.isLoggedIn() == true) {
            view?.showLoading()
            interactor?.syncAllAndSubscribeContent(!navigateToEventId.isEmpty(), {
                exitSplashScreen()
            }, { isDisabled ->
                if (isDisabled) {
                    view?.hideLoading()
                    view?.showDisabledUserMsg()
                    view?.showLoginForm()
                } else {
                    exitSplashScreen()
                }
            })
        } else {
            view?.showLoginForm()
        }
    }

    private fun exitSplashScreen() {
        if (navigateToChatFlag) {
            view?.navigateToChat()
        } else if (!navigateToEventId.isEmpty()) {
            view?.navigateToEvent(navigateToEventId)
        } else {
            view?.navigateToMain()
        }
    }

    override fun onDestroy() {
        view = null
        interactor?.destroy()
        interactor = null
    }

    override fun onLoginClick(email: String, password: String) {
        view?.hideLoginForm()
        view?.showLoading()
        interactor?.logIn(email, password, {
            view?.navigateToMain()
        }, { credentialsError, isDisabled ->
            view?.hideLoading()
            view?.showLoginForm()
            when {
                credentialsError -> view?.showLoginCredentialsError()
                isDisabled -> view?.showDisabledUserMsg()
                else -> view?.showLoginConnectionError()
            }
        })
    }

    override fun onContactClick() {
        view?.navigateToContact()
    }

    override fun onResetPassword(email: String) {
        view?.hideLoginForm()
        view?.showLoading()
        interactor?.resetPassword(email, {
            view?.showLoginForm()
            view?.hideLoading()
            view?.showCheckEmailMsg()
        }, {
            view?.showLoginForm()
            view?.hideLoading()
            view?.showResetPasswordError()
        })
    }

}