package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	private static ParkingService parkingService;

	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;

	@BeforeEach
	private void setUpPerTest() {
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	}

	@Test
	public void processExitingVehicleTest() {
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
			Ticket ticket = new Ticket();
			ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
			ticket.setParkingSpot(parkingSpot);
			ticket.setVehicleRegNumber("ABCDEF");
			when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
			when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
			when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
			when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

			parkingService.processExitingVehicle(new Date());

			verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
			verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}

	}

//	test of processIncomingVehicle() where everything work as wanted.
	@Test
	public void processIncomingVehicleTest() {
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
			when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
			when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
			when(inputReaderUtil.readSelection()).thenReturn(1);
			when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
			when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

			parkingService.processIncomingVehicle();

			verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
			verify(parkingSpotDAO, Mockito.times(1)).updateParking(parkingSpot);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}

	}

//	Test of processExitingVehicule() when ticketDAO.updateTicket() return false: 
	@Test
	public void processExitingVehicleTestUnableUpdate() {
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
			Ticket ticket = new Ticket();
			ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
			ticket.setParkingSpot(parkingSpot);
			ticket.setVehicleRegNumber("ABCDEF");
			when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
			when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
			when(ticketDAO.getNbTicket(anyString())).thenReturn(0);

			parkingService.processExitingVehicle(new Date());

			verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
			verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
			verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
			verify(parkingSpotDAO, Mockito.never()).updateParking(any(ParkingSpot.class));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}
	}

	@Test
	public void testGetNextParkingNumberIfAvailable() {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

		ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

		assertEquals(1, result.getId());
		assertTrue(result.isAvailable());
		System.out.println("ParkingSpot n°" + result.getId() + " available.");
	}

	// Test of getNextParkingNumberIfAvailable() method when no parking is available
	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);

		ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

		assertNull(result);

	}

	// Test of getNextParkingNumberIfAvailable() method when vehicle type is unknown
	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
		when(inputReaderUtil.readSelection()).thenReturn(3);
		ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

		assertNull(result);
	}

}
