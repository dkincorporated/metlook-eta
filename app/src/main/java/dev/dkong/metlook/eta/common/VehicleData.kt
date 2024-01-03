package dev.dkong.metlook.eta.common

import androidx.core.text.isDigitsOnly
import dev.dkong.metlook.eta.common.vehicle.Train
import dev.dkong.metlook.eta.common.vehicle.Tram
import dev.dkong.metlook.eta.common.vehicle.VehicleType

/**
 * Coordinator of vehicle-related data
 */
object VehicleData {
    /**
     * Package containing processed data
     * @param name display name of the vehicle
     * @param id the fleet number of the vehicle
     * @param source the source of the vehicle information
     * @param carCount the number of cars of the vehicle (if applicable)
     */
    data class VehicleInfo(
        val name: String,
        val id: String,
        val source: String,
        val carCount: Int? = null
    )

    /**
     * Determine vehicle based on PTV-provided data
     * @param id vehicle ID as provided by data source
     * @param routeType the [RouteType] of the vehicle
     */
    fun getVehicle(id: String?, routeType: RouteType): VehicleType? {
        // Null ID cannot be identified
        id ?: return null
        return when (routeType) {
            RouteType.Train -> {
                // Attempt to match HCMT
                val hcmtMatch = Regex("(?:90|99)((\\d{2})|0\\d)M$").find(id)
                hcmtMatch?.let { match ->
                    if (match.groupValues.size < 2) return@let
                    val hcmtNumber = match.groupValues[1]
                    return Train.Hcmt("Set $hcmtNumber ($id)", 7)
                }
                // Not HCMT; go by carriage number
                val carNumberExp = Regex("""(\d+M)""")
                val carNumbers = id.split("-")
                val carCount = carNumbers.size.div(2).minus(1)
                return carNumbers
                    .find { carNumberExp.matches(it) }
                    ?.replace("M", "")
                    ?.let {
                        when (it.toInt()) {
                            in 301..699 -> Train.Comeng(id, carCount)
                            in 701..844 -> Train.Siemens(id, carCount)
                            !in 299..851 -> Train.Xtrapolis(id, carCount)

                            else -> null
                        }
                    }
            }

            RouteType.Tram -> {
                return when (id.toInt()) {
                    in 116..230 -> Tram.Z3(id)
                    in 231..258 -> Tram.A1(id)
                    in 259..300 -> Tram.A2(id)
                    in 2003..2132 -> Tram.B2(id)
                    in 3001..3036 -> Tram.C(id)
                    in arrayOf(5103, 5106, 5111, 5113, 5123) -> Tram.C2(id)
                    in 3501..3538 -> Tram.D1(id)
                    in 5001..5021 -> Tram.D2(id)
                    in 6001..6050 -> Tram.E(id)
                    in 6051..6100 -> Tram.E2(id)
                    in arrayOf(
                        856,
                        888,
                        925,
                        928,
                        946,
                        957,
                        959,
                        961,
                        981,
                        983,
                        1010
                    ) -> Tram.W8(id)

                    else -> null
                }
            }

            RouteType.Bus -> {
                // TODO: Deal with buses
                return null
            }

            else -> null
        }
    }
}