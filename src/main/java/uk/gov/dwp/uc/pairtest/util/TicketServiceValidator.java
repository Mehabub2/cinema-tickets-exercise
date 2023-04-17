package uk.gov.dwp.uc.pairtest.util;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;

import java.util.List;
import java.util.function.Predicate;


public class TicketServiceValidator {

    /**
     * @param ticketTypeRequests
     */
    public boolean checkIfAdultNotPresent(List<TicketTypeRequest> ticketTypeRequests) {

        return ticketTypeRequests.stream().map(TicketTypeRequest::getTicketType)
                .noneMatch(ticketType -> ticketType.equals(Type.ADULT));
    }

    /**
     * @param ticketTypeRequests
     */
    public boolean checkTicketQuantityInValid(List<TicketTypeRequest> ticketTypeRequests) {
        Predicate<TicketTypeRequest> infantType = ticketTypeRequest -> ticketTypeRequest.getTicketType()
                .equals(Type.INFANT);
        Integer totalNumberOFtickets = ticketTypeRequests.stream().filter(infantType.negate()) // ignoring infant in
                .map(TicketTypeRequest::getNoOfTickets).reduce(Integer::sum).get();
        return (totalNumberOFtickets > 20);
    }

    /**
     * @param ticketTypeRequests
     */
    public boolean checkInfantCountInValid(List<TicketTypeRequest> ticketTypeRequests) {

        int numberOfAdults = countTotalTicketType(ticketTypeRequests, Type.ADULT);
        int numberOfInfants = countTotalTicketType(ticketTypeRequests, Type.INFANT);
        return (numberOfInfants > numberOfAdults);
    }

    /**
     * @param ticketTypeRequests
     * @param type
     */
    public int countTotalTicketType(List<TicketTypeRequest> ticketTypeRequests, Type type) {
        Predicate<TicketTypeRequest> checkForType = (ticketTypeRequest) -> ticketTypeRequest.getTicketType().equals(type);
        return ticketTypeRequests.stream()
                .filter(checkForType)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }

}
