package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();

	}

	@BeforeEach
	private void setUpPerTest() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1); // Return ParkingType.CAR
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		dataBasePrepareService.clearDataBaseEntries();

	}

	@AfterAll
	private static void tearDown() {

	}

	@Test
	public void testParkingACar() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		Ticket ticket = ticketDAO.getTicket("ABCDEF");

		assertNotNull(ticket);
		assertEquals("ABCDEF", ticket.getVehicleRegNumber());

		ParkingSpot parkingSpot = ticket.getParkingSpot();
		assertNotNull(parkingSpot);
		assertFalse(parkingSpot.isAvailable());
	}

	@Test
	public void testParkingLotExit() {
		testParkingACar();
		Date outTime = new Date();
		outTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processExitingVehicle(outTime);
		Ticket ticket = ticketDAO.getTicket("ABCDEF");

		assertNotNull(ticket.getOutTime());
		assertNotEquals(0, ticket.getPrice());

	}

	@Test
	public void testParkingLotExitRecurringUser() {
		testParkingACar();
		Date outTime = new Date();
		outTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processExitingVehicle(outTime);
		parkingService.processIncomingVehicle();
		parkingService.processExitingVehicle(outTime);
		Ticket ticket = ticketDAO.getTicket("ABCDEF");

		assertTrue(Fare.CAR_RATE_PER_HOUR > ticket.getPrice());

	}
}
