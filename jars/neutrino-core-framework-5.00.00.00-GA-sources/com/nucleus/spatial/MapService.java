package com.nucleus.spatial;

import java.util.List;

public interface MapService {

	public Long saveGeodata(String latitude, String longitude);

	public Long addID(String latitude, String longitude);

	public Long savelongitude(String longitude);

	public String retriveGeoData(Long Id);

	public double retiveLatitude(Long Id);

	public double retiveLongitude(Long Id);

	public List<Long> nearbyXkm(int kmsFromCenter, Long IdOfCenter);

	public List<Long> nearbyXkm(int kmsFromCenter, String address);

	

	public String retrieveAPP_id(Long id);

	public List<String> retrievegeodata(Long country);

	public List<String> retrievegeodatabystate(Long state);

	public List<String> retrievegeodatabycity(Long city);

	public String retriveaddrssbycust(Long Id);

	public String retrivecustomernumbycust(Long Id);

	public String retrivecustomernameStringbycust(String num);

	

	public String PointinRectangle(double d, double e, double f, double g,
			double h, double i);

	public List<String> retriveminmaxlatlong();

	void saveboundingboundaries(String minmaxlatlong, String negativeName);
	public List<BoundingCoordinates> retriveminmaxlatlongandNegname();
	
	public String retriveareanamebylatlong(String id);
	

	void deleteboundingboundaries(Long id);
	public Boolean PointinPolygon(String xcoordinate[],String ycoordinate[],
			double h, double i);
	void editboundingboundaries(String Minmax,Long id);
}
