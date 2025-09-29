package com.workflow.textExtraction;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.lingala.zip4j.ZipFile;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.LoadLibs;

public class TextExtraction {

	public static void main(String[] args) throws Throwable {
		//String filePath = "sample/72789016490-Losartan Potassium.pdf";
		//String filePath = "sample/01 Friday Registration.pdf";
		//String filePath = "sample/Michel Turgeon EF FR test file 2.pdf";
		//String filePath = "sample/Alto Townhomes - Deed of Trust (Assembled).pdf";
		//String filePath = "sample/S2067-Completed.pdf";
		//String filePath = "sample/00622.04.01-abcbs-authorization-org-det-request-v4 - Completed 1.pdf";
		//String filePath = "sample/S2067-Completed_FULL_TEXT.pdf";
		String filePath = "sample/S2067-Completed_LOCKED_FORM_FIELD_TEXT_ONLY.pdf";
		//String filePath = "sample/S2067-Completed_IMAGE.pdf";
		//String filePath = "sample/S2067-Completed.tif";
		//String filePath = "sample/file-sample_100kB.doc";
		//String filePath = "sample/file-sample_100kB.docx";
		//String filePath = "sample/file_example_XLS_1000.xls";
		//String filePath = "sample/file_example_XLSX_1000.xlsx";

		String method = "pdfExtractionFilePath";
		//String method = "pdfExtractionB64";
		Boolean appendExtractedFields = true;
		//String method = "ocrPdf";
		//String method = "tessPDF"; // This is for dev environment only as it requires File input (not b64) 
		//String method = "ocrImage";
		//String method = "pdfToImageOcrArray";
		//String method = "pdfPageCount";
		//String method = "tika";

		System.out.println("start time: " + new Date());
		if(method.equals("pdfExtractionFilePath")) {
			System.out.println(extractPdfTextFromFilePath(filePath, appendExtractedFields));
		} else if(method.equals("pdfExtractionB64")) {
			File file = new File(filePath);
			byte[] fileContent = Files.readAllBytes(file.toPath());
			System.out.println(extractPdfTextFromB64(Base64.getEncoder().encodeToString(fileContent), appendExtractedFields));
		} else if(method.equals("ocrPdf")) {
			System.out.println(ocrPdfFromB64(Base64.getEncoder().encodeToString(Files.readAllBytes(new File(filePath).toPath()))));
		} else if(method.equals("tessPDF")) {
			System.out.println(ocrFile_tesseract(new File(filePath)));
		} else if(method.equals("ocrImage")) {
			System.out.println(ocrImageFromB64(Base64.getEncoder().encodeToString(Files.readAllBytes(new File(filePath).toPath())), "tif"));
		} else if(method.equals("tika")) {
			System.out.println(extractTextFromB64_Tika(Base64.getEncoder().encodeToString(Files.readAllBytes(new File(filePath).toPath()))));
		} else if(method.equals("pdfPageCount")) {
			System.out.println(getPdfPageCount(Base64.getEncoder().encodeToString(Files.readAllBytes(new File(filePath).toPath()))));
		} else if(method.equals("pdfToImageOcrArray")) {
			System.out.println(convertPdfToB64ImageOcrArray(Base64.getEncoder().encodeToString(Files.readAllBytes(new File(filePath).toPath()))));
		} 
		
		System.out.println("end time: " + new Date());

	}

