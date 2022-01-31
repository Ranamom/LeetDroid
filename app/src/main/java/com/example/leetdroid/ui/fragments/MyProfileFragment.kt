package com.example.leetdroid.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

import com.bumptech.glide.Glide

import com.example.leetdroid.api.LeetCodeRequests
import com.example.leetdroid.api.URL

import com.example.leetdroid.data.entitiy.User
import com.example.leetdroid.data.viewModel.UserViewModel

import com.example.leetdroid.databinding.FragmentMyProfileBinding
import com.example.leetdroid.extensions.showSnackBar
import com.example.leetdroid.model.UserProfileErrorModel
import com.example.leetdroid.model.UserProfileModel
import com.example.leetdroid.ui.base.BaseFragment

import com.example.leetdroid.utils.Converters.ContributionsNodeConverters.fromContributionsNode
import com.example.leetdroid.utils.Converters.ContributionsNodeConverters.fromStringContributions
import com.example.leetdroid.utils.Converters.MatchedUserNodeConverters.fromMatchedUserNode
import com.example.leetdroid.utils.Converters.MatchedUserNodeConverters.fromStringMatchedUser

import com.example.leetdroid.utils.Converters.ProfileNodeConverters.fromProfileNode
import com.example.leetdroid.utils.Converters.ProfileNodeConverters.fromStringProfileNode
import com.example.leetdroid.utils.Converters.SubmitStatsNodeConverters.fromStringSubmitStats

import com.example.leetdroid.utils.Converters.SubmitStatsNodeConverters.fromSubmitStatsNode
import com.example.leetdroid.utils.JsonUtils
import com.example.leetdroid.utils.Preferences
import com.google.gson.Gson
import kotlinx.coroutines.launch

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class MyProfileFragment : BaseFragment() {

    private lateinit var myProfileBinding: FragmentMyProfileBinding
    private lateinit var userViewModel: UserViewModel
    private var user: User? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        myProfileBinding = FragmentMyProfileBinding.inflate(layoutInflater)
        val rootView = myProfileBinding.root

        userViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[UserViewModel::class.java]

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val preferences = Preferences(requireContext())
        if (!preferences.userDataLoaded) {
            loadUser()
            preferences.userDataLoaded = true
        } else {
            userViewModel.getUser.observe(viewLifecycleOwner, { it ->
                it?.let {
                    setupProfile(it)
                }
            })
        }
    }

    // setup profile from room database
    private fun setupProfile(user: User) {
        val matchedUser = fromStringMatchedUser(user.matchedUser)
        val contributions = fromStringContributions(user.contributions)
        val profile = fromStringProfileNode(user.profile)
        val acSubmissionNum = fromStringSubmitStats(user.acSubmissionNum)
        val totalSubmissionNum = fromStringSubmitStats(user.totalSubmissionNum)

        myProfileBinding.username.text =
            profile?.realName
        Glide.with(requireContext())
            .load(
                profile?.userAvatar
            )
            .circleCrop()
            .placeholder(android.R.drawable.progress_indeterminate_horizontal)
            .into(myProfileBinding.userProfileAvatar)
    }

    // load user from online
    private fun loadUser() {

        val call: Call = createApiCall()
        call.enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.d(Constant.TAG, call.toString(), e)
            }

            override fun onResponse(call: Call, response: Response) {

                val body = response.body!!.string()
                val userData: UserProfileModel = JsonUtils.generateObjectFromJson(
                    body,
                    UserProfileModel::class.java
                )
                if (userData.data?.matchedUser == null) {
                    val errorData: UserProfileErrorModel = JsonUtils.generateObjectFromJson(
                        body,
                        UserProfileErrorModel::class.java
                    )
                    if (errorData.errors?.get(0)?.message.toString() == "That user does not exist.") {
                        showSnackBar(requireActivity(), "user does not exist")
                        return
                    } else {
                        showSnackBar(
                            requireActivity(),
                            "Something went wrong, please try again later"
                        )
                        return
                    }
                }
                val user = User(
                    fromMatchedUserNode(userData.data?.matchedUser).toString(),
                    fromContributionsNode(userData.data?.matchedUser?.contributions!!).toString(),
                    fromProfileNode(userData.data?.matchedUser?.profile!!).toString(),
                    fromSubmitStatsNode(userData.data?.matchedUser?.submitStats?.acSubmissionNum!!).toString(),
                    fromSubmitStatsNode(userData.data?.matchedUser?.submitStats?.totalSubmissionNum!!).toString()
                )
                addUpdateUser(user)
            }
        })
    }

    // creates an okHttpClient call for user data
    private fun createApiCall(): Call {
        val okHttpClient = OkHttpClient()
        val postBody =
            Gson().toJson(LeetCodeRequests.Helper.getUserProfileRequest("dtausdvascudgtasvjdasyckgdj"))
        val requestBody: RequestBody =
            postBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val headers: Headers = Headers.Builder()
            .add("Content-Type", "application/json")
            .build()
        val request: Request = Request.Builder()
            .headers(headers)
            .post(requestBody)
            .url(URL.graphql)
            .build()
        return okHttpClient.newCall(request)
    }

    // add or update data
    private fun addUpdateUser(user: User) {
        val preferences = Preferences(requireContext())
        lifecycleScope.launch {
            if (!preferences.userAdded) {
                preferences.userAdded = true
                userViewModel.addUser(user)
            } else {
                user.id = 1
                userViewModel.updateUser(user)
            }
            userViewModel.getUser.observe(viewLifecycleOwner, { it ->
                it?.let {
                    setupProfile(it)
                }
            })
        }
    }

    object Constant {
        const val TAG = "MyProfileFragment"
    }
}