ActionPicker Plugin
===============

ActionPicker plugin allows you to show a list of action and make callback for them.


How to use
----------

* import the plugin to your project as explained [here](https://github.com/cobaltians/cobalt/wiki/Using-plugins)
* Add the cobalt.actionpicker.js to your web JS folder
* Add an html link to the cobalt.actionpicker.js plugin script after the cobalt link in the HEAD tag

use the cobalt.actionPicker shortcut like this

    cobalt.actionPicker({
        text : "What you want to do?", // ios only
        actions : [ "Chose a picture", "Take a picture", "Take a video" ],
        cancel : "Cancel", // ios only
    },function(data){
        cobalt.log('picker callback', data)
        document.getElementById('choiceField').innerHTML = "Choice n°"+JSON.stringify(data.index);
    });

function(data) allow you to do stuff when you click on an action.

("Chose a picture"= {"index":0};"Take a picture"= {"index":1};"Take a video"= {"index":2}).


ToDo
----------------

iOS Version.
