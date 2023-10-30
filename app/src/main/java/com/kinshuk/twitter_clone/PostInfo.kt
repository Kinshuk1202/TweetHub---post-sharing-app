package com.kinshuk.twitter_clone

class PostInfo {
    var UserUID :String? = null
    var text :String? = null
    var postImage :String? = null
    var imgPath:String?=null
    var UserName:String?=null
    var Date:String?=null
    var time :String?=null
    constructor(UserUID :String,text :String,postImage :String,imgPath:String,UserName:String,Date:String, time :String)
    {
        this.UserUID = UserUID
        this.postImage = postImage
        this.text = text
        this.imgPath = imgPath
        this.UserName = UserName
        this.Date = Date
        this.time = time
    }
}