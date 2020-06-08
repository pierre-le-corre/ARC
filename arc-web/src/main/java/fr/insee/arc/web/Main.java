package fr.insee.arc.web;

import org.springframework.context.support.GenericXmlApplicationContext;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class Main {

    @SuppressWarnings("resource")
	public static void main(String[] args) {

    GenericXmlApplicationContext ctx = new GenericXmlApplicationContext("applicationContext.xml");

	@SuppressWarnings("unused")
	PropertiesHandler propertitiesHandler = (PropertiesHandler) ctx.getBean("properties");
	
	System.out.println(propertitiesHandler.getDatabaseDriverClassName());
   
    }

}