(function(cobalt){
    var plugin={
        name:"PubSub",

        init:function(options){
            //create shortcuts
            cobalt.publish=this.publish.bind(this);
            cobalt.subscribe=this.subscribe.bind(this);
            cobalt.unsubscribe=this.unsubscribe.bind(this);

        },
        publish:function(channel, message){
            if (typeof channel == "string" && typeof message == "object"){
                this.send("publish", { channel : channel, message : message });
            }else{
                cobalt.log("PubSub : channel must be a string and message should be an object");
            }
        },
        subscribe:function(channel, callback){
            var callback_id = cobalt.registerCallback(callback)
            if (typeof channel == "string" && typeof callback_id == "string"){
                this.send("subscribe", { channel : channel, callback : callback_id });
            }else{
                cobalt.log("PubSub : channel must be a string and callback should be set");
            }
        },
        unsubscribe:function(channel){
            if (typeof channel == "string"){
                this.send("unsubscribe", { channel : channel });
            }else{
                cobalt.log("PubSub : channel must be a string");
            }
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