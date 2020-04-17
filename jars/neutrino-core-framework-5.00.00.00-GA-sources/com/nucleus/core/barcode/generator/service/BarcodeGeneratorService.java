package com.nucleus.core.barcode.generator.service;

public interface BarcodeGeneratorService {

		public byte[] createBarcodeImage(String barcodeReferenceNumber);
		
		public String getUniqueBarcodeReferenceNumber();
		
				
}
