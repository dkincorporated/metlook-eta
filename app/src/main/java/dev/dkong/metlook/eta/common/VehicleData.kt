package dev.dkong.metlook.eta.common

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
    fun getVehicle(id: String?, routeType: RouteType): VehicleInfo? {
        if (id == null) return null
        return VehicleInfo(
            id,
            id,
            "PTV"
        )
//        return when (routeType) {
//            RouteType.Train -> {
//                // Attempt to match HCMT
//                val hcmtMatch = Regex("""(\b\d{1,2}m)\b""").find(id)
//                hcmtMatch?.let { match ->
//                    val hcmtNumber = match.groupValues[1]
//                    return VehicleInfo(
//                        "High-Capacity Metro Train",
//                        "Set $hcmtNumber",
//                        "PTV",
//                        7
//                    )
//                }
//                // Not HCMT; go by carriage number
//                return null
//            }
//
//            else -> null
//        }
    }
}