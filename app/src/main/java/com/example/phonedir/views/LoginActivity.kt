package com.example.phonedir.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.phonedir.R
import com.example.phonedir.data.Result
import com.example.phonedir.data.entities.UserEntity
import com.example.phonedir.data.model.LoginRequestModel
import com.example.phonedir.data.model.LoginResponseModel
import com.example.phonedir.databinding.ActivityLoginBinding
import com.example.phonedir.repository.MainRepository
import com.example.phonedir.viewmodel.MainViewModel
import com.example.phonedir.viewmodel.MainViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: MainViewModel

    @Inject
    lateinit var mainRepository: MainRepository

    private var userList: ArrayList<UserEntity> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(mainRepository)
        )[MainViewModel::class.java]

        binding.loginButton.setOnClickListener {
            loginUser()
        }

        CoroutineScope(Dispatchers.Main).launch {
            viewModel.getAllUserData().collectLatest { userDbList->
                userList = userDbList as ArrayList<UserEntity>
            }
        }
    }

    private fun loginUser() {

        val userEmail = binding.inputEmail.text.toString()
        val password = binding.inputPassword.text.toString()

        when {
            userEmail.isEmpty() -> {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                return
            }

            password.isEmpty() -> {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val loginRequestModel = LoginRequestModel(
            email = userEmail,
            password = password
        )

        viewModel.loginUser(loginRequestModel)

        viewModel.userData.observe(this) { result ->
            when (result) {
                is Result.Error -> {
                    Toast.makeText(this, result.exception.message, Toast.LENGTH_SHORT).show()
                    binding.loginButton.visibility = View.VISIBLE
                    binding.progressCircular.visibility = View.GONE
                    viewModel.userData.removeObservers(this)
                }

                is Result.Loading -> {
                    binding.loginButton.visibility = View.GONE
                    binding.progressCircular.visibility = View.VISIBLE
                }

                is Result.Success -> {
                    insertUserData(result.data)

                }
            }
        }



    }

    private fun insertUserData(data: LoginResponseModel) {
        CoroutineScope(Dispatchers.IO).launch {

            listOf(
                async {
                    if (userList.isEmpty()){
                        viewModel.insertUserData(UserEntity(
                            userId = data.user.id,
                            email = data.user.email,
                            userName = data.user.username.toString(),
                            name = data.user.name,
                            accessToken = data.accessToken,
                            firstTimeCALLStatus = 1,
                            firstTimeSMSStatus = 1
                        ))
                    }else{
                        viewModel.deleteAllUserData()
                        viewModel.insertUserData(UserEntity(
                            userId = data.user.id,
                            email = data.user.email,
                            userName = data.user.username.toString(),
                            name = data.user.name,
                            accessToken = data.accessToken,
                            firstTimeCALLStatus = 1,
                            firstTimeSMSStatus = 1
                        ))
                    }
                }.await(),

                async {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            )


        }
    }
}