# chat
A group chat based android app

An android app named 'chat' for group chatting. Using the app you can chat with anybody who is already registered in the app. The chat box can send images,videos as well as audios.Moreover, it can show the number of unread messages in one login session.

The activities in the app :-

## 1. MainActivity.class
This activity is used for user login and is the first activity when we start the app.
A user can login using his  name and email-id. On successful login he is directed to Chats.class.

<img src="/Screenshots/5.png" align="left" height="400" width="250" >__

## 2. Main2Activity.class
This activity is used for user registration. A user will register using his name, email-id and a profile-pic(optional). On successful registration he is directed to Chats.class.
![alt text](/Screenshots/1.png "MainActivity.class")<br />
## 3. Chats.class
This activity displays a list of all the group chats having one of the members as  the user who logged in.
![alt text](/Screenshots/9.png "MainActivity.class")
## 4. Groupname.class
This activity will appear after you click the 'Create Group' option in the overflow menu in Chats.class. Here, you mention the name and group-pic(optional) of the group your are creating. After clicking on 'Proceed' you are directed to CreateGroup.class .
![alt text](/Screenshots/8.png "MainActivity.class")
## 5. CreateGroup.class
This activity displays list of all users who are registered with the app. Here you could choose your other group members. After clicking on 'Create' your group gets created and you are directed to ChatRoomActivity.class.
![alt text](/Screenshots/11.png "MainActivity.class")
## 6. ChatRoomActivity.class
This activity is basically a chat box where you send and receive messages. You could send text, images, videos as well as audios.
![alt text](/Screenshots/12.png "MainActivity.class")
![alt text](/Screenshots/4.png "MainActivity.class")
## 7. group_inf.class
This activity will be visible after you click on the toolbar in ChatRoomActivity.class. This activity will show you all the group details.
![alt text](/Screenshots/13.png "MainActivity.class")
![alt text](/Screenshots/14.png "MainActivity.class")
![alt text](/Screenshots/15.png "MainActivity.class")
## 8. chk.class
This activity is used to view a profile image on full screen . Whenever you click on a image icon  in any activity (except group_inf.class) , chk.class gets started displaying the selected image on full screen.
![alt text](/Screenshots/7.png "MainActivity.class")


> Moreover, we have used Firebase cloud messgaing for getting chat notifications.
