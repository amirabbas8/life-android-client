package net.saoshyant.Life.app

import android.app.Activity
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.util.Log

import net.saoshyant.Life.R

import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by Amir Abbas on 13/03/2017.
 */

class Upload {
    fun uploadFile(activity: Activity, coordinatorLayout: CoordinatorLayout?, finalFile: File, address: String): Int {


        var serverResponseCode = 0
        val fileName = finalFile.name
        val conn: HttpURLConnection
        val dos: DataOutputStream
        val lineEnd = "\r\n"
        val twoHyphens = "--"
        val boundary = "*****"
        var bytesRead: Int
        var bytesAvailable: Int
        var bufferSize: Int
        val buffer: ByteArray
        val maxBufferSize = 1 * 1024 * 1024

        if (!finalFile.isFile) {


            activity.runOnUiThread { Snackbar.make(coordinatorLayout!!, "Source File not exist ", Snackbar.LENGTH_LONG).show() }

            return 0

        } else {
            try {

                // open a URL connection to the Servlet
                val fileInputStream = FileInputStream(finalFile)
                val url = URL(address)

                // Open a HTTP  connection to  the URL
                conn = url.openConnection() as HttpURLConnection
                conn.doInput = true // Allow Inputs
                conn.doOutput = true // Allow Outputs
                conn.useCaches = false // Don't use a Cached Copy
                conn.requestMethod = "POST"
                conn.setRequestProperty("Connection", "Keep-Alive")
                conn.setRequestProperty("ENCTYPE", "multipart/form-data")
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
                conn.setRequestProperty("uploaded_file", fileName)

                dos = DataOutputStream(conn.outputStream)

                dos.writeBytes(twoHyphens + boundary + lineEnd)
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd)

                dos.writeBytes(lineEnd)

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available()

                bufferSize = Math.min(bytesAvailable, maxBufferSize)
                buffer = ByteArray(bufferSize)

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize)

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize)
                    bytesAvailable = fileInputStream.available()
                    bufferSize = Math.min(bytesAvailable, maxBufferSize)
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize)

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd)
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

                // Responses from the server (code and message)
                serverResponseCode = conn.responseCode

                if (serverResponseCode == 200) {

                    activity.runOnUiThread { }
                }

                //close the streams //
                fileInputStream.close()
                dos.flush()
                dos.close()

            } catch (ex: MalformedURLException) {

                ex.printStackTrace()

                activity.runOnUiThread {
                    //  Toast.makeText(MainActivity.this, "MalformedURLException Exception : check script url.", Toast.LENGTH_SHORT).show();
                }

                Log.e("Upload file to server", "error: " + ex.message, ex)
            } catch (e: Exception) {

                e.printStackTrace()

                activity.runOnUiThread { Snackbar.make(coordinatorLayout!!, R.string.errorconection, Snackbar.LENGTH_LONG).show() }
                Log.e("Upload file", e.message, e)
            }

            return serverResponseCode

        } // End else block
    }

}
