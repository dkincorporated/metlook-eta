package dev.dkong.metlook.eta.common.vehicle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Template for Trams
 * @param name display name of the vehicle
 * @param classId the class of the vehicle
 * @param id the fleet number of the vehicle
 */
sealed class Tram(name: String, private val classId: String, id: String) :
    VehicleType(name = name, id = id, icon = {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .requiredSize(48.dp)
        ) {
            Text(
                text = classId,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
    }) {
    class Z3(id: String) : Tram("Comeng", "Z3", id)
    class A1(id: String) : Tram("Comeng", "A1", id)
    class A2(id: String) : Tram("Comeng", "A2", id)
    class B2(id: String) : Tram("Comeng", "B2", id)
    class C(id: String) : Tram("Alstom Citadis", "C", id)
    class D1(id: String) : Tram("Siemens Combino", "D1", id)
    class D2(id: String) : Tram("Siemens Combino", "D2", id)
    class C2(id: String) : Tram("Alstom Citadis", "C2", id)
    class E(id: String) : Tram("Bombardier Flexity Swift", "E", id)
    class E2(id: String) : Tram("Bombardier Flexity Swift", "E2", id)
    class W8(id: String) : Tram("MMTB", "W8", id)
}