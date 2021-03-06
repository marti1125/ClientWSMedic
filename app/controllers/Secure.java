package controllers;

import play.*;
import play.libs.*;
import play.libs.WS.HttpResponse;
import play.mvc.*;

import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyRep.Type;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import models.*;

public class Secure extends Controller {
	
	public static void login(){
		render();
	}
	
	public static void validation(String codigo, String password) throws UnsupportedEncodingException{
		
		String wsServer = Play.configuration.getProperty("Server-WS");
		
		HttpResponse res = WS.url(wsServer + "rest/UserInfoService/login")
				.setParameter("codigo", codigo)
				.setParameter("password", MySQLPassword(password)).get();
		
		String content = res.getString();
		System.out.println("contenido"  + content);
		String codigoWS = "";
		String nombreCompletoWS = "";
		
		try {
			
			Document doc = convertStringToDocument(content);
			
			doc.getDocumentElement().normalize();
			 
			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
		 
			NodeList nList = doc.getElementsByTagName("usuario");
		 
			System.out.println("----------------------------");
		 
			for (int temp = 0; temp < nList.getLength(); temp++) {
		 
				Node nNode = nList.item(temp);
		 
				System.out.println("\nCurrent Element :" + nNode.getNodeName());
		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
					Element eElement = (Element) nNode;
					
					codigoWS = eElement.getElementsByTagName("codigo").item(0).getTextContent();
					nombreCompletoWS = eElement.getElementsByTagName("nombreCompleto").item(0).getTextContent();
					
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		if(!codigoWS.equals("noexiste")){
			System.out.println("error   "+codigoWS);
			session.put("codigo", codigoWS);
			session.put("nombreCompleto", nombreCompletoWS);
			redirect("/");
		} else {
			System.out.println("error   "+codigoWS);
			flash.error("error");
			login();
		}
		
	}
	
	public static void logout(){
		session.clear();
		flash.success("exito");
		redirect("/secure/login");
	}
	
	private static Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
        DocumentBuilder builder; 
        try 
        { 
            builder = factory.newDocumentBuilder(); 
            Document doc = builder.parse( new InputSource( new StringReader( xmlStr ) ) );
            return doc;
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return null;
    }
	
	public static String MySQLPassword(String plainText) throws UnsupportedEncodingException {
	    byte[] utf8 = plainText.getBytes("UTF-8");
	    return "*" + DigestUtils.sha1Hex(DigestUtils.sha1(utf8)).toUpperCase();
	}
	
}
