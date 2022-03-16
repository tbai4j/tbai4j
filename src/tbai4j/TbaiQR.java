package tbai4j;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class TbaiQR {
	private static final Logger logger = LoggerFactory.getLogger(TbaiQR.class);
	
	public static void generateQRcode(String text, String filePath) throws WriterException, IOException {
		generateQRcode(text, 150, 150, filePath);
	}
	public static void generateQRcode(String text, int width, int height, String filePath) throws WriterException, IOException{  
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
        logger.debug("QR Generado.Texto: {}  Path: {} ",text,filePath );
        
	} 		

}
