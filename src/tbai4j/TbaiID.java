package tbai4j;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TbaiID {
	
	private String nif;
	private String fecha;
	private String firma;
	
	private String url="https://pruebas-ticketbai.araba.eus/tbai/qrtbai/";
	private String serie;
	private String numfact;
	private String importe;
	
	public TbaiID(Document docsig) throws XPathExpressionException, ParseException {
		Element element = docsig.getDocumentElement();
		setNif(TbaiXMLUtils.getNodeTextContent(element,"Sujetos/Emisor/NIF"));
		setSerie(TbaiXMLUtils.getNodeTextContent(element,"Factura/CabeceraFactura/SerieFactura"));
		setNumfact(TbaiXMLUtils.getNodeTextContent(element,"Factura/CabeceraFactura/NumFactura"));
		setFecha(TbaiXMLUtils.getNodeTextContent(element,"Factura/CabeceraFactura/FechaExpedicionFactura"));
		setImporte(TbaiXMLUtils.getNodeTextContent(element,"Factura/DatosFactura/ImporteTotalFactura"));
		setFirma(TbaiXMLUtils.getNodeTextContent(element,"//ds:SignatureValue"));
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) throws ParseException {
	
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
	    Date fechaDate= formatter.parse(fecha);

	    DateFormat tbaidateFormat = new SimpleDateFormat("ddMMyy");  
	    this.fecha = tbaidateFormat.format(fechaDate);  
	    
	}
	public String getFirma() {
		return firma;
	}
	public void setFirma(String firma) {
		this.firma = firma;
	}
	public String getSerie() {
		return serie;
	}
	public void setSerie(String serie) {
		this.serie = serie;
	}
	public String getNumfact() {
		return numfact;
	}
	public void setNumfact(String numfact) {
		this.numfact = numfact;
	}
	public String getImporte() {
		return importe;
	}
	public void setImporte(String importe) {
		this.importe = importe;
	}
	
	public String getNif() {
		return nif;
	}
	public void setNif(String nif) {
		this.nif = nif;
	}
	
	public String getTbaiID() throws UnsupportedEncodingException {
		String firma13;
		if(null!=firma && firma.length()>13) {
			firma13=firma.substring(0, 13);
		}else {
			firma13 =this.firma;
		}
		
		String id="TBAI-"+this.nif+"-"+this.fecha+"-"+firma13+"-";
		String crc8=TbaiCRC8.calculate(id);
		id=id+crc8;
		return id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public void writeIDtoFile(String file) throws IOException {
		FileWriter writer = new FileWriter(file);
		String id=getTbaiID();
	    writer.write(id);
	    writer.write(System.lineSeparator());
	    writer.write(getUrlQR(id));
	    writer.close();
	}
	public void writetoFile(String file,Properties props) throws IOException {
		FileWriter writer = new FileWriter(file);
		
	    Set<String> keys = props.stringPropertyNames();
	    for (String key : keys) {
	    	writer.write(key + "=" + props.getProperty(key));
	    	writer.write(System.lineSeparator());
	    }
	    writer.close();
	}
	
	
	public String getUrlQR(String id) throws UnsupportedEncodingException {
		/*
		id – identificativo TicketBAI
		s – serie factura
		nf – número factura
		i – importe total de la factura
		cr – crc-8
		*/
		
		String urlqr = this.url + "?id=" + id + "&s=" + this.serie + "&nf=" + this.numfact + "&i=" + importe;
		String crc8qr = TbaiCRC8.calculate(urlqr);
		urlqr = urlqr + "&cr=" + crc8qr;
		return urlqr;
	}
	
	
	
}
