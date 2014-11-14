(function(cobalt){
    var plugin={
        name:"webservices",

        settings:{
            base:{
                url :"",
                params : {}
                ////newCall.headers['Accept'] = "*/*";
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
                pause : this.pause.bind(this),
                resume : this.resume.bind(this),
                clearPausedCalls : this.clearPausedCalls.bind(this),
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
                storageKey :    ( typeof options.storageKey =="string" && options.storageKey.length ) ? options.storageKey : undefined
            }

            if (options.url){
                newCall.url = ((/^https?:/).test(options.url)) ? options.url : self.settings.base.url + options.url;
                newCall.successCallback = (typeof options.successCallback =="function") ?  options.successCallback : self.settings.defaultParameters.successCallback || undefined;
                newCall.errorCallback = (typeof options.errorCallback =="function") ?  options.errorCallback : self.settings.defaultParameters.errorCallback || undefine;

                var params = cobalt.utils.extend( this.settings.base.params, options.params );
                if (params){
                    newCall.params = cobalt.utils.param(params);
                }
                newCall.type = options.type || this.settings.defaultParameters.type;

                newCall.headers = cobalt.utils.extend( this.settings.defaultParameters.headers, options.headers );
                if (! newCall.headers['User-Agent']){
                    newCall.headers['User-Agent'] = navigator.userAgent;
                }
            }

            if (newCall.storageKey){
                newCall.cacheCallback = (typeof options.cacheCallback =="function") ?  options.cacheCallback : self.settings.defaultParameters.cacheCallback || undefined;
                newCall.cacheError = (typeof options.cacheError =="function") ?  options.cacheError : self.settings.defaultParameters.cacheError || undefined;

                if (newCall.url){
                    newCall.saveToStorage = options.saveToStorage || this.settings.defaultParameters.saveToStorage;
                }

                if (newCall.cacheCallback){
                    newCall.sendCacheResult = options.saveToStorage || this.settings.defaultParameters.saveToStorage;
                }
            }


            self.send("call", newCall, function( data ){
                cobalt.log('WS call started with id = '+data.callId, newCall)
                newCall.callId = data.callId;
                self.calls[data.callId] = newCall;
            })


        },
        /* abort pending calls and stack all following calls
         *   call_ids = array of call ids.
         *   if none, all calls are paused
         */
        pause:function( call_ids ){
            self.send("pause", { call_ids : call_ids || [] })
        },
        /* resend aborted calls and send all stacked calls
         *   call_ids = array of call ids.
         *   if none, all stacked calls are resumed
         */
        resume:function( call_ids ){
            self.send("resume", { call_ids : call_ids || [] })
        },
        clearPausedCalls:function(){
            self.send("clearPausedCalls")
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
