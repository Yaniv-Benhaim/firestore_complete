package tech.gamedev.firestorecomplete

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import tech.gamedev.firestorecomplete.data.models.User

class MainActivity : AppCompatActivity() {

    //REFERENCE TO A COLLECTION IN FIRESTORE
    private val personCollectionRef = Firebase.firestore.collection("users")
    private var users = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //CREATING A USER FROM FIELDS AND PASSING A NEW USER TO THE SAVE METHOD
        btnSave.setOnClickListener {

            val firstName = et1.text.toString()
            val lastName = et2.text.toString()
            val age: Int = et3.text.toString().toInt()

            val user = User(firstName, lastName, age)
            saveUser(user)
        }

        //GETTING ALL USER DOCUMENTS
        btnGetUser.setOnClickListener {
            retrieveUsers()
        }

        //LISTEN FOR REALTIME UPDATES FROM FIRESTORE
        /*subscribeToRealTimeUpdates()*/


    }

    //METHOD THAT STARTS A COROUTINE IN DISPATCHERS.IO (NOT MAIN THREAD)
    private fun saveUser(user: User) = CoroutineScope(Dispatchers.IO).launch {
        try {
            //SETTING THE USER AND CALLING .await()
            // TO PAUSE UNTIL UPLOADING IS FINISHED AND THEN GO TO NEXT LINE
            personCollectionRef.document(user.name).set(user).await()

            //GOING BACK TO MAIN THREAD SO WE CAN DO UI RELATED STUFF LIKE A TOAST
            withContext(Dispatchers.Main) {
                setToast("User saved Successfully")
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                setToast(e.message.toString())
            }
        }
    }

    //METHOD THAT STARTS A COROUTINE IN DISPATCHERS.IO (NOT MAIN THREAD)
    private fun retrieveUsers() = CoroutineScope(Dispatchers.IO).launch {
        val fromAge = etFromAge.text.toString().toInt()
        val toAge = etTillAge.text.toString().toInt()


        try {
            //CLEARING ARRAY LIST IN CASE ITS NOT EMPTY
            users.clear()

            val querySnapshot = personCollectionRef
                .whereGreaterThan("age", fromAge)
                .whereLessThan("age", toAge)
                .orderBy("age")
                .get()
                .await()


            //GETTING ALL DOCUMENTS AND SAVING TO A QUERY SNAPSHOT
            // AND CALL .await() TO LET IT FINISH BEFORE GOING TO NEXT LINE
            /* val querySnapshot = personCollectionRef.get().await()*/

            //LOOPING OVER ALL DOCUMENTS IN OUR QUERY SNAPSHOT
            for (document in querySnapshot.documents) {
                //CONVERTING DOCUMENT TO A USER OBJECT
                val user = document.toObject<User>()
                //ADDING USER OBJECT TO THE ARRAY
                users.add(user!!)
            }
            //GOING BACK TO MAIN THREAD AND SHOW THE USER ON OUR UI
            withContext(Dispatchers.Main) {
                tvUser.text = users[0].name
                setToast(users.size.toString())
            }

            //SHOWING EXCEPTION IN UI/MAIN THREAD
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                setToast(e.message.toString())
            }
        }
    }

    //METHOD THAT GET REALTIME UPDATES FROM FIRESTORE
    private fun subscribeToRealTimeUpdates() {
        personCollectionRef.addSnapshotListener { querySnapshot, error ->
            error?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            querySnapshot?.let {
                //CLEAR ARRAY
                users.clear()
                //LOOPING OVER ALL DOCUMENTS IN OUR QUERY SNAPSHOT
                for (document in it) {
                    //CONVERTING DOCUMENT TO A USER OBJECT
                    val user = document.toObject<User>()
                    //ADDING USER OBJECT TO THE ARRAY
                    users.add(user)
                    //SET TEXTVIEW TO LAST NAME IN OUT LIST
                    tvUser.text = users.last().name
                    setToast(users.size.toString())
                }

            }
        }
    }

    //SIMPLE TOAST FUNCTION
    private fun setToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }
}