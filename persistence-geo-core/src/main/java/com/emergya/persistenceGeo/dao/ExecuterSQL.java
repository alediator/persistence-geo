/*
 * ExecuterSQL.java
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

import java.util.List;

/**
 * Interfaz para la ejecution de sentencias sql en database
 * 
 * @author <a href="mailto:adiaz@emergya.com">adiaz</a>
 *
 */
public interface ExecuterSQL {
	
	/**
	 * Ejecuta el sql pasado como argumento
	 */
	void execute(String sql);
	
	/**
	 * Ejecuta el sql pasado como argumento
	 * 
	 * @return resultado de la consulta
	 */
	public Object uniqueResult(String sql);
	
	/**
	 * Ejecuta el sql pasado como argumento
	 * 
	 * @return resultado de la consulta
	 */
	@SuppressWarnings("rawtypes")
	public List list(String sql);

}
