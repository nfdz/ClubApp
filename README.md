
⚠️  **The project is currently discontinued** ⚠️

# ClubApp
Android application to manage events, users and loyalty of your club.

The application is made in Kotlin using native Android and common design patterns so it is very easy to fork, modify and extend according to your needs.

## About
This project was developed as an open source, educational and non-profit project for a regional cultural club.

It is functional but not completely finished, there are many points that are unpolished and need to be improved. 

Feel free to fork it, hack it and make it yours :)

## Features

* Club events managed according to categories and dates.
* Confirmation of assistance through QR code.
* Loyalty of members through points system based on attendance at events.
* Alerts with notification push for administrators.
* Private club chat. It has push notifications.

## Screenshots

<p align="center">
  <img src="screenshots/member-1.png?raw=true" width="250" alt="Screenshot"/>
  <img src="screenshots/member-2.png?raw=true" width="250" alt="Screenshot"/>
  <img src="screenshots/member-3.png?raw=true" width="250" alt="Screenshot"/>
</p>
<p align="center">
  <img src="screenshots/member-4.png?raw=true" width="250" alt="Screenshot"/>
  <img src="screenshots/member-5.png?raw=true" width="250" alt="Screenshot"/>
  <img src="screenshots/member-6.png?raw=true" width="250" alt="Screenshot"/>
</p>
<p align="center">
  <img src="screenshots/admin-1.png?raw=true" width="250" alt="Screenshot"/>
  <img src="screenshots/admin-2.png?raw=true" width="250" alt="Screenshot"/>
  <img src="screenshots/admin-3.png?raw=true" width="250" alt="Screenshot"/>
</p>
<p align="center">
  <img src="screenshots/admin-4.png?raw=true" width="250" alt="Screenshot"/>
  <img src="screenshots/admin-5.png?raw=true" width="250" alt="Screenshot"/>
  <img src="screenshots/admin-6.png?raw=true" width="250" alt="Screenshot"/>
</p>

## Setup

1. Setup your **Firebase** project with your App Ids and Key hashes and copy your firebase files **google-services.json** to **adminApp** and **memberApp** folder.
2. Enable and install **Firebase Functions**. Functions are located in **firebase_functions_src** folder.
3. Enable and setup **Firebase Auth** (email/password). You can customize messages about create account, restore password, etc, there.
4. Enable administrators creating the user manually in **Firebase Auth** and you also have to add the email to a list in the **Firebase Database**.

<p align="center">
  <img src="screenshots/admin-setup.png?raw=true" width="550" alt="Create admin"/>
</p>

5. Replace everywhere **YOUR.MEMBER.APP.ID.HERE** and **YOUR.ADMIN.APP.ID.HERE** for your ids. (Ctlr + Shift + R)
6. Replace in **memberApp** **Strings.xml**  file next fields with your content: **app_name,club_about_content,club_contact_content, club_schedule_content, club_location_content, club_location_uri, club_location_uri_query, playlist_spotify_uri, club_facebook_url, club_facebook_url, event_url_host**.
7. Replace in **adminApp** **Strings.xml** file next fields with your content: **app_name**.
8. Replace in **memberApp** in drawable folder: **image_logo_land, image_logo_square, image_club_location.webp** (image of google maps with help to find your location).
9. You can modify text and images of event categories. The categories are defined in **EventCategory** enum class in **commonLibrary** project. String texts and drawables are referred from there.
10. You can modify App colors in **colors.xml** file in **membersApp** and **adminApp**.

### Pending Improvements
* Implement repository pattern and improve the Dependency Inversion (Firebase and so on).
* The adminApp is a proof of concept, implement it properly sharing logic and models with the memberApp.
* Implement more tests to improve the coverage.

## License

[GNU General Public License v3](https://www.gnu.org/licenses/gpl-3.0.en.html "GNU General Public License v3")
