package com.example.customchatbot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.customchatbot.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.FileInputStream
import java.util.Properties

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chatAdapter: ChatAdapter

    val apiKey = BuildConfig.API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView 및 어댑터 초기화
        chatAdapter = ChatAdapter()
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = chatAdapter
        }

        // 전송 버튼 클릭 리스너 설정
        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()
            if (message.isNotEmpty()) {
                // 사용자 메시지를 RecyclerView에 추가
                chatAdapter.addMessage(ChatMessage(message, true))
                // 챗봇 API 호출 및 응답 처리
                requestChatbotResponse(message, apiKey)
                // EditText 내용 지우기
                binding.messageEditText.text.clear()
            }
        }
    }

    // 챗봇 API 호출 및 응답 처리 함수
    private fun requestChatbotResponse(userMessage: String, apiKey: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://hd1pj0o360.apigw.ntruss.com/custom/v1/14498/3c113aa0d89d24aa954fab4b6533e0ee42dd1c2b264c015cd7a0200f5698ea32/") // API 엔드포인트의 기본 URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ClovaChatbotService::class.java)

        val request = ChatbotRequest(userMessage)

        val call = service.sendChatMessage(apiKey, request)
        call.enqueue(object : Callback<ChatbotResponse> {
            override fun onResponse(call: Call<ChatbotResponse>, response: Response<ChatbotResponse>) {
                if (response.isSuccessful) {
                    val chatbotResponse = response.body()
                    // 챗봇 응답을 RecyclerView에 추가
                    chatbotResponse?.let { chatAdapter.addMessage(ChatMessage(it.text, false)) }
                } else {
                    Log.e("요청 실패", response.code().toString())
                }
            }

            override fun onFailure(call: Call<ChatbotResponse>, t: Throwable) {
                Log.e("네트워크 오류", t.message ?: "알 수 없는 오류")
            }
        })
    }
}