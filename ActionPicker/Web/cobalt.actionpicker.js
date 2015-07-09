(function(cobalt){
    var plugin={
        name:"actionpicker",

        init:function(options){
            //create shortcuts
            cobalt.actionPicker=this.getAction.bind(this);

            if (options){
                this.config(options);
            }

        },
        config:function(settings){
            
        },
        handleEvent:function(json){
            cobalt.log(this.name, ' plugin : unknown event received :', json)
        },
        getAction:function(option,callback){
            this.send("getAction",option, callback);
         },
        send:function(action, data, callback){
        cobalt.send({ type : "plugin", name : this.name, action : action, data : data }, callback);
        }
    };
    cobalt.plugins.register(plugin);

})(cobalt || {});