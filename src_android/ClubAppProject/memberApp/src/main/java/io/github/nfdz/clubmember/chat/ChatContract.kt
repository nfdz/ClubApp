package io.github.nfdz.clubmember.chat

interface ChatView {
    fun setChatContent(items: List<ChatMessage>, alertNewMessage: Boolean)
    fun showLoading()
    fun hideLoading()
    fun clearSendBox()
    fun showSendErrorMessage()
    fun showMessageCopiedToTheClipboard()
    fun showWelcomeToChat()
}

interface ChatPresenter {
    fun onCreate()
    fun onDestroy()
    fun onResume()
    fun onPause()
    fun onChatMessageLongClick(message: ChatMessage)
    fun onSendMessageClick(text: String)
    fun onChatEvent()
}

interface ChatInteractor {
    fun initialize()
    fun destroy()
    fun syncChat()
    fun cancelNotifications()
    fun getAndSetFirstTimeInChat(): Boolean
    fun copyMessageToTheClipboard(message: ChatMessage)
    fun loadChatMessages(callback: (items: List<ChatMessage>, newMessages: Boolean) -> Unit)
    fun sendMessage(text: String, successCallback: () -> Unit, errorCallback: () -> Unit)
}

data class ChatMessage(val author: String, val timestamp: Long, val text: String, val highlight: Boolean, val isOwn: Boolean)