package com.example.calculadora_penal


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.*



// Model classes correspondentes ao backend
data class CalcRequest(
    val totalYears: Int,
    val totalMonths: Int,
    val totalDays: Int,
    val diasTrabalhados: Int,
    val horasEstudadas: Int,
    val dataInicio: String,
    val crimeType: String,
    val agravantes: Map<String, Boolean>,
    val regimeInicial: String
)

data class CalcResult(
    val inputSummary: Map<String, Any>?,
    val results: List<Map<String, String>>?,
    val liberdadeCondicional: String?
)

interface ApiService {
    @POST("/calculate")
    suspend fun calculate(@Body req: CalcRequest): CalcResult
}

class MainActivity : ComponentActivity() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000") // usar 10.0.2.2 para emulador; em device real, IP do server
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PenalApp(api)
        }
    }
}

@Composable
fun PenalApp(api: ApiService) {
    var screen by remember { mutableStateOf(1) }
    if (screen == 1) {
        HomeScreen { screen = 2 }
    } else {
        CalculatorScreen(api) { screen = 1 }
    }
}

@Composable
fun WhatsAppNewChatButton() {
    val context = LocalContext.current

    FloatingActionButton(
        onClick = {
            val url =
                "https://api.whatsapp.com/send/?phone=5511989498044&text=Ol%C3%A1%2C+sou+advogado%2C+como+posso+ajudar%3F&type=phone_number&app_absent=0"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFF25D366),
        contentColor = Color.White
    ) {
        Icon(Icons.Filled.Phone, contentDescription = "New chat")
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RegimeDropdown(
    regimeOptions: List<String>,
    selectedRegime: String,
    onRegimeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedRegime,
            onValueChange = {},
            readOnly = true,
            label = { Text("Regime inicial") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            regimeOptions.forEach { option ->
                DropdownMenuItem(
                    content = { Text(option) },
                    onClick = {
                        onRegimeSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CrimeDropdown(
    crimeOptions: List<String>,
    selectedCrime: String,
    onCrimeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedCrime,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo do crime") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            crimeOptions.forEach { option ->
                DropdownMenuItem(
                    content = { Text(option) },
                    onClick = {
                        onCrimeSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun HomeScreen(onCalculateClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0, 12, 80)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // placeholder logo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_branca),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onCalculateClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(215, 146, 77)
                )
            ) {
                Text("Calcular Pena")
            }
        }
    }
}

@Composable
fun CalculatorScreen(api: ApiService, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var years by remember { mutableStateOf("0") }
    var months by remember { mutableStateOf("0") }
    var days by remember { mutableStateOf("0") }

    var diasTrabalhados by remember { mutableStateOf("0") }
    var horasEstudadas by remember { mutableStateOf("0") }

    var dataInicio by remember { mutableStateOf("") } // DD/MM/aaaa
    val crimeOptions = listOf(
        "Comum",
        "Hediondo",
        "Hediondo com resultado de morte",
        "Organização criminosa (crime hediondo)",
        "Tráfico de drogas"
    )
    var selectedCrime by remember { mutableStateOf(crimeOptions[0]) }

    var reincidente by remember { mutableStateOf(false) }
    var violencia by remember { mutableStateOf(false) }

    val regimeOptions = listOf("Fechado", "Semi-aberto", "Aberto")
    var selectedRegime by remember { mutableStateOf(regimeOptions[0]) }

    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<CalcResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxSize().verticalScroll(rememberScrollState())
            .padding(12.dp), color = Color.White
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Tempo de pena total")
            Row {
                OutlinedTextField(
                    value = years,
                    onValueChange = { years = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Anos") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = months,
                    onValueChange = { months = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Meses") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Dias") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Aqui: colocar as seções uma abaixo da outra
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                ) {
                    Text("Remição")
                    OutlinedTextField(
                        value = diasTrabalhados,
                        onValueChange = { diasTrabalhados = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Dias trabalhados") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = horasEstudadas,
                        onValueChange = { horasEstudadas = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Horas estudadas") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                ) {
                    Text("Dados da Pena")
                    OutlinedTextField(
                        value = dataInicio,
                        onValueChange = { dataInicio = it },
                        label = { Text("Data de início (DD/MM/AAAA)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    )

                    CrimeDropdown(
                        crimeOptions = crimeOptions,
                        selectedCrime = selectedCrime,
                        onCrimeSelected = { selectedCrime = it }
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = reincidente, onCheckedChange = { reincidente = it })
                        Text("Reincidente")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = violencia, onCheckedChange = { violencia = it })
                        Text("Crime com violência ou grave ameaça")
                    }

                    RegimeDropdown(
                        regimeOptions = regimeOptions,
                        selectedRegime = selectedRegime,
                        onRegimeSelected = { selectedRegime = it }
                    )
                }
            }

            // Botão e resultados abaixo do Row
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    // validações simples
                    error = null
                    result = null
                    if (dataInicio.isBlank()) {
                        error = "Informe a data de início"
                        return@Button
                    }
                    loading = true
                    scope.launch {
                        try {
                            val req = CalcRequest(
                                totalYears = years.toIntOrNull() ?: 0,
                                totalMonths = months.toIntOrNull() ?: 0,
                                totalDays = days.toIntOrNull() ?: 0,
                                diasTrabalhados = diasTrabalhados.toIntOrNull() ?: 0,
                                horasEstudadas = horasEstudadas.toIntOrNull() ?: 0,
                                dataInicio = dataInicio,
                                crimeType = selectedCrime,
                                agravantes = mapOf(
                                    "reincidente" to reincidente,
                                    "violencia" to violencia
                                ),
                                regimeInicial = selectedRegime
                            )
                            val resp = api.calculate(req)
                            result = resp
                        } catch (e: Exception) {
                            error = "Erro ao conectar ao servidor: ${e.message}"
                        } finally {
                            loading = false
                        }
                    }

                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(215, 146, 77),
                    contentColor = Color.White
                )
            ) {
                Text("Calcular")
            }

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            }
            error?.let { Text(it, color = Color.Red) }

            Spacer(Modifier.height(12.dp))

            // Resultados
            result?.let { res ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp), elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Progressões e Benefícios", style = MaterialTheme.typography.h6)
                        Spacer(Modifier.height(8.dp))
                        res.results?.forEach { item ->
                            Divider()
                            Spacer(Modifier.height(8.dp))
                            Text(item["title"] ?: "", color = Color(0xFF1A4BA8))
                            Text("Data prevista: ${item["date"] ?: ""}")
                            Text(item["percent"] ?: "", color = Color(0xFF4B6CB7))
                            Spacer(Modifier.height(8.dp))
                        }
                        Divider()
                        Spacer(Modifier.height(8.dp))
                        Text("Liberdade Condicional", color = Color(0xFF1A4BA8))
                        Text(
                            res.liberdadeCondicional ?: "",
                            color = if ((res.liberdadeCondicional ?: "").contains("Não há"))
                                Color.Red else Color.Black
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Entre em contato com um de nossos advogados")
                WhatsAppNewChatButton()
            }
        }
    }
}


