package com.example.calculadora_penal


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Shapes
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import java.util.Calendar



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
    @POST("calculate")
    suspend fun calculate(@Body req: CalcRequest): CalcResult
}

class MainActivity : ComponentActivity() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/") // usar 10.0.2.2 para emulador; em device real, IP do server
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(ApiService::class.java)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PenalApp(api)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
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
        shape = CircleShape,
        containerColor = Color(0xFF25D366),
        contentColor = Color.White,
        modifier = Modifier.size(56.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = "Abrir WhatsApp",
            modifier = Modifier.size(28.dp),
            tint = Color.White
        )
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
            Spacer(Modifier.height(48.dp))
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
fun ResultadosView(result:  CalcResult?) {
    val focusManager = LocalFocusManager.current

    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var processo by remember { mutableStateOf("") }

    result?.let { res ->
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {

            // Card de Resultados
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                elevation = 4.dp
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

            Spacer(Modifier.height(16.dp))
            Text("Envie o seu resultado para um de nossos advogados:", fontWeight = FontWeight.Bold,  fontSize = 18.0.sp)
            Spacer(Modifier.height(16.dp))

            // Formulário
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = {
                    Text(buildAnnotatedString {
                        append("Nome Completo")
                        withStyle(SpanStyle(color = Color.Red)) { append(" *") }
                    })
                },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true
            )

            Spacer(Modifier.height(5.dp))

            OutlinedTextField(
                value = telefone,
                onValueChange = { telefone = it },
                label = {
                    Text(buildAnnotatedString {
                        append("Número (WhatsApp)")
                        withStyle(SpanStyle(color = Color.Red)) { append(" *") }
                    })
                },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true
            )

            Spacer(Modifier.height(5.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true
            )

            Spacer(Modifier.height(5.dp))

            OutlinedTextField(
                value = processo,
                onValueChange = { processo = it },
                label = { Text("Número do Processo") },
                placeholder = { Text("NNNNNNN-DD.AAAA.J.TR.OOOO") },
                leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {

                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Enviar")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalculatorScreen(api: ApiService, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var years by remember { mutableStateOf("0") }
    var months by remember { mutableStateOf("0") }
    var days by remember { mutableStateOf("0") }

    var diasTrabalhados by remember { mutableStateOf("0") }
    var horasEstudadas by remember { mutableStateOf("0") }

//    var dataInicio by remember { mutableStateOf("") }
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

    var dataInicioDisplay by remember { mutableStateOf("") }  // Mostrado na tela (DD/MM/AAAA)
    var dataInicioISO by remember { mutableStateOf("") }       // Enviado ao backend (YYYY-MM-DD)

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // Formata para exibição e envio
            dataInicioDisplay = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            dataInicioISO = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Surface(
        modifier = Modifier
            .fillMaxSize().verticalScroll(rememberScrollState())
            .padding(12.dp), color = Color.White
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Calculadora Penal",color = Color.Black, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 32.0.sp)
            Spacer(Modifier.height(12.dp))
            Text("Tempo de pena total", fontSize = 18.0.sp)
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


            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                ) {
                    Text("Remição", textDecoration = TextDecoration.Underline, fontSize = 18.0.sp)
                    Spacer(Modifier.height(10.dp))
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

                Row(
                    modifier = Modifier
                        .height(360.dp).padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VerticalDivider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(8.dp),
                        color = Color(215, 146, 77),
                        thickness = 4.dp
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                ) {
                    Text("Dados da Pena", textDecoration = TextDecoration.Underline, fontSize = 18.0.sp)

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = dataInicioDisplay,
                        onValueChange = {},
                        label = { Text("Data de Início") },
                        placeholder = { Text("Selecione a data") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Selecionar data")
                            }
                        },
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


            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    error = null
                    result = null
                    if (dataInicioISO.isBlank()) {
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
                                dataInicio = dataInicioISO,
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
            ResultadosView(result = result)

            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 12.dp)
            ) {
                Divider(thickness = 1.dp, color = Color(215, 146, 77))
                Spacer(Modifier.height(12.dp))

                Text(
                    "Quero falar com um advogado",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A4BA8)
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Atendimento rápido via WhatsApp",
                        fontSize = 15.sp,
                        color = Color.Gray
                    )

                    WhatsAppNewChatButton()
                }

                Spacer(Modifier.height(8.dp))
                Divider(thickness = 1.dp, color = Color(215, 146, 77))
            }
        }
        }
    }



