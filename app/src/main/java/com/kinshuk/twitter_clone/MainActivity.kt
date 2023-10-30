package com.kinshuk.twitter_clone

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.kinshuk.twitter_clone.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var lstOfTweets = arrayListOf<Ticket>()
    var adapter : MyTweetsAdapter ?=null
    var myEmail :String?=null
    var uid :String?=null
    var postText: String ?=null
    private var database= FirebaseDatabase.getInstance()
    private var myRef=database.reference
    private var mAuth:FirebaseAuth?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var b: Bundle? = intent.extras
        myEmail = b!!.getString("email")
        uid = b.getString("uid")
        mAuth = FirebaseAuth.getInstance()
        FirebaseApp.initializeApp(this)
        //dummy
        lstOfTweets.add(Ticket("0", "hi", "url", "add", "", "","",""))

        adapter = MyTweetsAdapter(this, lstOfTweets)
        binding.lvTweets.adapter = adapter
        println("call to kia hai bhai ne")
        LoadPosts()
    }

    inner class MyTweetsAdapter:BaseAdapter{
        var listTweetsAdapter = ArrayList<Ticket>()
        var context:Context ?=null
        constructor(context:Context,listTweetsAdapter:ArrayList<Ticket>):super(){
            this.listTweetsAdapter = listTweetsAdapter
            this.context = context
        }

        override fun getCount(): Int {
            return listTweetsAdapter.size
        }

        override fun getItem(p0: Int): Any {
            return listTweetsAdapter[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            var myTweet = listTweetsAdapter[p0]
            if(myTweet.tweetPersonUID.equals("add"))
            {
                var myView = layoutInflater.inflate(R.layout.add_ticket,null)
                var attach_iv = myView.findViewById<ImageView>(R.id.iv_attach)
                var post_iv = myView.findViewById<ImageView>(R.id.iv_post)
                var post_txt = myView.findViewById<TextView>(R.id.etPost)
                var curDate = Date()
                val formatter =  SimpleDateFormat("yyyy-MM-dd")
                val tymformatter = SimpleDateFormat("HH:mm a")
                val postdt = formatter.format(curDate) as String
                val postTym = tymformatter.format(curDate) as String
                attach_iv.setOnClickListener {
                        postText = post_txt.text.toString()
                        loadImage()
                }
                post_txt.text = postText
                post_iv.setOnClickListener {
                    if (DownloadURL != null) {
                        // upload
                        myRef.child("posts").push().setValue(PostInfo(uid!!, post_txt.text.toString(), DownloadURL!! , imagePath!! , splitString(mAuth!!.currentUser!!.email.toString()), postdt , postTym))
                        Toast.makeText(applicationContext, "Posted!!", Toast.LENGTH_SHORT).show()
                        postText = ""
                        post_txt.text = postText
                    } else {
                        Toast.makeText(applicationContext, "Image not uploaded yet. Please select an image first.", Toast.LENGTH_LONG).show()
                    }
                }

                return myView
            }
            else if(myTweet.tweetPersonUID.equals("loading"))
            {
                return layoutInflater.inflate(R.layout.loading_ticket,null)
            }
            else
            {
                var myView = layoutInflater.inflate(R.layout.tweets_ticket,null)
                var tv = myView.findViewById<TextView>(R.id.txt_tweet)
                var userid = myView.findViewById<TextView>(R.id.usename)
                var post = myView.findViewById<ImageView>(R.id.postimg)
                tv.text = myTweet.tweetText
                var date = myView.findViewById<TextView>(R.id.date)
                var time = myView.findViewById<TextView>(R.id.liked)
                date.text = myTweet.TweetDate
                time.text = myTweet.TweetTime
                userid.text = myTweet.UserName
                val imgId = myTweet.imgpath

                val storageRef = FirebaseStorage.getInstance().reference.child("images/imagePost/$imgId")
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    Picasso.with(context).load(downloadUrl).into(post)
                }.addOnFailureListener {
                    Toast.makeText(this@MainActivity,it.message,Toast.LENGTH_SHORT).show()
                }

                myRef.child("Users").child(myTweet.tweetPersonUID!!)
                    .addValueEventListener(object :ValueEventListener{

                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            try {

                                var td= dataSnapshot.value as HashMap<String,Any>

                                for(key in td.keys){

                                    var userInfo= td[key] as String
                                    if(key.equals("DpPath")){

                                        //
                                        var dp = myView.findViewById<ImageView>(R.id.dp)
                                        val DpId = userInfo
                                        val strRef = FirebaseStorage.getInstance().reference.child("images/images/$DpId.jpg")
                                        strRef.downloadUrl.addOnSuccessListener { uri ->
                                            val downloadUrl = uri.toString()
                                            Picasso.with(context).load(downloadUrl).into(dp)
                                        }.addOnFailureListener {

                                            Toast.makeText(this@MainActivity,"images/images/$DpId",Toast.LENGTH_SHORT).show()
                                        }
                                        //
                                    }
                                }

                            }catch (ex:Exception){}


                        }

                        override fun onCancelled(p0: DatabaseError) {

                        }
                    })

                return myView
            }
        }
    }



    //Load image
    val PICK_IMAGE_CODE = 253

    fun loadImage(){
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,PICK_IMAGE_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_CODE && data !=null){
            val selectedImage = data.data
            val filePathColum = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage!!,filePathColum,null,null,null)
            cursor!!.moveToFirst()
            val colindex = cursor.getColumnIndex(filePathColum[0])
            val picturePath = cursor.getString(colindex)
            cursor.close()
            Upload(BitmapFactory.decodeFile(picturePath))
        }
    }
    var DownloadURL:String?=null
    var imagePath: String?= null
    fun Upload(bitmap:Bitmap){

        lstOfTweets.add(0,Ticket("0", "hi", "url", "loading", "", "","",""))
        adapter!!.notifyDataSetChanged()
        val storage= FirebaseStorage.getInstance()
        val storgaRef=storage.getReferenceFromUrl("gs://twitter-clone-46e75.appspot.com/images")
        val df= SimpleDateFormat("ddMMyyHHmmss")
        val dataobj= Date()
        imagePath = splitString(myEmail) + "."+ df.format(dataobj)+ ".jpg"
        val ImageRef=storgaRef.child("imagePost/$imagePath")
        val baos= ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data= baos.toByteArray()
        val uploadTask=ImageRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext,"fail to upload", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener { taskSnapshot ->

            DownloadURL= taskSnapshot.storage.downloadUrl.toString()
            lstOfTweets.removeAt(0)
            adapter!!.notifyDataSetChanged()

        }

    }

    private fun splitString(myEmail: String?): String {
        return myEmail!!.split("@")[0]
    }

    fun LoadPosts(){
        println("chl to rha h")
        myRef.child("posts")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        println("Ye bhi theek hai")
                        lstOfTweets.clear()
                        lstOfTweets.add(Ticket("0","hi","url","add" , "","","",""))
                        var td = snapshot.value as HashMap<String,Any>
                        for(key in td.keys){

                            var post = td[key] as HashMap<String,Any>
                            lstOfTweets.add(Ticket(key,post["text"] as String,post["postImage"] as String,post["userUID"] as String , post["imgPath"] as String , post["userName"] as String , post["date"] as String , post["time"] as String))
                        }
                        adapter!!.notifyDataSetChanged()
                    }catch (ex:Exception){
                        Log.e("LoadPosts", "Database Error: ${ex.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("LoadPosts", "Database Error: ${error.message}")
                }

            })
    }
}