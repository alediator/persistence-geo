/*
 * CSVFileConnectorTest.java
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
package com.emergya.openfleetservices.importer.connector;

import java.io.File;
import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.emergya.openfleetservices.importer.data.DataSetDescriptor;
import com.emergya.persistenceGeo.dao.DataSetManager;

/**
 * Test for {@link DataSetDescriptor} use in {@link CSVfileConnector} and {@link DataSetManager}
 * creating a CSV by {@link CSVFileConnectorTest#TEST_ONE} file
 * 
 * @author <a href="mailto:adiaz@emergya.com">adiaz</a>
 *
 */
@ContextConfiguration(locations={"classpath:modelContext.xml"})
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
@Transactional
public class CSVFileConnectorTest extends AbstractJUnit4SpringContextTests{
	
	public static final String TEST_ONE = "target/test-classes/ficheros/csv/r:rl.csv";
	
	@Autowired
	private DataSetManager dataSetManager;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateTableFromCSV() {
		String pathname =  TEST_ONE;
		File f = new File(pathname);
		CSVfileConnector csvfile = new CSVfileConnector(f);
		DataSetDescriptor dsd = csvfile.getDescriptor();
		dataSetManager.createTable(dsd);
		Iterator<Object[]> it = csvfile.getIterator();
		while(it.hasNext()){
			dataSetManager.addData(dsd, it.next());
		}
		String address = "{poblacion},España";
		dataSetManager.geocode(dsd, address);
	}

}
