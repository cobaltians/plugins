(function(cobalt){
    var plugin={
        name:"appInfos",

        init:function(options){
            //create shortcuts
            cobalt.getAppInfos = this.getAppInfos.bind(this);
			
            if (options){
                this.config(options);
            }

        },
        config:function(settings){
        },
        /*
            Callback is called with an object of app informations. This object contains :
            {
                versionName : "1.5.2" //the public verbose version number of the app.
                versionCode : 4, //the build number of the app (for app submissions).
            }
         */
        getAppInfos:function(callback){
            this.send("getAppInfos", {}, function( data ){
				if (typeof callback == "function"){
					callback(data);
				}else{
                    cobalt.log('Received infos = ',data, typeof callback)
                }
            })
        },
        handleEvent:function(json){
            cobalt.log(this.name, ' plugin : unknown event received :', json)
        },
        send:function(action, data, callback){
			cobalt.send({ type : "plugin", name : this.name, action : action, data : data }, callback);
        }
    };
    cobalt.plugins.register(plugin);

})(cobalt || {});