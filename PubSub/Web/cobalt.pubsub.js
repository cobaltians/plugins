(function(cobalt){
    var plugin={
        name:"PubSub",

        init:function(options){
            //create shortcuts
            
			
            if (options){
                this.config(options);
            }

        },
        config:function(settings){
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