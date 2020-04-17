package com.nucleus.spatial;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.persistence.EntityDaoImpl;
import com.nucleus.service.BaseServiceImpl;

@Named("mapService")
@Transactional
public class MapServiceImpl extends BaseServiceImpl implements MapService {

                @Inject
                @Named("entityDao")
                private EntityDaoImpl entityDao;

                public final String QUERY_GET_GEODATA_FROM_GEODATAID = "address.findGeocode";
                public final String QUERY_GET_LATITUDE_FROM_GEODATAID = "address.findGeocodeLatitude";
                public final String QUERY_GET_LONGITUDE_FROM_GEODATAID = "address.findGeocodeLogitude";
                

                public final String QUERY_GET_aaddID_FROM_GEODATAID = "address.findidbylatitude&longitude";
                public final String QUERY_GET_Geodata_FROM_country = "address.findidlatitudelongitudebycountry";
                public final String QUERY_GET_Geodata_FROM_state = "address.findidlatitudelongitudebystate";
                public final String QUERY_GET_Geodata_FROM_CITY = "address.findidlatitudelongitudebycity";
                public final String QUERY_GET_addbyCustname = "address.findbyCustomerName";
                public final String QUERY_GET_custnumbyid = "address.findCustomernumberbyid";
                public final String QUERY_GET_custnamebyid = "address.findCustomernamebyid";
                public final String QUERY_GET_MINMAXLATLONG= "address.getminmaxlatlong";
                public final String QUERY_GET_MINMAXLATLONGandNEGNAME= "address.getminmaxlatlongandNegName";
                public final String QUERY_GET_AREANAMEbyLATLONG= "address.getnamebylatlong";

                @Override
                public Long saveGeodata(String latitude, String longitude) {
                                NeutrinoValidator.notNull(latitude, "latitude cannot be null");
                                NeutrinoValidator.notNull(longitude, "longitude cannot be null");
                                GeoData geoLatLon = new GeoData();
                                geoLatLon.setLatitude(Double.valueOf(latitude).doubleValue());
                                geoLatLon.setLongitude(Double.valueOf(longitude).doubleValue());
                                entityDao.persist(geoLatLon);
                                return geoLatLon.getId();
                }
                
                @Override
                public void saveboundingboundaries(String minmaxlatlong,String negativeName) {
                                
                                NeutrinoValidator.notNull(minmaxlatlong, "minmaxlatlongitude cannot be null");
                               
                                BoundingCoordinates boundingcoordinates = new BoundingCoordinates();
                                boundingcoordinates.setMinmaxlatlonglatitude(minmaxlatlong);
                                boundingcoordinates.setNegativeName(negativeName);
                                
                                entityDao.persist(boundingcoordinates);
                                
                }

                @Override
                public double retiveLatitude(Long id) {
                                NeutrinoValidator.notNull(id, "Id cannot be null");
                                NamedQueryExecutor<Double> executor = new NamedQueryExecutor<Double>(
                                                                QUERY_GET_LATITUDE_FROM_GEODATAID).addParameter("id", id);
                                return entityDao.executeQueryForSingleValue(executor);

                }

                @Override
                public double retiveLongitude(Long id) {
                                NeutrinoValidator.notNull(id, "Id cannot be null");
                                NamedQueryExecutor<Double> executor = new NamedQueryExecutor<Double>(
                                                                QUERY_GET_LONGITUDE_FROM_GEODATAID).addParameter("id", id);
                                return entityDao.executeQueryForSingleValue(executor);
                }

                @Override
                public List<Long> nearbyXkm(int kmsFromCenter, Long IdOfCenter) {
                                // TODO Auto-generated method stub

                                return null;
                }

                @Override
                public String retriveGeoData(Long id) {
                                NeutrinoValidator.notNull(id, "Id cannot be null");
                                NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>(
                                                                QUERY_GET_GEODATA_FROM_GEODATAID).addParameter("id", id);
                                return entityDao.executeQueryForSingleValue(executor);
                }

                @Override
                public List<Long> nearbyXkm(int kmsFromCenter, String address) {
                                // TODO Auto-generated method stub
                                return null;
                }

                

                @Override
                public List<String> retrievegeodata(Long country) {
                                NeutrinoValidator.notNull(country, "Id cannot be null");
                                NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>(
                                                                QUERY_GET_Geodata_FROM_country).addParameter("countryId",
                                                                country);
                                return entityDao.executeQuery(executor);

                }

                @Override
                public Long addID(String latitude, String longitude) {
                                // TODO Auto-generated method stub
                                return null;
                }

                @Override
                public Long savelongitude(String longitude) {
                                // TODO Auto-generated method stub
                                return null;
                }

                @Override
                public String retrieveAPP_id(Long id) {
                                // TODO Auto-generated method stub
                                return null;
                }

