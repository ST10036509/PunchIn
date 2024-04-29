package za.co.varsitycollege.st10036509.punchin.activities
/*
AUTHOR: Leonard Bester
CREATED: 29/04/2024
LAST MODIFIED: 29/04/2024
*/
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityProjectViewBinding


class ProjectViewActivity : AppCompatActivity(){

    private lateinit var binding: ActivityProjectViewBinding // Bind the ProjectViewActivity with the KT and XML files

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityProjectViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}