	public static String extractTextFromB64_Tika(String b64Content) throws Throwable {
		InputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(b64Content));
		Tika tika = new Tika();
		
/*
		String classpath = System.getProperty("java.class.path");
		String[] classpathEntries = classpath.split(File.pathSeparator);
		for(String entry : classpathEntries)
			System.out.println("java.class.path entry: [" + entry + "]");
*/
		//TikaConfig tikaConfig = new TikaConfig("tikaConfig.xml");
/*
		String tikaConfigB64 = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4NCjxwcm9wZXJ0aWVzPg0KPCEtLSAgPHBhcnNlcnM+DQogICAgPHBhcnNlciBjbGFzcz0ib3JnLmFwYWNoZS50aWthLnBhcnNlci5EZWZhdWx0UGFyc2VyIj4NCiAgICAgIDxtaW1lPmFwcGxpY2F0aW9uL3gtdGlrYS1tc29mZmljZTwvbWltZT4NCgkgIDxtaW1lPmFwcGxpY2F0aW9uL21zd29yZDwvbWltZT4NCiAgICAgIDxwYXJzZXItZXhjbHVkZSBjbGFzcz0ib3JnLmFwYWNoZS50aWthLnBhcnNlci5FbXB0eVBhcnNlciIvPg0KICAgIDwvcGFyc2VyPg0KICAgIDxwYXJzZXIgY2xhc3M9Im9yZy5hcGFjaGUudGlrYS5wYXJzZXIuRW1wdHlQYXJzZXIiPg0KICAgICAgPG1pbWUtZXhjbHVkZT5hcHBsaWNhdGlvbi94LXRpa2EtbXNvZmZpY2U8L21pbWUtZXhjbHVkZT4NCgkgIDxtaW1lLWV4Y2x1ZGU+YXBwbGljYXRpb24vbXN3b3JkPC9taW1lLWV4Y2x1ZGU+DQogICAgPC9wYXJzZXI+LS0+DQogICAgPCEtLSA8cGFyc2VyIGNsYXNzPSJvcmcuYXBhY2hlLnRpa2EucGFyc2VyLm1pY3Jvc29mdC54bWwuV29yZE1MUGFyc2VyIj4gDQoJPHBhcnNlciBjbGFzcz0ib3JnLmFwYWNoZS50aWthLnBhcnNlci5taWNyb3NvZnQub294bWwueHdwZi5tbDIwMDYuV29yZDIwMDZNTFBhcnNlciI+DQogICAgICA8bWltZT5hcHBsaWNhdGlvbi94LXRpa2EtbXNvZmZpY2U8L21pbWU+DQoJICA8bWltZT5hcHBsaWNhdGlvbi9tc3dvcmQ8L21pbWU+DQogICAgPC9wYXJzZXI+DQogIDwvcGFyc2Vycz4tLT4NCiAgPHNlcnZpY2UtbG9hZGVyIGR5bmFtaWM9InRydWUiLz4NCiAgPHNlcnZpY2UtbG9hZGVyIGxvYWRFcnJvckhhbmRsZXI9IlRIUk9XIi8+DQo8L3Byb3BlcnRpZXM+";
		InputStream tikaConfigStream = new ByteArrayInputStream(Base64.getDecoder().decode(tikaConfigB64));
		TikaConfig tikaConfig = new TikaConfig(tikaConfigStream);

		System.out.println("tikaConfig.getServiceLoader().isDynamic(): " + tikaConfig.getServiceLoader().isDynamic());
		System.out.println("tikaConfig.getServiceLoader().LoadErrorHandler(): " + tikaConfig.getServiceLoader().getLoadErrorHandler());
		
		TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
		Tika tika = new Tika(tikaConfig);
		Metadata metadata = new Metadata();
		System.out.println("Tika version: [" + tika.toString() + "]");
		System.out.println("tika.detect(stream): [" + tika.detect(stream) + "]");
/*		
/*
		MediaTypeRegistry registry = tikaConfig.getMediaTypeRegistry();
		for(MediaType type : registry.getTypes()) {
			System.out.println("Tika MediaTypes: [" + type.toString() + "]");
		}

		CompositeParser parser = (CompositeParser) tikaConfig.getParser();
		for(Parser p : parser.getAllComponentParsers()) {
			System.out.println("Tika Parser: [" + p.getClass().getName() + "]");
		}
*/

