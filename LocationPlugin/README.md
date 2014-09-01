LocationPlugin
===============

Location plugin allows you to get the geolocation of the user.


How to use
----------

* import the plugin to your project as explained [here](https://github.com/cobaltians/cobalt/wiki/Using-plugins)
* Add the cobalt.loaction.js to your web JS folder
* Add an html link to the cobalt.location.js plugin script after the cobalt link in the HEAD tag

use the cobalt.getLocation shortcut like this

    //somewhere after cobalt inited
    cobalt.getLocation(function(position){
        //You received postion. It contains 'longitude' and 'latitude' as numbers.
        cobalt.log('postion is :', position)
    });


You can handle error messages like this :

    cobalt.init({
        plugins:{
            location:{
                onError:function(code, text){
                    //handle errors
                }
            }
        }
    });
    
Known error codes and texts are :

* DISABLED : Location detection has been disabled by user
* NULL : No location found



Planned features
----------------

Next features are:
 * registering and unregistering to location changes
 * ...
