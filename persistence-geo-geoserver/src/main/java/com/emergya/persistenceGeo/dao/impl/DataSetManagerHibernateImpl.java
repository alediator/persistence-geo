/*
 * DataSetManagerHibernateImpl.java
 * 
 * Copyright (C) 2012
 * 
 * This file is part of Proyecto persistenceGeo
 * 
 * This software is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 * As a special exception, if you link this library with other files to produce
 * an executable, this library does not by itself cause the resulting executable
 * to be covered by the GNU General Public License. This exception does not
 * however invalidate any other reasons why the executable file might be covered
 * by the GNU General Public License.
 * 
 * Authors:: Alejandro Díaz Torres (mailto:adiaz@emergya.com)
 */
package com.emergya.persistenceGeo.dao.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.emergya.openfleetservices.importer.connector.NominatimConnector;
import com.emergya.openfleetservices.importer.data.Column;
import com.emergya.openfleetservices.importer.data.DataSetDescriptor;
import com.emergya.openfleetservices.importer.ddbb.DataBaseType;
import com.emergya.persistenceGeo.dao.DataSetManager;

/**
 * Manager for {@link DataSetDescriptor} implementation transational
 * 
 * @author <a href="mailto:adiaz@emergya.com">adiaz</a>
 *
 */
@Repository
@Transactional
public class DataSetManagerHibernateImpl extends ExecuterSQLHibernateImpl
		implements DataSetManager {
	
	/**
	 * Not use for update
	 * @deprecated use {@link ExecuterSQLHibernateImpl} methods 
	 */
	protected JdbcTemplate simpleJdbcTemplate = null;

	@Autowired
	final public void setDataSource(final DataSource dataSource) {
		this.simpleJdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * Create a Table that suits the {@link DataSetDescriptor} given.
	 * 
	 * The tablename attribute of the datasetdescriptor is also updated.
	 * 
	 * @param dsd
	 * @return the table name
	 */
	public String createTable(DataSetDescriptor dsd) {
		String tableName = dsd.getTablename();
		String nameGeomColumn = dsd.getGeoColumnName();
		String pk = dsd.getNamePK();
		List<Column> columns = dsd.getFields();
		int srid = 4326;
		String columnsToTable = "";
		String sqlCreateTable = "CREATE TABLE ";
		Column geoColumn = null;
		for(Column c: columns){
			if(c.getName().equals(nameGeomColumn)){
				geoColumn = new Column();
				geoColumn.setName(c.getName());
				geoColumn.setType(c.getType());
				columns.remove(c);
			}
		}
		if(geoColumn == null){
			geoColumn = new Column();
			geoColumn.setName(nameGeomColumn);
			geoColumn.setType(DataBaseType.GEOMETRY);
		}
		Iterator<Column> it = columns.iterator();
		Column c = (Column)it.next();
		columnsToTable += c.getName() + " " + c.getType();
		while(it.hasNext()){
			c = (Column)it.next();
			columnsToTable += ", " + c.getName() + " " + c.getType();
		}
		sqlCreateTable = sqlCreateTable + tableName + 
				" (" + pk + " SERIAL PRIMARY KEY, " +
				columnsToTable + ")";
		this.execute(sqlCreateTable);
		
		// Add the geometry field into DB
		String sqlGeometry = "Select AddGeometryColumn ('" + tableName
				+ "', '" + geoColumn.getName() 
				+ "', " + String.valueOf(srid)
				+ ", '" + geoColumn.getType()
				+ "', " + String.valueOf(2) + ")";
		
		this.execute(sqlGeometry);
		
		return tableName;
	}

	/**
	 * Add one row to the table
	 * 
	 * @param dsd
	 * @param it
	 */
	public void addData(DataSetDescriptor dsd, Object[] it) {
		
		String sqlInsert = "INSERT INTO ";
		String tableName = dsd.getTablename();
		List<Column> fields = dsd.getFields();
		
		sqlInsert += tableName + "(" + fields.get(0).getName();
		for(int i=1; i<fields.size(); i++){
			sqlInsert += ", " + fields.get(i).getName();
		}
		sqlInsert += ") VALUES ('" + it[0] + "'";
		
		for(int j=1; j<it.length; j++){
			sqlInsert += ", '" + it[j] + "'";
		}
		sqlInsert += ")";
		
		this.execute(sqlInsert);
	}

	/**
	 * Add multiple rows to the table (usually calling
	 * {@link #addData(DataSetDescriptor, Object[])}
	 * 
	 * @param dsd
	 * @param it
	 */
	public void addAllData(DataSetDescriptor dsd, Iterator<Object[]> it) {
		while (it.hasNext()) {
			this.addData(dsd, it.next());
		}
	}

	/**
	 * Given a tablename and the column where the address lies, for each row, it
	 * geocodes the address and saves the geometry on the geocolumn
	 * 
	 * @param dsd
	 * @return
	 */
	public int geocode(DataSetDescriptor dsd, String address) {
		NominatimConnector nm = new NominatimConnector("http://nominatim.openstreetmap.org/search.php");
		nm.setFormat("json");
		boolean containColumn = address.contains("{");
		int rowCont = 0;
		if(!containColumn){
			// In order to not contain a column name into the search, the geometry field is the same for all
			nm.setQuery(address);
			String geoColumnAddress = nm.getAddress();
			System.out.println(geoColumnAddress);
			// Update all fields with geoColumnAddress and geoColumnName
			this.updateGeometryColumn(dsd, geoColumnAddress);
		}else{
			// Get the columns from address
			List<String> col = new LinkedList<String>();
			nm.getColumnsToGeom(col, address);
			SqlRowSet columnsMap = this.getColumnsByList(dsd, col);
			while(columnsMap.next()){
				for(String c:col){
					String dir = address;
					String param = columnsMap.getString(c);
					int pk = columnsMap.getInt(dsd.getNamePK());
					dir = dir.replace("{" + c + "}", param);
					nm.setQuery(dir);
					String geoAddress = nm.getAddress();
					if(geoAddress!=null){
						String[] splitGeom = geoAddress.split(",");
						Double lon = Double.valueOf(splitGeom[0]);
						Double lat = Double.valueOf(splitGeom[1]);
						String updateSQL = "UPDATE " + dsd.getTablename() +
								" SET " + dsd.getGeoColumnName() + " = ST_GeomFromEWKT(" + MARK_KEY + GEOM_KEY + ")" +
								" WHERE " + dsd.getNamePK() + "="+ MARK_KEY + PK_KEY;
						Map<String, String> paramMap = new HashMap<String, String>();
						paramMap.put(PK_KEY, new Integer(pk).toString());
						paramMap.put(GEOM_KEY, "SRID=4326;POINT("+lon+" "+lat+")");
						String toExecuteSQL = getSQLToExecute(updateSQL, paramMap);
						this.execute(toExecuteSQL);
					}else{
						rowCont++;
					}
				}
			}
		}
		return rowCont;
	}
	
	/**
	 * Mark a key to be mapped in a SQL query
	 */
	protected static final String MARK_KEY = ":";
	
	/**
	 * GEOM parameter for a geometry
	 */
	protected static final String GEOM_KEY = "geom";
	
	/**
	 * KEY parameter for pk index
	 */
	protected static final String PK_KEY = "geom";
	
	/**
	 * Obtain sql sentence to execute
	 * 
	 * @param previus
	 * @param parameterMap
	 * 
	 * @return previus parsed with parameterMap
	 */
	protected static String getSQLToExecute(String previus, Map<String, String> parameterMap){
		String toExecuteSQL = new String(previus);
		for(String key: parameterMap.keySet()){
			toExecuteSQL = toExecuteSQL.replace(MARK_KEY + key, parameterMap.get(key));
		}
		return toExecuteSQL;
	}
	
	public SqlRowSet getColumnsByList(DataSetDescriptor dsd, List<String> col){
		String sql = "SELECT pk, ";
		for(String s: col){
			if(!s.equals(col.get(col.size()-1))){
				sql += s + ",";
			}else{
				sql += s;
			}
		}
		sql += " FROM " + dsd.getTablename();
		return this.simpleJdbcTemplate.queryForRowSet(sql);
	}

	public void updateGeometryColumn(DataSetDescriptor dsd, String geoColumnAddress){
		String geoColumnName = dsd.getGeoColumnName();
		String tableName = dsd.getTablename();
		String splitGeom[] = geoColumnAddress.split(",");
		Double lon = Double.valueOf(splitGeom[0]);
		Double lat = Double.valueOf(splitGeom[1]);
		String updateSQL = "UPDATE " + tableName +
				" SET " + geoColumnName + " = ST_GeomFromEWKT(\'SRID=4326;POINT(" + lon + " " + lat + ")\')";
		this.execute(updateSQL);
	}

}