		String extractedText = tika.parseToString(stream);
/*		
		String extractedText = tika.parseToString(stream, metadata); 
		System.out.println("extractedText length: " + extractedText.length());  

	    for (String s: metadata.names()) {
	        System.out.println(s + ": " + metadata.get(s));
	    }
*/		
		return extractedText;
	}
	
	public static String extractPdfTextFromFilePath(String filePath, Boolean appendExtractedFields) throws Throwable {
		File file = new File(filePath);
		byte[] fileContent = Files.readAllBytes(file.toPath());
		return extractPdfText_pdfbox(Base64.getEncoder().encodeToString(fileContent), appendExtractedFields);
	}

	public static String extractPdfTextFromB64(String b64Content, Boolean appendExtractedFields) throws Throwable {
		return extractPdfText_pdfbox(b64Content, appendExtractedFields);
	}

	public static String ocrPdfFromB64(String b64Content) throws Throwable {
		BufferedImage[] images = convertPdfToImages(b64Content);
		return ocrImages_tesseract(images);
	}

	public static String ocrImageFromB64(String b64Content, String imageExtension) throws Throwable {
		byte[] bytes = Base64.getDecoder().decode(b64Content);

		// If this is a tiff image, check for multi-page tiff
		if(imageExtension.equals("tif") || imageExtension.equals("tiff")) {
			ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
			Iterator<ImageReader> imageReadersIterator = ImageIO.getImageReadersBySuffix(imageExtension);
			if(imageReadersIterator.hasNext()) {
				ImageReader reader = imageReadersIterator.next();
				reader.setInput(iis);
				int numPages = reader.getNumImages(true);
				BufferedImage[] images = new BufferedImage[numPages];
				for(int i=0; i<numPages; i++) {
					BufferedImage image = reader.read(i);
					images[i] = image;
				}
				return ocrImages_tesseract(images);
			} else {
				return null;
			}
		} else {
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
			BufferedImage[] images = new BufferedImage[1];
			images[0] = image;
			return ocrImages_tesseract(images);
		}
	}
	
	public static void unzip(String zipFilePath, String unzipFolderPath) throws Throwable {
		ZipFile zipFile = new ZipFile(zipFilePath);
		zipFile.extractAll(unzipFolderPath);
		zipFile.close();
	}
	
	public static Boolean fileExists(String filePath) throws Throwable {
		File file = new File(filePath);
		return file.exists();
	}

	public static void deleteFile(String filePath) throws Throwable {
		File file = new File(filePath);
		file.delete();
	}

	public static int getPdfPageCount(String b64Content) throws Throwable {
		byte[] bytes = Base64.getDecoder().decode(b64Content);
		PDDocument document = Loader.loadPDF(bytes);
        return document.getNumberOfPages();
	}

	public static String convertPdfToB64ImageOcrArray(String b64Content) throws Throwable {
		byte[] bytes = Base64.getDecoder().decode(b64Content);
		PDDocument document = Loader.loadPDF(bytes);
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		int pageCount = document.getNumberOfPages();
		ImageType imageType = ImageType.BINARY; // Specify black and white
		BufferedImage[] images = new BufferedImage[pageCount];
	
		for(int page=0; page<pageCount; page++) {
	        BufferedImage image = pdfRenderer.renderImageWithDPI(page, Integer.parseInt("300"), imageType);
	        images[page] = image;
	    }
		return ocrImagesToArray_tesseract(images);
	}
	
	private static File convertPdfToTiffFile(String b64Content) throws Throwable {
		byte[] bytes = Base64.getDecoder().decode(b64Content);
		PDDocument document = Loader.loadPDF(bytes);
		
		File outputTiff = new File("sample/S2067-Completed.tif");
		OutputStream os = new FileOutputStream(outputTiff);
		ImageOutputStream ios = ImageIO.createImageOutputStream(os);

		ImageWriter writer = ImageIO.getImageWritersByFormatName("TIFF").next();
		ImageWriteParam params = writer.getDefaultWriteParam();
		params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		params.setCompressionType("LZW");
		params.setCompressionQuality(1.0f);
		writer.setOutput(ios);
		writer.prepareWriteSequence(null);

		PDFRenderer pdfRenderer = new PDFRenderer(document);
		ImageType imageType = ImageType.BINARY; // Specify black and white

        for(int page=0; page<document.getNumberOfPages(); page++) {
            BufferedImage image = pdfRenderer.renderImageWithDPI(page, Integer.parseInt("300"), imageType);
            IIOMetadata metadata = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), params);
            writer.writeToSequence(new IIOImage(image, null, metadata), params);
        }

        ios.flush();
        ios.close();
        os.flush();
        os.close();
        writer.dispose();
        
        return outputTiff;
	}

	private static BufferedImage[] convertPdfToImages(String b64Content) throws Throwable {
		byte[] bytes = Base64.getDecoder().decode(b64Content);
		PDDocument document = Loader.loadPDF(bytes);
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		int pageCount = document.getNumberOfPages();
		ImageType imageType = ImageType.BINARY; // Specify black and white
		BufferedImage[] images = new BufferedImage[pageCount];

		for(int page=0; page<pageCount; page++) {
            BufferedImage image = pdfRenderer.renderImageWithDPI(page, Integer.parseInt("300"), imageType);
            images[page] = image;
        }
		
        return images;
	}
	
	private static String ocrFile_tesseract(File file) throws Throwable {
		String tessdataFolderPath = LoadLibs.extractTessResources("tessdata").getPath();
		Tesseract tesseract = new Tesseract();
		tesseract.setDatapath(tessdataFolderPath);
		return tesseract.doOCR(file);
	}

	public static String getTess4jFolderPath() throws Throwable {
		File tess4jFile = LoadLibs.extractTessResources("");
		return tess4jFile.getPath();
	}
	
	private static String ocrImagesToArray_tesseract(BufferedImage[] images) throws Throwable {
		System.out.println("ocrImages_tesseract()");
		System.out.println("java.class.path: " + System.getProperty("java.class.path"));
		System.out.println("LoadLibs.LIB_NAME: " + LoadLibs.LIB_NAME);
		System.out.println("LoadLibs.LIB_NAME_NON_WIN: " + LoadLibs.LIB_NAME_NON_WIN);
		System.out.println("LoadLibs.TESS4J_TEMP_DIR: " + LoadLibs.TESS4J_TEMP_DIR);

		String tessdataFolderPath = LoadLibs.extractTessResources("tessdata").getPath();
		System.out.println("tessdataFolderPath: " + tessdataFolderPath);

		Tesseract tesseract = new Tesseract();
		tesseract.setDatapath(tessdataFolderPath);
		
		JsonArray jsonArray = new JsonArray();
		if(images != null && images.length > 0) {
			for(int i=0; i<images.length; i++) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageIO.write(images[i], "jpg", os);
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("imageB64", Base64.getEncoder().encodeToString(os.toByteArray()));
				jsonObject.addProperty("imageOcrText", tesseract.doOCR(images[i]));
				jsonArray.add(jsonObject);
			}
		}
		JsonObject output = new JsonObject();
		output.add("images", jsonArray);
		Gson gson = new GsonBuilder().disableHtmlEscaping().create(); // Must do this, otherwise = get converted to "\u003d" which is invalid Base64
		return gson.toJson(output);
	}
	
	private static String ocrImages_tesseract(BufferedImage[] images) throws Throwable {
		System.out.println("ocrImages_tesseract()");
		System.out.println("java.class.path: " + System.getProperty("java.class.path"));
		System.out.println("LoadLibs.LIB_NAME: " + LoadLibs.LIB_NAME);
		System.out.println("LoadLibs.LIB_NAME_NON_WIN: " + LoadLibs.LIB_NAME_NON_WIN);
		System.out.println("LoadLibs.TESS4J_TEMP_DIR: " + LoadLibs.TESS4J_TEMP_DIR);

		String tessdataFolderPath = LoadLibs.extractTessResources("tessdata").getPath();
		System.out.println("tessdataFolderPath: " + tessdataFolderPath);

		Tesseract tesseract = new Tesseract();
		tesseract.setDatapath(tessdataFolderPath);
		
		String ocrText = "";
		if(images != null && images.length > 0) {
			for(int i=0; i<images.length; i++) {
				ocrText += tesseract.doOCR(images[i]); 
			}
		}
		return ocrText;
	}
	
	private static String flattenPdf_pdfbox(String b64Content) throws Throwable {
		byte[] bytes = Base64.getDecoder().decode(b64Content);
		PDDocument document = Loader.loadPDF(bytes);

		PDDocumentCatalog catalog = document.getDocumentCatalog();
		PDAcroForm form = catalog.getAcroForm();
/*
		List<PDField> fields = form.getFields();
		for(PDField field: fields) {
			field.setReadOnly(true);
			Object value = field.getValueAsString();
			String name = field.getFullyQualifiedName();
			System.out.print(name);
			System.out.print(" = ");
			System.out.print(value);
			System.out.println();
		}
*/		
		form.flatten();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		document.setAllSecurityToBeRemoved(true);
		document.save(baos);
		byte[] flattenedBytes = baos.toByteArray();
		return Base64.getEncoder().encodeToString(flattenedBytes);
	}
	
	private static String extractPdfText_pdfbox(String b64Content, Boolean appendExtractedFields) throws Throwable {
		byte[] bytes = Base64.getDecoder().decode(b64Content);
		PDDocument document = Loader.loadPDF(bytes);
		
		//System.out.println("isReadOnly: " + document.getCurrentAccessPermission().isReadOnly());
		//System.out.println("isAllSecurityToBeRemoved: " + document.isAllSecurityToBeRemoved());
		//document.setAllSecurityToBeRemoved(removeAllSecurity);

		// Initialize string to capture extracted fields - if they exist - which will be appended to end of PDF text later
		String extractedFields = "";
		
		// Determine if form and fields exist and PDF needs to be flattened
		PDDocumentCatalog catalog = document.getDocumentCatalog();
		PDAcroForm form = catalog.getAcroForm();
		
		// If the form exists, check if fields exist, and if so capture the value to append later and then flatten document to include fields in text output 
		if(form != null) {
			List<PDField> fields = form.getFields();
			if(fields != null && fields.size() > 0) {
				for(PDField field: fields) {
					extractedFields += field.getFullyQualifiedName() + ": " + field.getValueAsString() + "\r\n"; 
				}
				String b64ContentFlattened = flattenPdf_pdfbox(b64Content);
				byte[] documentBytesFlattened = Base64.getDecoder().decode(b64ContentFlattened);
				document = Loader.loadPDF(documentBytesFlattened);
			}
		}
		PDFTextStripper pdfStripper = new PDFTextStripper();
		pdfStripper.setSortByPosition(true);
		String pdfText = pdfStripper.getText(document); 

		// If fields were found and extracted previously, append them to the PDF text  
		if(appendExtractedFields && !extractedFields.equals("")) {
			pdfText += extractedFields;
		}
		
		return pdfText;
	}
}
