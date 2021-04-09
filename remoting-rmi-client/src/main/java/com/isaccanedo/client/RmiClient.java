package com.isaccanedo.client;

import com.isaccanedo.api.Booking;
import com.isaccanedo.api.BookingException;
import com.isaccanedo.api.CabBookingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

@SpringBootApplication public class RmiClient {

    @Bean RmiProxyFactoryBean service() {
        RmiProxyFactoryBean rmiProxyFactory = new RmiProxyFactoryBean();
        rmiProxyFactory.setServiceUrl("rmi://localhost:1099/CabBookingService");
        rmiProxyFactory.setServiceInterface(CabBookingService.class);
        return rmiProxyFactory;
    }

    public static void main(String[] args) throws BookingException {
        CabBookingService service = SpringApplication.run(RmiClient.class, args).getBean(CabBookingService.class);
        Booking bookingOutcome = service.bookRide("13 Seagate Blvd, Key Largo, FL 33037");
        System.out.println(bookingOutcome);
    }

}
