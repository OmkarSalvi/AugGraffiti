# AugGraffiti
AugGraffiti is an augmented reality social art game.
Gameplay is simple:
1) As an artist, the user can place 3 graffiti tags in different locations.
2) User can collect other artists’ tags.
3) Collecting a tag earns the user +100 pts and earns the artist +100 pts.


Note:
Before running the app you need to do follow these steps:

Go to settings-> Apps->AugGraffiti->Permissions
then set all the permissions for the app manually. 

This is important otherwise App won't be able to run properly.
After giving permissions, you can run the app.
-------------------------------------------------------------------------------------------------------------------------------------
How to use all activities in App:

Sign-in:
Click on sign in button and select an email id to sign in with
on success you will see activity2 which is a map. 

Place tag:
On this map your current location will shown by red color marker.
clicking this marker will launch placetag activity.

Collect tag:
On this map in activity2, nearby tags will be indictaed by blue color marker. 
clicking this marker will launch collecttag activity.
These nearby tags are connected to your location tag by light blue lines. These lines show direction in which every tag is placed respective to your current location.

Gallery:
Clicking gallery button will launch gallery activity. It takes few seconds for the server to rspond and activity to retrieve the image.
Thus wait for few seconds, then you will be able to see all the tags collected by you.

Score:
On activity2 in left bottom corner you can see your score.

Signout:
On clicking signout button you will be directed to 1st login activity.
