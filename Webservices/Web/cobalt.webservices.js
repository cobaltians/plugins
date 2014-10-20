(function(cobalt){
    var plugin={
        name:"webservices",
        onError:undefined,
        onSuccess:undefined,

        init:function(options){
            cobalt.log('init webservices plugin with options', options)

            //create shortcuts
            //cobalt.getLocation=this.getLocation.bind(this);

/*            if (options && typeof options.onError == "function"){
                this.onError=options.onError;
            }
*/
        },
        /*getLocation:function(callback){
            if (typeof callback== "function"){
                this.onSuccess = callback;
            }
            cobalt.log('sending getLocation call', this.onSuccess)
            cobalt.send({ type : "plugin", name:"location", action : "getLocation"})

        },*/
        handleEvent:function(json){
            cobalt.log('received native location plugin event', json)
            
        }
    };
    cobalt.plugins.register(plugin);

})(cobalt || {});