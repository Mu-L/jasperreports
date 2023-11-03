/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2023 Cloud Software Group, Inc. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Contributors:
 * Eugene D - eugenedruy@users.sourceforge.net 
 * Adrian Jackson - iapetus@users.sourceforge.net
 * David Taylor - exodussystems@users.sourceforge.net
 * Lars Kristensen - llk@users.sourceforge.net
 */
package net.sf.jasperreports.engine.convert;

import net.sf.jasperreports.charts.ChartVisitor;
import net.sf.jasperreports.charts.JRChart;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.base.JRBasePrintFrame;


/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 */
public class ChartsConvertVisitor extends ConvertVisitor implements ChartVisitor
{
	
	/**
	 *
	 */
	public ChartsConvertVisitor(ReportConverter reportConverter)
	{
		this(reportConverter, null);
	}

	/**
	 *
	 */
	public ChartsConvertVisitor(ReportConverter reportConverter, JRBasePrintFrame parentFrame)
	{
		super(reportConverter, parentFrame);
	}

	@Override
	public void visitChart(JRChart chart)
	{
		JRPrintElement printImage = null;//FIXME ChartConverter.getInstance().convert(reportConverter, chart);
		addElement(parentFrame, printImage);
		addContour(reportConverter, parentFrame, printImage);
	}
	
}
