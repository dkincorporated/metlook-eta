package dev.dkong.metlook.eta

import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.common.VehicleData
import dev.dkong.metlook.eta.common.vehicle.Train
import org.junit.Test

/**
 * Test vehicle type identification
 */
internal class VehicleDataTest {
    /**
     * Test HCMT identification
     */
    @Test
    fun testHcmt() {
        assert(VehicleData.getVehicle("9026M-9926M", RouteType.Train) is Train.Hcmt)
    }

    /**
     * Test X'Trapolis identification
     */
    @Test
    fun testXtrapolis() {
        assert(
            VehicleData.getVehicle(
                "1388T-1413T-175M-176M-225M-226M",
                RouteType.Train
            ) is Train.Xtrapolis
        )
        assert(
            VehicleData.getVehicle(
                "1427T-1652T-253M-254M-903M-904M",
                RouteType.Train
            ) is Train.Xtrapolis
        )
    }

    /**
     * Test Siemens identification
     */
    @Test
    fun testSiemens() {
        assert(
            VehicleData.getVehicle(
                "2542T-2569T-783M-784M-837M-838M",
                RouteType.Train
            ) is Train.Siemens
        )
    }

    /**
     * Test Comeng identification
     */
    @Test
    fun testComeng() {
        val vehicleInput = "1170T-1171T-639M-640M-653M-654M"
        assert(VehicleData.getVehicle(vehicleInput, RouteType.Train) is Train.Comeng)
    }
}