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
	
})

cobalt.ws.call({
	url : "", //added to base.url if defined
	params :{},
	storageKey : "", //result will be saved at this key into storage
	successCallback : function( data ){} //called at WS call end. 
	cacheCallback : function( data ){}   //called if something into storageKey
	errorCallback : function( callId, methodName, parameters ){}
	
});



### Full usage example :

	cobalt.ws.config({
		base : {
			url : "http://api.myservice.com",
			params : { apiKey : "3455345dd" }
		},
		defaultParameters:{
			type : "POST"
			errorCallback : function( callId, methodName, parameters ){
				cobalt.log('WS ERROR', callId, methodName);
			}
		}
	);

	cobalt.ws.call({
		url : "getUser",
		userId: 42,
		storageKey : "user:412",
		cacheCallback : function( data ){
			cobalt.log('latest user data was', data.user )	
		},
		successCallback : function( data ){
			cobalt.log('server user data is', data.user )
		}
	})

	cobalt.ws.getStorageValue({
		storageKey : "user:412",
		successCallback : function( data ){
			cobalt.log('latest user data was', data.user )
		},
		errorCallback : function(storageKey){
			cobalt.log('nothing in ', storageKey )
		}
	})
	
	
### How it works

When cobalt.ws.call is called on JS side, JS sends this to native :
	
	{ type : "plugin", name : "webservices", action : "call", data : {
		url : ""
		params : {},
		type : "GET",
		saveToStorage : true/false,
		storageKey : "" //optionnal
	}, callback : 412 }
	
As soon as request comes, native calls the callback with the callId
	
	cobalt.sendCallback(callback, { callId : 2312 })
	
When WS server answers, native sends this to web
	
	{ type : "plugin", name : "webservices", action : "onWSResult", data : {
		callId : 2312,
		data : {} // the data that was given by the server
	}}
	
If error occurs, native sends this to web
	
	{ type : "plugin", name : "webservices", action : "onWSError", data : {
		callId : 2312,
		data : {} // the data that was given by the server
	}}

If already something in storageKey and saveToStorage = true, native call this on web
	
	{ type : "plugin", name : "webservices", action : "onStorageResult", data : {
		callId : 2312,
		data : {} // the data that was in storageKey
	}}