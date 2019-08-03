package io.github.nfdz.clubmember.chat

class ChatPresenterImpl(var view: ChatView?, var interactor: ChatInteractorImpl?) : ChatPresenter {
    
    override fun onCreate() {
        interactor?.initialize()
        updateContent()
        if (true == interactor?.getAndSetFirstTimeInChat()) {
            view?.showWelcomeToChat()
        }
    }

    private fun updateContent() {
        interactor?.loadChatMessages { items, newMessages ->
            view?.setChatContent(items, newMessages)
        }
    }

    override fun onDestroy() {
        view = null
        interactor?.destroy()
        interactor = null
    }

    override fun onResume() {
        CHAT_FOREGROUND_STATE = true
        interactor?.syncChat()
        interactor?.cancelNotifications()
    }

    override fun onPause() {
        CHAT_FOREGROUND_STATE = false
    }

    override fun onChatMessageLongClick(message: ChatMessage) {
        interactor?.copyMessageToTheClipboard(message)
        view?.showMessageCopiedToTheClipboard()
    }

    override fun onSendMessageClick(text: String) {
        view?.showLoading()
        interactor?.sendMessage(text, {
            view?.hideLoading()
            view?.clearSendBox()
        }, {
            view?.hideLoading()
            view?.showSendErrorMessage()
        })
    }

    override fun onChatEvent() {
        updateContent()
    }

}

var CHAT_FOREGROUND_STATE = false