/**
 * 
 */
package com.nucleus.core.excelprocessor;


public class CellStyle {

	
	private short fontSize;
	private String fontName;
	private String fontStyle;
	private String fontColor;
	private short backgroundColor;
	private String alignment;
	public CellStyle() {
      super();
      this.fontSize = FileConstants.TWELVE;
      this.fontName = "Arial Black";
      this.fontStyle = null;
      this.fontColor = "Black";
      this.backgroundColor = 0;
      this.alignment = "center";
  }	
	public short getFontSize() {
		return fontSize;
	}
	public void setFontSize(short fontSize) {
		this.fontSize = fontSize;
	}
	public String getFontName() {
		return fontName;
	}
	public void setFontName(String fontName) {
		this.fontName = fontName;
	}
	public String getFontStyle() {
		return fontStyle;
	}
	public void setFontStyle(String fontStyle) {
		this.fontStyle = fontStyle;
	}
	public String getFontColor() {
		return fontColor;
	}
	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}
	public short getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(short backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	public String getAlignment() {
		return alignment;
	}
	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}
}
