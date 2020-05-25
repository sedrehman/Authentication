# Authentication
Java authentication userbase

Syed Rehman


How to run this software
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
(if you're using eclipse the cvs file to save user info will be on bin/p1/website/
otherwise in src/p1/website/)

to run on windown :

cd into 			cd authentication\src
then run the exact command 	javac p1\Server.java
then				java p1/Server 
//note that I used '/' intentionally instead of '\' sometimes so careful

to run of linux :

cd into 			cd authentication/src
then run the exact command 	javac p1/Server.java
then				java p1/Server   


the program should run on an while loop so do your testing while its running.


How to test your software
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To test the program open Chrome (as I tested on Chrome). Search (must be https)-

https://localhost:8080/

My website should load. You can check control+shift+i  to check the responses and 
requests. 

Navigate at your lesure. There are 3 sites.-

	https://localhost:8080/index.html
	https://localhost:8080/login.html
	https://localhost:8080/profile.html

Objectives~
1. If you create an account, you must remember your password and email. They will
be saved in userFile.csv (you 'may' delete it innitially, it will be recreated tho)
your password will be saved in the file at column 2 in the form sha256 + salt 

2. For step I just mentioned, if you entered a password under 8 characters, or 
email length less than 5 or if the length of your favorite class is less than 2.
The page will let you know which one of the requirements hasn't been met. Once 
everything is entered correctly, your accrount will be made.

3. Now if you just signed up it will auto log you in. To test authentication you must
open up an incognito browser or simply delete the cookie from with ctrl + shift + i and
under the application tag. Then you can navigate to log in. Note just extension "/" is 
routed to "/login.html" for user's convenience. If you enter correct email and password
you will be logged in. Otherwise an error message will tell your credentials are not
correct.

4. Once you're logged in, a token is sent to your browser in the form of cookie. If you 
opened up a new tab and went to 
https://localhost:8080/ 
or
https://localhost:8080/login.html
you will be auto logged in.

Im using AJAX for this profile.html. 

The architecture of your software
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

I used java. And I'm strictly using SSLSocket. Where Im using my seft certified cert
for tls-1.2. For the sha-256 I'm using "java.security.MessageDigest". This is built in 
library in Java 8. Every request is handled by a new java Thread, multithreading is used
here.

For the front end, nothing but html, css and javascript is used. 



