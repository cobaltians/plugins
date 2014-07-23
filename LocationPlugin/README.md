LocationPlugin
===============

Location plugin allows you to get the geolocation of the user.


How to use
----------


Run the script that will certainly be done in a few days...

Link to the cobalt.loaction.js plugin script after the cobalt link in the HEAD or use the generated cobalt.plugins.js

use the cobalt.getLocation shortcut like this

    //somewhere after cobalt inited
    cobalt.getLocation(function(position){
        //You received postion. It contains 'longitude' and 'latitude' as numbers.
        cobalt.log('postion is :', position)
    });


Planned features
----------------

Next features are:
 * error/user-should-enable-location callback and errors
 * registering and unregistering to location changes
 * ...
