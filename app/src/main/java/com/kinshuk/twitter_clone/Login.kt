package com.kinshuk.twitter_clone

import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthSettings
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.core.Context
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kinshuk.twitter_clone.databinding.ActivityLoginBinding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class Login : AppCompatActivity() {
    private var mAuth:FirebaseAuth?=null
    private lateinit var binding: ActivityLoginBinding
    private var database= FirebaseDatabase.getInstance()
    private var myRef=database.reference
    var newuser = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth=FirebaseAuth.getInstance()
        if(newuser.equals(true)) {
            binding.ivPerson.setOnClickListener {
                newuser = false
                checkPermission()
            }
        }
        else
            LoadTweets()


    }
    val READIMAGE:Int = 253
    fun checkPermission(){
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),READIMAGE)
        }
        loadImage()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        when(requestCode){
            READIMAGE->{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    loadImage()
                }
//                else{
//                    Toast.makeText(this,"Cannot access",Toast.LENGTH_LONG).show()
//                }
            }
            else->super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    fun LoginToFireBase(email:String,password:String)
    {
        mAuth!!.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){task->
                if(task.isSuccessful) {
                    Toast.makeText(this, "Successfull Login", Toast.LENGTH_LONG).show()
                    SaveImageInFirebase()
                }
                else
                {
                    mAuth!!.signInWithEmailAndPassword(email,password).addOnCompleteListener(this){
                        if(it.isSuccessful){
                            Toast.makeText(this, "Successfull Login", Toast.LENGTH_LONG).show()
                            LoadTweets()
                        }
                        else{
                            Toast.makeText(this, "Failed Login", Toast.LENGTH_LONG).show()
                        }
                    }
                }

            }
    }
    fun SaveImageInFirebase(){
        var currentUser =mAuth!!.currentUser
        val email:String=currentUser!!.email.toString()
        val storage=FirebaseStorage.getInstance()
        val storgaRef=storage.getReferenceFromUrl("gs://twitter-clone-46e75.appspot.com/images")
        val df=SimpleDateFormat("ddMMyyHHmmss")
        val dataobj=Date()
        val DpPath= splitString(email) + "."+ df.format(dataobj)
        val imagePath= "$DpPath.jpg"
        val ImageRef=storgaRef.child("images/$imagePath")
        binding.ivPerson.isDrawingCacheEnabled=true
        binding.ivPerson.buildDrawingCache()

        val drawable=binding.ivPerson.drawable as BitmapDrawable
        val bitmap=drawable.bitmap
        val baos=ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data= baos.toByteArray()
        val uploadTask=ImageRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext,"fail to upload",Toast.LENGTH_LONG).show()
        }.addOnSuccessListener { taskSnapshot ->

            var DownloadURL= taskSnapshot.storage.downloadUrl.toString()!!

            myRef.child("Users").child(currentUser.uid).child("email").setValue(currentUser.email)
            myRef.child("Users").child(currentUser.uid).child("ProfileImage").setValue(DownloadURL)
            myRef.child("Users").child(currentUser.uid).child("DpPath").setValue(DpPath)
            LoadTweets()
        }

    }

    private fun splitString(s: String): String {
        return  s.split("@")[0]
    }

    fun LoadTweets(){
        var currentuser = mAuth!!.currentUser
        if(currentuser!=null)
        {
            var intent = Intent(this,MainActivity::class.java)
            intent.putExtra("email",currentuser.email)
            intent.putExtra("uid",currentuser.uid)
            startActivity(intent)
            finish()
        }
    }

    val PICK_IMAGE_CODE = 253

    fun loadImage(){
        var intent = Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,PICK_IMAGE_CODE)

    }

    @Deprecated("Deprecated in Java")
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
            binding.ivPerson.setImageBitmap(BitmapFactory.decodeFile(picturePath))
        }
    }
    fun buLogin(view: View) {
        LoginToFireBase(binding.mail.text.toString() , binding.pass.text.toString())
    }
}