package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.User;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.util.Messages;
import uk.gov.dwp.uc.pairtest.util.TicketServiceValidator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class TicketServiceImpl implements TicketService {

    TicketPaymentService paymentService;
    SeatReservationService reservationServcie;
    final TicketServiceValidator validator = new TicketServiceValidator();
    int paymentAmount;
    int numberOfSeatsForReservation;

    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService reservationServcie) {
        super();
        this.paymentService = paymentService;
        this.reservationServcie = reservationServcie;
    }

    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(User user, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        validateRequest(user, ticketTypeRequests);
        findNumberOfSeatsAndPaymentAmount(Arrays.asList(ticketTypeRequests));
        paymentService.makePayment(user.getAccountId(), paymentAmount);
        reservationServcie.reserveSeat(user.getAccountId(), numberOfSeatsForReservation);// payment is always a success :Assumption

    }

    /**
     * @param ticketTypeRequests
     */
    private void findNumberOfSeatsAndPaymentAmount(List<TicketTypeRequest> ticketTypeRequests) {
        int numberOfAdults = validator.countTotalTicketType(ticketTypeRequests, Type.ADULT);
        int numberOfChilds = validator.countTotalTicketType(ticketTypeRequests, Type.CHILD);
        numberOfSeatsForReservation = numberOfAdults + numberOfChilds;
        paymentAmount = Math.addExact(Math.multiplyExact(numberOfAdults, Messages.ADULT_FARE),
                Math.multiplyExact(numberOfChilds, Messages.CHILD_FARE));


    }

    /**
     * @param user
     * @param ticketTypeRequests
     * @throws InvalidPurchaseException
     */
    private void validateRequest(User user, TicketTypeRequest[] ticketTypeRequests) throws InvalidPurchaseException {

        String exceptionMessage = "";

        // User Null check
        if (Objects.isNull(user)) {
            exceptionMessage = Messages.EXCEPTION_WHEN_USER_NULL;
        }

        // Account ID Positive Number check
        else if (user.validateUser().equals(Messages.INVALID_ACCOUNT_ID)) {
            exceptionMessage = Messages.INVALID_ACCOUNT_ID;
        }

        // Check If at least One Request is provided
        else if (ticketTypeRequests.length < 1) {
            exceptionMessage = Messages.EXCEPTION_WHEN_ZERO_TICKET_REQUEST;
        }

        // Check if No Adult present in Booking Request
        else if (validator.checkIfAdultNotPresent(Arrays.asList(ticketTypeRequests))) {

            exceptionMessage = Messages.EXCEPTION_WHEN_NO_ADULT;
        }

        // Check if Infant count is more than adult count in Booking Request
        else if (validator.checkInfantCountInValid(Arrays.asList(ticketTypeRequests))) {
            exceptionMessage = Messages.INFANTS_NUMBER_GREATER_THAN_ADULTS;
        }

        // Check Max of 20 bookings for Request
        else if (validator.checkTicketQuantityInValid(Arrays.asList(ticketTypeRequests))) {
            exceptionMessage = Messages.INVALID_TICKET_QUANTITY;
        }

        if (!exceptionMessage.equals("")) {
            throw new InvalidPurchaseException(exceptionMessage);
        }
    }
}
