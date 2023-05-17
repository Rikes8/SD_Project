package com.example.demo;

import com.example.demo.beans.WebServerRMI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Class principal da aplicação
 */
@SpringBootApplication
public class DemoApplication {


	public static void main(String[] args) throws RemoteException {
		SpringApplication.run(DemoApplication.class, args);
	}

	/**
	 * metodo que retorna uma referencia do SearchModule
	 * @return Referencia WebServerRMI
	 * @throws RemoteException
	 */
	@Bean
	public WebServerRMI server() throws RemoteException {
		return new WebServerRMI();
	}

}
