#PubSub


PubSub plugin allows you to send messages between Cobalt WebViews of the same app.

## Why this plugin ?

Each view of a Cobalt application is in a different web page and may be in a different controller. The application can share information between pages with localStorage. But sometimes, that's not enough.

## Publishing and subscribing

### The concept of PubSub

The concept of subscribing and publishing to a channel is used on many languages. Check out the [wikipedia page](https://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern) about this pattern.

In a Cobalt application, this plugin allows WebViews to talk together, to share news about data changes or anything.

### cobalt.publish

Publish a give message in the specified channel.

**cobalt.publish( channel, message )**

* `channel` : (string) The name of the chanel in which you want to send your message.
* `message` : (JSON object) The message that will be sent in the channel.

**Example :**

    cobalt.publish("myMessages",{
       myValue : 42,
       anotherValue : "foo"
    });

If the channel does not exist, it does nothing.

### cobalt.subscribe

Subscribe the current WebView to messages from the specified channel.

**cobalt.subscribe( channel, callback)**

* `channel` : (string) The name of the chanel you want to subscribe.
* `callback` : (function) A javascript function to handle the messages when they'll come. This function receives one parametter : the message sent (JSON object).

**Example :**

    cobalt.subscribe("myMessages",function(message){
       cobalt.log('received something on myMessages', message);
    });

If the channel does not exist, it creates it.

### cobalt.unsubscribe

Unsubscribe the current WebView to messages from the specified channel.

**cobalt.subscribe( channel)**

* `channel` : (string) The name of the chanel you want to unsubscribe.

**Example :**

    cobalt.unsubscribe("myMessages");

If the channel does not exist, it does nothing.


## Installing the plugin

* import the plugin to your project as explained [here](https://github.com/cobaltians/cobalt/wiki/Using-plugins)
* Add the cobalt.pubsub.js to your web JS folder
* Add an html link to the cobalt.pubsub.js plugin script after the cobalt link in the HEAD tag
* Use as described here.




