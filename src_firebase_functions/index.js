const functions = require('firebase-functions');

// Create and Deploy Your First Cloud Functions
// https://firebase.google.com/docs/functions/write-firebase-functions

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

exports.createChatMessage = functions.firestore.document('chat_messages/{messageId}').onCreate(event => {
  // TODO limit message storage number in database -> delete old ones

  const data = event.data()
  let authorAlias = data.authorAlias;
  let text = data.text;

  const message = (0 === text.length) ? "Nuevo mensaje" : (`${authorAlias}: ${text}`)
  pushChatMessage(message);
  return true;
});

// Function to push notification to a topic.
function pushChatMessage(message) {
  var payload = {
  	notification: {
      title: "Chat",
      body: message
    },
    data: {
      pushType: "chat"
    }
  };

  admin.messaging().sendToTopic("chat", payload, { 'time_to_live': 0 })
  .then(function(response) {
    console.log("Successfully sent message:", response);
    return true;
  })
  .catch(function(error) {
    console.log("Error sending message:", error);
  });
}

exports.createAlertMessage = functions.firestore.document('alerts/{alertId}').onCreate(event => {
  // TODO limit alerts storage number in database -> delete old ones

  const data = event.data()
  let title = data.title;
  let message = data.message;

  if (!(0 === title.length || 0 === message.length)) {
    pushAlertMessage(title, message);
  }
  return true;
});

// Function to push notification to a topic.
function pushAlertMessage(title, message) {
  var payload = {
  	notification: {
      title: title,
      body: message
    }
  };

  admin.messaging().sendToTopic("chat", payload, { 'time_to_live': 86400 })
  .then(function(response) {
    console.log("Successfully sent message:", response);
    return true;
  })
  .catch(function(error) {
    console.log("Error sending message:", error);
  });
}
