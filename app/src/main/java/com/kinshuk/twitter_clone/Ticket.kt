package com.kinshuk.twitter_clone

import androidx.appcompat.widget.ToolbarWidgetWrapper
import kotlin.concurrent.timer

class Ticket {
    var tweetID:String?=null
    var UserName:String ?=null
    var tweetText:String?=null
    var tweetImageURL:String?=null
    var tweetPersonUID:String?=null
    var imgpath:String?=null
    var TweetDate:String?=null
    var TweetTime :String?=null
    constructor(tweetID:String ,  tweetText:String,tweetImageURL:String ,
                    tweetPersonUID:String,imgpath:String,UserName:String,TweetDate:String,TweetTime:String)
    {
        this.tweetID = tweetID
        this.tweetText = tweetText
        this.tweetImageURL = tweetImageURL
        this.imgpath = imgpath
        this.tweetPersonUID= tweetPersonUID
        this.UserName = UserName
        this.TweetDate = TweetDate
        this.TweetTime = TweetTime
    }
}