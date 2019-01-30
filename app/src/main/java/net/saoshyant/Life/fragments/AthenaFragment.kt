package net.saoshyant.Life.fragments

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import net.saoshyant.Life.R
import net.saoshyant.Life.app.DatabaseHandler
import net.saoshyant.Life.app.MyApplication
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class AthenaFragment : Fragment() {
    private var txtOutput: TextView? = null
    private var txtInput: TextView? = null
    private var coordinatorLayout: CoordinatorLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_athena, container, false)
        coordinatorLayout = rootView.findViewById(R.id.coordinatorLayout) as CoordinatorLayout
        txtOutput = rootView.findViewById(R.id.txtOutput) as TextView
        txtInput = rootView.findViewById(R.id.txtInput) as TextView
        val imgHelp = rootView.findViewById(R.id.imgHelp) as ImageButton
        val btnSpeak = rootView.findViewById(R.id.btnSpeak) as ImageButton
        btnSpeak.setOnClickListener { promptSpeechInput() }
        imgHelp.setOnClickListener {
            val dialog = Dialog(activity!!)
            dialog.setContentView(R.layout.athena_help_dialog)
            dialog.setTitle(R.string.help)
            val ok = dialog.findViewById(R.id.ok) as ImageButton

            ok.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
        return rootView

    }

    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "با من صحبت کن")
        try {
            startActivityForResult(intent, 100)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(activity!!.applicationContext, "گوشیت نداره", Toast.LENGTH_SHORT).show()
        }

    }


    //Receiving speech input

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            100 -> {
                if (resultCode == RESULT_OK && null != data) {

                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    txtInput!!.text = result[0]
                    Processsend(result[0])
                }
            }
        }
    }

    private fun Processsend(input: String) {

        val db1 = DatabaseHandler(activity!!.applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails
        val params = HashMap<String, String>()
        params["cap"] = "mind"
        params["id"] = user["idNo"]!!
        params["code"] = user["code"]!!
        params["input"] = input
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/atina.php", Response.Listener { response ->
            Log.e("plus", response)
            try {
                val responseObj = JSONObject(response)
                val feedArray = responseObj.getJSONArray("mind")
                val feedObj = feedArray.get(0) as JSONObject
                txtOutput!!.text = feedObj.getString("output")
            } catch (e: JSONException) {
                //Log.e("plus", e.getMessage());
                Snackbar.make(coordinatorLayout!!, R.string.errorproblem, Snackbar.LENGTH_LONG).show()
            }
        },
                Response.ErrorListener { Snackbar.make(coordinatorLayout!!, R.string.errorconection, Snackbar.LENGTH_LONG).show() }
        ) {

            /* Passing user parameters to our server
             * @return*/
            override fun getParams(): Map<String, String> {
                // Log.e(TAG, "Posting params: " + params.toString());
                return params
            }
        }
        // Adding request to request queue
        MyApplication.instance!!.addToRequestQueue(strReq)
    }
}// Required empty public constructor