                @Override
                public List<String> retrievegeodatabystate(Long state) {
                                NeutrinoValidator.notNull(state, "Id cannot be null");
                                NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>(
                                                                QUERY_GET_Geodata_FROM_state).addParameter("stateId", state);
                                return entityDao.executeQuery(executor);

                }

                @Override
                public List<String> retrievegeodatabycity(Long city) {
                                NeutrinoValidator.notNull(city, "Id cannot be null");
                                NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>(
                                                                QUERY_GET_Geodata_FROM_CITY).addParameter("cityId", city);
                                return entityDao.executeQuery(executor);

                }

                @Override
                public String retriveaddrssbycust(Long id) {/*
                                NeutrinoValidator.notNull(id, "Id cannot be null");
                                NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>(
                                                                QUERY_GET_addbyCustname).addParameter("id", id);
                                List<String> lati_longi_list = (List<String>) entityDao
                                                                .executeQuery(executor);
                                if (lati_longi_list != null && !lati_longi_list.isEmpty()) {
                                                return lati_longi_list.get(0);
                                } else {
                                                return null;
                                }

                */
                	return "";
                }

                @Override
                public String retrivecustomernumbycust(Long id) {
                             
                	return "";
                }

                @Override
                public String retrivecustomernameStringbycust(String num) {
                	return "";
                }
                @Override
                public List<String> retriveminmaxlatlong() {
                                
                                NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>(
                                                                QUERY_GET_MINMAXLATLONG);
                                return entityDao.executeQuery(executor);
                } 
                
                @Override
                public String PointinRectangle(double d,double e,double f,double g,double h,double i) {
                                
                                if((d<h)&&(f<i)&&(e>h)&&(g>i)){
                                                return "TRUE";
                                }
                                else
                                                return "FALSE";
                                

                                // TODO Auto-generated method stub
                                
                                
                }

				@Override
				public List<BoundingCoordinates> retriveminmaxlatlongandNegname() {
					NamedQueryExecutor<BoundingCoordinates> executor = new NamedQueryExecutor<BoundingCoordinates>(
							QUERY_GET_MINMAXLATLONGandNEGNAME);
                   List<BoundingCoordinates> results = entityDao.executeQuery(executor);
                   return results;
					// TODO Auto-generated method stub
					
				}

				@Override
				public String retriveareanamebylatlong(String minmaxlatlonglatitude) {
					// TODO Auto-generated method stub
					 NeutrinoValidator.notNull(minmaxlatlonglatitude, "Id cannot be null");
                     NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>(
                                                     QUERY_GET_custnumbyid).addParameter("minmaxlatlonglatitude", minmaxlatlonglatitude);
                     return entityDao.executeQueryForSingleValue(executor);
				}

				@Override
				public void deleteboundingboundaries(Long id) {
					// TODO Auto-generated method stub
					
                    BoundingCoordinates boundingcoordinates = new BoundingCoordinates();
                    boundingcoordinates = entityDao.find(BoundingCoordinates.class,id);
                    
                    entityDao.delete(boundingcoordinates);
                    
					
				}

				@Override
				public Boolean PointinPolygon(String[]  xcoordinate ,
						String[] ycoordinate, double x, double y) {
					int i1; int j=(xcoordinate.length)-1 ;
					  Boolean  oddNodes=false      ;

					  for (i1=0; i1<(xcoordinate.length); i1++) {
					    if (((Double.valueOf(ycoordinate[i1]).doubleValue())< y && (Double.valueOf(ycoordinate[j]).doubleValue())>=y
					    ||   (Double.valueOf(ycoordinate[j]).doubleValue())< y && (Double.valueOf(ycoordinate[i1]).doubleValue())>=y)
					    &&  ((Double.valueOf(xcoordinate[i1]).doubleValue())<=x || (Double.valueOf(xcoordinate[j]).doubleValue())<=x)) {
					      oddNodes^=((Double.valueOf(xcoordinate[i1]).doubleValue())+(y-(Double.valueOf(ycoordinate[i1]).doubleValue()))/((Double.valueOf(ycoordinate[j]).doubleValue())-(Double.valueOf(ycoordinate[i1]).doubleValue()))*((Double.valueOf(xcoordinate[j]).doubleValue())-(Double.valueOf(xcoordinate[i1]).doubleValue()))<x); }
					    j=i1; }

					  return oddNodes; 
					
					
				}

				@Override
				public void editboundingboundaries(String Minmax, Long id) {
					   BoundingCoordinates boundingcoordinates = entityDao.find(BoundingCoordinates.class,id);
	                    boundingcoordinates.setMinmaxlatlonglatitude(Minmax);
	                  /*if(boundingcoordinates.getId()==null)
	                    entityDao.persist(boundingcoordinates);
	                  else*/
	                	  entityDao.update(boundingcoordinates);
					
				}

				
}
