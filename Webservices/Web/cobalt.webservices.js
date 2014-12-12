(function(cobalt){
    var plugin={
        name:"webservices",

        settings:{
            base:{
                url :"",
                params : {}
            },
            defaultParameters:{
                type : "GET",
                saveToStorage : true,
                filterData : undefined,
                sendCacheResult : true
            }
        },
        calls:{},

        init:function(options){
            //create shortcuts
            cobalt.ws={
                config : this.config.bind(this),
                call : this.call.bind(this),
                retry : this.retry.bind(this),
                clearStorage : this.clearStorage.bind(this)
            }
            if (options){
                this.config(options);
            }

        },
        config:function(settings){
            cobalt.log('config webservices plugin with settings', settings)
            if (settings){
                if (settings.base){
                    this.settings.base = cobalt.utils.extend(this.settings.base, settings.base);
                }
                if (settings.defaultParameters){
                    this.settings.defaultParameters = cobalt.utils.extend(this.settings.defaultParameters, settings.defaultParameters);
                }
            }
        },
        call:function(options){
            var self=this;
            var newCall = {
                processData :    options.processData || this.settings.defaultParameters.processData,
                storageKey :    ( typeof options.storageKey =="string" && options.storageKey.length ) ? options.storageKey : undefined,
                isWebLayer :    options.isWebLayer || undefined
            }
            if (options.url){

                newCall.type = options.type || this.settings.defaultParameters.type;

                newCall.headers = cobalt.utils.extend( this.settings.defaultParameters.headers, options.headers );
                if (! newCall.headers['User-Agent']){
                    newCall.headers['User-Agent'] = navigator.userAgent;
                }

                newCall.url = ((/^https?:/).test(options.url)) ? options.url : self.settings.base.url + options.url;
                newCall.successCallback = (typeof options.successCallback =="function") ?  options.successCallback : self.settings.defaultParameters.successCallback || undefined;
                newCall.errorCallback = (typeof options.errorCallback =="function") ?  options.errorCallback : self.settings.defaultParameters.errorCallback || undefined;


                var params = cobalt.utils.extend({}, this.settings.base.params); //to avoid default modifications.
                if (options.params){
                    switch (typeof options.params){
                        case "object":
                            params = cobalt.utils.extend( params, options.params );
                            break;
                        case "string":
                            cobalt.log('WS warning, params is a string so any default parameter will be ignored. Use an object to combine params with default params.')
                            params = options.params;
                            break;
                        default :
                            cobalt.log('WS warning, params should be a string or object')
                    }
                }
                if (params){
                    switch (typeof params){
                        case "object":
                            if ( (newCall.type=="POST" ||newCall.type=="PUT"||newCall.type=="DELETE") && ( newCall.headers && newCall.headers['Content-Type'] == "application/json")){
                                newCall.params = JSON.stringify(params);
                                newCall.params = (newCall.params != "{}") ? newCall.params : "";
                            }else{
                                newCall.params = cobalt.utils.param(params)
                            }
                            break;
                        case "string":
                            newCall.params = params;
                            break;
                    }
                }
            }

            if (newCall.storageKey){
                newCall.cacheCallback = (typeof options.cacheCallback =="function") ?  options.cacheCallback : self.settings.defaultParameters.cacheCallback || undefined;
                newCall.cacheError = (typeof options.cacheError =="function") ?  options.cacheError : self.settings.defaultParameters.cacheError || undefined;

                if (newCall.url){
                    newCall.saveToStorage = options.saveToStorage || this.settings.defaultParameters.saveToStorage;
                }

                if (newCall.cacheCallback){
                    newCall.sendCacheResult = options.sendCacheResult || this.settings.defaultParameters.sendCacheResult;
                }
            }


            self.send("call", newCall, function( data ){
                cobalt.log('WS call started with id = '+data.callId, newCall)
                newCall.callId = data.callId;
                self.calls[data.callId] = newCall;
            })
        },
        retry:function(callId, callNewData){
            var self=this;
            if (callId && this.calls[callId]){
                var oldCall = this.calls[callId];
                var newCall = callNewData;

                newCall.successCallback = oldCall.successCallback;
                newCall.errorCallback = oldCall.errorCallback;
                newCall.cacheCallback = oldCall.cacheCallback;
                newCall.cacheError = oldCall.cacheError;

                self.send("call", newCall, function( data ){
                    cobalt.log('WS call retryed with id = '+data.callId, newCall)
                    newCall.callId = data.callId;
                    self.calls[data.callId] = newCall;
                });

            }else{
                cobalt.log('WS error : no callId to retry')
            }

        },
        clearStorage:function(){
            self.send("clearStorage")
        },
        handleEvent:function(json){
            cobalt.log('received webservices plugin event', json)
            var data = ( json && json.data ) ? json.data : undefined;
            if (data && data.callId && this.calls[data.callId]){
                var concernedCall = this.calls[data.callId];
                switch (json.action){
                    case "onWSError":
                        if (concernedCall.errorCallback){
                            concernedCall.errorCallback( data.data || data.text, data.statusCode, concernedCall )
                        }else{
                            cobalt.log('WS error : No JS error callback for call ' + data.callId)
                        }
                        break;
                    case "onWSResult":
                        if (concernedCall.successCallback){
                            concernedCall.successCallback( data.data || data.text, data.statusCode, concernedCall )
                        }else{
                            cobalt.log('WS success but no JS callback for call ' + data.callId)
                        }
                        break;
                    case "onStorageResult":
                        if (concernedCall.cacheCallback){
                            concernedCall.cacheCallback( data.data || data.text, concernedCall )
                        }else{
                            cobalt.log('Some data in storage but no JS callback for call '+ data.callId)
                        }
                        break;
                    case "onStorageError":
                        if (concernedCall.cacheError){
                            concernedCall.cacheError( data.data || data.text, concernedCall )
                        }else{
                            cobalt.log('Some data in storage but no JS callback for call '+ data.callId)
                        }
                        break;
                    default :
                        cobalt.log('WS unknown action received from native side for call '+ data.callId);

                }
            }else{
                cobalt.log('WS unknown call event for call ', json)
            }


            
        },
        send:function(action, data, callback){
            cobalt.send({ type : "plugin", name : "webservices", action : action, data : data }, callback);
        }
    };
    cobalt.plugins.register(plugin);

})(cobalt || {});