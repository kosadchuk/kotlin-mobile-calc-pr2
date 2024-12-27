package com.example.secondlabkotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.secondlabkotlin.ui.theme.SecondLabKotlinTheme
import kotlin.math.round


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecondLabKotlinTheme {
                HarmfulSubstancesEmissionsCalculator()
            }
        }
    }
}

@Composable
fun HarmfulSubstancesEmissionsCalculator() {
    val fuelSelectOptions = listOf(
        "Донецьке газове вугілля марки ГР" to "coal",
        "Високосірчистий мазут марки 40" to "mazut",
        "Природний газ із газопроводу Уренгой-Ужгород" to "gas")
    var selectedFuel by remember { mutableStateOf("") }
    var fuelWeight by remember { mutableStateOf("") }

    var result by remember { mutableStateOf <Map<String, Double>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Калькулятор викидів шкідливих речовин")

        Text(text = "Оберіть паливо:")
        fuelSelectOptions.forEach { (fuelName, id) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedFuel == id,
                    onClick = { selectedFuel = id }
                )
                Text(text = fuelName)
            }
        }

        TextField(
            value = fuelWeight,
            onValueChange = { fuelWeight = it },
            label = { Text(text = "Маса палива (тонни або тис. м³)") }
        )

        Button(
            onClick = {
                result = calculateEmissions(
                    fuel = selectedFuel,
                    fuelWeight = fuelWeight.toDouble()
                )
            },
            enabled = selectedFuel.isNotEmpty() && fuelWeight.isNotEmpty()
        ) {
            Text(text = "Розрахувати")
        }

        result?.let { map ->
            if (map.isNotEmpty()) {
                val k_value =  String.format("%.2f", map["k_value"])
                val E_value =  String.format("%.2f", map["E_value"])
                Text(text = "Показник емісії твердих частинок: $k_value г/ГДж")
                Text(text = "Валовий викид: $E_value т.")
            }
        }
    }

}

fun calculateEmissions(fuel: String, fuelWeight: Double): Map<String, Double> {

    val flueGasCleaningEfficiency = 0.985
    var lowerCombustionHeat = 0.0
    var flyAshFraction = 0.0
    var A_value = 0.0
    var combustibleSubstancesInEmissions = 0.0

    when (fuel) {
        "coal" -> {
            lowerCombustionHeat = 20.47
            flyAshFraction = 0.80
            A_value = 25.20
            combustibleSubstancesInEmissions = 1.5
        }
        "mazut" -> {
            lowerCombustionHeat = 39.48
            flyAshFraction = 1.0
            A_value = 0.15
            combustibleSubstancesInEmissions = 0.0
        }
        "gas" -> {
            lowerCombustionHeat = 33.08
            flyAshFraction = 0.0
            A_value = 25.20
            combustibleSubstancesInEmissions = 1.5
        }
    }
    val k_value = (1_000_000 / lowerCombustionHeat) * flyAshFraction *
            (A_value / (100 - combustibleSubstancesInEmissions)) *
            (1 - flueGasCleaningEfficiency)
    val E_value = 1e-6 * k_value *  lowerCombustionHeat * fuelWeight
    return mapOf(
        "k_value" to k_value,
        "E_value" to E_value
    )
}
