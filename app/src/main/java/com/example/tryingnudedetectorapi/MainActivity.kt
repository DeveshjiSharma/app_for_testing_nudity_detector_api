package com.example.tryingnudedetectorapi



import android.app.Activity
import android.content.Intent

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.get
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var textView:TextView
    private lateinit var but1: Button
    private lateinit var but2: Button
    private lateinit var imageView:ImageView
    private lateinit var client:OkHttpClient
    private lateinit var request:RequestBody
    private var getUrl:String="https://catfact.ninja/fact"
    private var postUrl:String="https://api.genderize.io/?name=hugi"
    private var REQ_CODE=12
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView=findViewById(R.id.textData)
        but1=findViewById(R.id.btnGet)
        but2=findViewById(R.id.btnPost)
        client= OkHttpClient()
        imageView=findViewById(R.id.imageView)

        but1.setOnClickListener {
//            val intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.type = "image/*"
//            startActivityForResult(intent, REQ_CODE)

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(intent,REQ_CODE)
            Toast.makeText(this,"Clicked",Toast.LENGTH_SHORT).show()
        }
        but2.setOnClickListener {
          getExample()
        }
    }



    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQ_CODE && resultCode==Activity.RESULT_OK && data?.data!=null){
            val imageUri=data.data
            if(imageUri != null){
                imageView.setImageURI(imageUri)
                uploadImage1(imageUri)
            }else{
                Toast.makeText(this, "Failed to retrieve image", Toast.LENGTH_SHORT).show()
            }

        }
    }
    private fun uploadImage(imageUri : Uri){
        val file = File(imageUri.path!!)
        Toast.makeText(this, "${file.exists()} or not",Toast.LENGTH_LONG).show()
        val mediaType="image/jpeg".toMediaTypeOrNull()
//        val body =file.asRequestBody(mediaType)
           val body=MultipartBody.Builder()
               .setType(MultipartBody.FORM)
               .addFormDataPart("image",file.name,
                   file.asRequestBody("image/jpef".toMediaTypeOrNull())
               )
               .build()
    val request = Request.Builder()
            .url("http://192.168.175.19:3001/analyze")
            .post(body)
            .build()

        client.newCall(request).enqueue(object:Callback{
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    textView.text=e.stackTraceToString()
                    Log.d("Failed",e.stackTraceToString())
                    Toast.makeText(this@MainActivity,"${e.stackTraceToString()} Failed",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responsebody=response.body?.string()
                runOnUiThread {
                    if(response.isSuccessful){
                        textView.text=responsebody
                        Toast.makeText(this@MainActivity,"${responsebody} Done",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this@MainActivity,"Failed while sending",Toast.LENGTH_SHORT).show()
                    }
                }

            }

        })


    }
    private fun uploadImage1(imageUri: Uri) {
        val inputStream = contentResolver.openInputStream(imageUri)
        inputStream?.use { input ->
            val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
            val mediaType = "image/jpeg".toMediaTypeOrNull()
            val requestBody=MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image",tempFile.name,
                    tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .build()
            val request = Request.Builder()
                .url("http://192.168.175.19:3001/analyze")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        textView.text=e.stackTraceToString()
                        Log.d("Failed",e.stackTraceToString())
                        Toast.makeText(this@MainActivity,"${e.stackTraceToString()} Failed",Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onResponse(call: Call, response: Response) {
                    val responsebody=response.body?.string()
                    runOnUiThread {
                        if(response.isSuccessful){
                            textView.text=responsebody
                            Toast.makeText(this@MainActivity,"$responsebody Done",Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this@MainActivity,"Failed while sending",Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            })
        } ?: run {
            runOnUiThread {
                Toast.makeText(this@MainActivity," Failed",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getExample(){
      val request=Request.Builder()
          .url(getUrl)
          .build()

      client.newCall(request).enqueue(object: Callback {
          override fun onFailure(call: Call, e: IOException) {
              e.printStackTrace()
              // Display error message
              runOnUiThread {
                  Toast.makeText(this@MainActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
              }
          }

          override fun onResponse(call: Call, response: Response) {
             val responseData=response.body?.string()
              runOnUiThread{
                  textView.text=responseData
                  Toast.makeText(this@MainActivity,responseData,Toast.LENGTH_SHORT).show()
              }
          }

      })
  }

}
