package com.nucleus.core.barcode.generator.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.general.util.documentgenerator.DocumentGeneratorException;
import com.nucleus.finnone.pro.general.util.documentgenerator.DocumentGeneratorMessageConstants;

@Named("barcodeGeneratorService")
public class BarcodeGeneratorServiceImpl implements BarcodeGeneratorService {

	private static final String IMAGE_TYPE = "image/x-png";
	private static final String NONE = "none";
	private static final String TOP = "top";
	private static final AtomicLong atomicSequence = new AtomicLong(1);
	private static final String PADDING_STRING = "00000000000000000000";

	@Value("${barcode.text.font.name}") 
	private String barcodeTextFontName; 

	@Value("${barcode.text.font.size}") 
	private double barcodeTextFontSize;

	@Value("${barcode.text.position}")
	private String barcodeTextPosition;
	
	@Value("${barcode.image.bar.height.mm}")
	private double barcodeImageBarHeight;
	
	@Value("${barcode.image.bar.module.width.mm}")
	private double barcodeImageBarModuleWidth;	

	@Value("${barcode.image.oriantation.angle}")
	private int barcodeImageOrientation; 
		
	@Value("${barcode.image.resolution.dpi}")
	private int barcodeImageDPI;
	
	@Value("${barcode.image.anti.alias.flag}")
	private boolean barcodeImageAntiAliasFlag;
	
	private HumanReadablePlacement humanReadablePlacement;

	@Override
	public byte[] createBarcodeImage(String barcodeRefrenceNumber) {
		ByteArrayOutputStream barcodeImageOutputStream = new ByteArrayOutputStream();
		try {
			Code128Bean bean = getCode128BeanInstance();

			BitmapCanvasProvider canvas = new BitmapCanvasProvider(barcodeImageOutputStream, IMAGE_TYPE,barcodeImageDPI, BufferedImage.TYPE_BYTE_BINARY, barcodeImageAntiAliasFlag, barcodeImageOrientation);
			bean.generateBarcode(canvas, barcodeRefrenceNumber);
			canvas.finish();
			
		} catch (IOException exception) {
			DocumentGeneratorException de = new DocumentGeneratorException("Barcode image generation failed.",
					exception, DocumentGeneratorMessageConstants.BARCODE_GENERATION_FAILED);
			List<Message> list = de.getMessages();
			list.add(new Message(DocumentGeneratorMessageConstants.BARCODE_GENERATION_FAILED,
					Message.MessageType.ERROR));
			de.setMessages(list);
			throw de;
		}

		return barcodeImageOutputStream.toByteArray();
	}

	private Code128Bean getCode128BeanInstance() {
		Code128Bean code128Bean = new Code128Bean();

		code128Bean.setFontName(barcodeTextFontName);
		code128Bean.setFontSize(barcodeTextFontSize);
		code128Bean.setHeight(barcodeImageBarHeight);
		code128Bean.setModuleWidth(barcodeImageBarModuleWidth);
		code128Bean.setMsgPosition(getHumanReadablePlacement());
		
		return code128Bean;
	}

	@Override
	public String getUniqueBarcodeReferenceNumber() {
		StringBuilder identifierStringBuilder = new StringBuilder(PADDING_STRING);
		
		long systemNanoSeconds = Math.abs(System.nanoTime());
		String uniqueIdentifier = identifierStringBuilder.append(String.valueOf(systemNanoSeconds)).append(atomicSequence.getAndIncrement()).toString();

		int uniqueIdentifierLength = uniqueIdentifier.length();
		uniqueIdentifier = uniqueIdentifier.substring(uniqueIdentifierLength-20,uniqueIdentifierLength);
			
		return uniqueIdentifier;
	}

	

	public HumanReadablePlacement getHumanReadablePlacement() {
		return humanReadablePlacement;
	}

	@PostConstruct
	public void initializeHumanReadablePlacement() {
		HumanReadablePlacement humanReadablePlacementInstance = null;
		
		switch (barcodeTextPosition) {
			case NONE:
				humanReadablePlacementInstance = HumanReadablePlacement.HRP_NONE;
				break;
			case TOP:
				humanReadablePlacementInstance = HumanReadablePlacement.HRP_TOP;
				break;
			default:
				humanReadablePlacementInstance = HumanReadablePlacement.HRP_BOTTOM;
				break;
		}
		
		this.humanReadablePlacement = humanReadablePlacementInstance;
		
	}
}
