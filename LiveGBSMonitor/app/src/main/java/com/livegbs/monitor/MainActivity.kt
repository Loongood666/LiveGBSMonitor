import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private var token: String = ""
    private val channelList = mutableListOf<Channel>()
    private lateinit var lvChannels: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        lvChannels = findViewById<ListView>(R.id.lvChannels)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            login(username, password)
        }

        lvChannels.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val channel = channelList[position]
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("streamUrl", channel.streamUrl)
            startActivity(intent)
        }
    }

    private fun login(username: String, password: String) {
        val loginRequest = LoginRequest(username, password)
        RetrofitClient.apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse?.code == 200 && loginResponse.data != null) {
                        token = loginResponse.data!!.token
                        Toast.makeText(this@MainActivity, "登录成功", Toast.LENGTH_SHORT).show()
                        getChannelList()
                    } else {
                        Toast.makeText(this@MainActivity, "登录失败：${loginResponse?.msg}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "登录接口请求失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "网络错误：${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getChannelList() {
        RetrofitClient.apiService.getChannelList("Bearer $token").enqueue(object : Callback<ChannelResponse> {
            override fun onResponse(call: Call<ChannelResponse>, response: Response<ChannelResponse>) {
                if (response.isSuccessful) {
                    val channelResponse = response.body()
                    if (channelResponse?.code == 200 && channelResponse.data.isNotEmpty()) {
                        channelList.clear()
                        channelList.addAll(channelResponse.data)
                        val adapter = ArrayAdapter(
                            this@MainActivity,
                            android.R.layout.simple_list_item_1,
                            channelList.map { "${it.deviceName}-${it.name}" }
                        )
                        lvChannels.adapter = adapter
                        lvChannels.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this@MainActivity, "暂无通道数据", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "获取通道列表失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ChannelResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "网络错误：${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}