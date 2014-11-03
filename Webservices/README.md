WebservicesPlugin
===============

Webservices plugin allows you to ...


How to use
----------

* import the plugin to your project as explained [here](https://github.com/cobaltians/cobalt/wiki/Using-plugins)
* Add the cobalt.webservices.js to your web JS folder
* Add an html link to the cobalt.webservices.js plugin script after the cobalt link in the HEAD tag



### Usage on the web side

	cobalt.ws.config({
		base : {
			url : "", 
			params : {}
		},
		defaultParameters:{
			type : "GET", //default to GET
			errorCallback : function( callId, methodName, parameters ){}
		}
	});
	
	cobalt.ws.call({
		url : "", //added to base.url if defined
		params :{},
		storageKey : "", //result will be saved at this key into storage
		successCallback : function( data ){} //called at WS call end. 
		cacheCallback : function( data ){}   //called if something into storageKey
		errorCallback : function( callId, methodName, parameters ){}
	});


### Example :

	cobalt.ws.config({
		base : {
			url : "http://api.myservice.com",
			params : { apiKey : "3455345dd" }
		},
		defaultParameters:{
			type : "POST",
			errorCallback : function( callId, errorCode ){
				cobalt.log('WS ERROR', callId, errorCode);
			}
		}
	});

	cobalt.ws.call({
		url : "getUser",
		params : {
                    userId: 42
                },
		storageKey : "user:412",
		cacheCallback : function( data, relatedCall ){
			cobalt.log('latest user data was', data.user )	
		},
		successCallback : function( data, relatedCall ){
			cobalt.log('server user data is', data.user )
		}
	})
	
	cobalt.ws.call({
		storageKey : "user:412",
		cacheCallback : function( data, relatedCall ){
			if (data){
				cobalt.log('latest user data was', data.user )	
			}
		}
	})

	
### How it works

When **cobalt.ws.call** is called on JS side, JS sends this to native :
	
	{ type : "plugin", name : "webservices", action : "call", data : {
		url : ""
		params : {},
		type : "GET",
		saveToStorage : true/false,
		sendCacheResult : true/false,
		storageKey : "", //optionnal
		processData : {} //optionnal
	}, callback : 412 }
	
As soon as request comes, native calls the callback with a generated callId
	
	cobalt.sendCallback(callback, { callId : 2312 })
	
When WS server answers, native sends this to web
	
	{ type : "plugin", name : "webservices", action : "onWSResult", data : {
		callId : 2312, //previously generated callId
		data : {} // the data that was given by the server
		text : "" // the server response as text if JSON parse of the response failed
	}}
	

If error occurs, native sends this to web
	
	{ type : "plugin", name : "webservices", action : "onWSError", data : {
		callId : 2312, //previously generated callId
		data : {}, // the data that was given by the server,
		text : "", // the server response as text if JSON parse of the response failed,
		errorCode : 401 //the error code sent by the server
	}}

If saveToStorage was true, native store the result with the storageKey
If already something in storageKey and sendCacheResult was true, native sends this to the web
	
	{ type : "plugin", name : "webservices", action : "onStorageResult", data : {
		callId : 2312,
		data : {} // the data that was in storageKey,
		text : "" // the text that was in storageKey if not JSON
	}}


**getting the cache result only** :

User can also get the result form cache without calling server again like this :

	cobalt.ws.call({
		storageKey : "user:412",
		cacheCallback : function( data, relatedCall ){
			cobalt.log('latest user data was', data.user )	
		},
		cacheError : function( err, relatedCall  ){
			cobalt.log('error retrieving user ', err )	
		}
	})

In this case, native side sends a "onStorageResult" message (see above) with the data in storageKey. 

If something went wrong and sendCacheResult was true, native sends this to the web :

	{ type : "plugin", name : "webservices", action : "onStorageError", data : {
		callId : 2312,
		text : "" // the error detail as string, for example : "EMPTY", "NOT_FOUND" or "UNKNOWN_ERROR"
	}}


**processData** :

This parameter can be used to treat the data received from cache or the server before sending it back to the web.
It can be usefull to filtering, sorting, or doing anything with the data before sending it back to the web.

A method named "treatData" on the native side should be overrided to filter or sort the data.


**storeValue and storedValueForKey** :

Cobalt is storing cache data into NSUserDefault on iOS and into XXXXXXXX for Android. 
These two methos on the native side are overridable to change the way data is stored.

* storeValue takes the storageKey and the data and should store the data somewhere
* storedValueForKey takes the storageKey string as parameter and returns the data from somewhere

if data or storageKey is null, those functions are not called by the Cobalt plugin




