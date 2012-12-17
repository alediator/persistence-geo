/*
 * DataSetManager.java
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
package com.emergya.persistenceGeo.dao;

import java.util.Iterator;
import java.util.List;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.emergya.openfleetservices.importer.data.DataSetDescriptor;

/**
 * Manager for {@link DataSetDescriptor}
 * 
 * @author <a href="mailto:adiaz@emergya.com">adiaz</a>
 *
 */
public interface DataSetManager extends ExecuterSQL {

	/**
	 * Create a Table that suits the {@link DataSetDescriptor} given.
	 * 
	 * The tablename attribute of the datasetdescriptor is also updated.
	 * 
	 * @param dsd
	 * @return the table name
	 */
	public String createTable(DataSetDescriptor dsd);

	/**
	 * Add one row to the table
	 * 
	 * @param dsd
	 * @param it
	 */
	public void addData(DataSetDescriptor dsd, Object[] it);

	/**
	 * Add multiple rows to the table (usually calling
	 * {@link #addData(DataSetDescriptor, Object[])}
	 * 
	 * @param dsd
	 * @param it
	 */
	public void addAllData(DataSetDescriptor dsd, Iterator<Object[]> it);

	/**
	 * Given a tablename and the column where the address lies, for each row, it
	 * geocodes the address and saves the geometry on the geocolumn
	 * 
	 * @param dsd
	 * @return
	 */
	public int geocode(DataSetDescriptor dsd, String address);
	
	/**
	 * Get colums by List
	 * 
	 * @param dsd
	 * @param col
	 * @return
	 */
	public SqlRowSet getColumnsByList(DataSetDescriptor dsd, List<String> col);

	/**
	 * Update geometry column
	 * 
	 * @param dsd
	 * @param geoColumnAddress
	 */
	public void updateGeometryColumn(DataSetDescriptor dsd, String geoColumnAddress);

}
